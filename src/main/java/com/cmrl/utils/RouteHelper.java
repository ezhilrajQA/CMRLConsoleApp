package com.cmrl.utils;

import com.cmrl.model.JourneyResult;
import com.cmrl.model.Station;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.*;


/**
 * Utility class to calculate metro journeys between stations,
 * considering same-line and cross-line routes with interchanges.
 * <p>
 * Features:
 * <ul>
 *     <li>Handles same-line journeys by extracting a line segment between two stations</li>
 *     <li>Supports cross-line journeys using predefined interchange stations</li>
 *     <li>Calculates total stops and returns a {@link JourneyResult}</li>
 *     <li>Uses Log4j2 for structured logging</li>
 * </ul>
 *
 * <p><b>Interchange Stations:</b> Chennai Central, Alandur</p>
 *
 * Example usage:
 * <pre>
 *     JourneyResult result = RouteHelper.calculateJourney(allStations, startStation, endStation);
 * </pre>
 *
 * @author Ezhil
 */
public class RouteHelper {

    private RouteHelper(){}

    /** Logger instance for tracking journey calculation steps. */
    private static final Logger log = LogManager.getLogger(RouteHelper.class);

    /** Predefined list of interchange stations for cross-line journeys. */
    private static final List<String> INTERCHANGE_STATIONS = Arrays.asList("Chennai Central", "Alandur");


    /**
     * Calculates a metro journey between a start and end station.
     * <p>
     * - If both stations belong to the same line, a direct path is returned. <br>
     * - If they belong to different lines, the nearest interchange station is used to switch lines. <br>
     * - Logs detailed steps during calculation.
     * </p>
     *
     * @param stations the list of all stations in the metro system
     * @param start    the starting station
     * @param end      the destination station
     * @return a {@link JourneyResult} containing:
     *         <ul>
     *             <li>ordered list of stations in the journey</li>
     *             <li>number of stops</li>
     *             <li>interchange station used (if any)</li>
     *         </ul>
     */


    public static JourneyResult calculateJourney(List<Station> stations, Station start, Station end) {
        log.info("Calculating journey from '{}' (Line: {}) to '{}' (Line: {})",
                start.getName(), start.getLine(), end.getName(), end.getLine());

        List<Station> journeyStations = new ArrayList<>();
        String interchangeStation = null;

        if (start.getLine().equalsIgnoreCase(end.getLine())) {
            // ✅ Same line journey
            log.debug("Journey is on the same line: {}", start.getLine());
            journeyStations.addAll(getLinePart(getLine(stations, start), start, end));
        } else {
            // ✅ Cross-line journey
            log.debug("Journey involves line change from '{}' to '{}'", start.getLine(), end.getLine());

            List<Station> startLine = getLine(stations, start);
            List<Station> endLine = getLine(stations, end);

            interchangeStation = findNearestInterchange(startLine, start);
            log.debug("Nearest interchange station found: {}", interchangeStation);

            if (interchangeStation == null) {
                log.warn("No valid interchange found for cross-line journey from '{}'", start.getName());
                return new JourneyResult(List.of(), 0, null); // No valid route
            }

            Station startInterchange = findStationByName(startLine, interchangeStation);
            Station endInterchange = findStationByName(endLine, interchangeStation);

            if (startInterchange == null || endInterchange == null) {
                log.warn("Invalid interchange stations. Start: {}, End: {}", startInterchange, endInterchange);
                return new JourneyResult(List.of(), 0, null); // Invalid interchange
            }

            // Start → Interchange
            journeyStations.addAll(getLinePart(startLine, start, startInterchange));
            log.debug("Added journey segment from '{}' to interchange '{}'", start.getName(), interchangeStation);

            // Interchange → Destination
            List<Station> endPart = getLinePart(endLine, endInterchange, end);
            if (!endPart.isEmpty()) endPart.remove(0); // avoid duplicate interchange
            journeyStations.addAll(endPart);
            log.debug("Added journey segment from interchange '{}' to destination '{}'", interchangeStation, end.getName());
        }

        int stops = Math.max(0, journeyStations.size() - 1);
        log.info("Journey calculated successfully with {} stops, interchange: {}", stops, interchangeStation);
        return new JourneyResult(journeyStations, stops, interchangeStation);
    }

    /**
     * Retrieves all stations belonging to the same line as the given station.
     *
     * @param allStations list of all metro stations
     * @param station     the reference station
     * @return a list of stations belonging to the same line
     */
    private static List<Station> getLine(List<Station> allStations, Station station) {
        return allStations.stream()
                .filter(s -> s.getLine().equalsIgnoreCase(station.getLine()))
                .toList();
    }


    /**
     * Finds the nearest interchange station from the given start station on the line.
     * <p>
     * The nearest interchange is determined by index distance along the line.
     * </p>
     *
     * @param lineStations list of stations on the current line
     * @param start        the starting station
     * @return the name of the nearest interchange station, or {@code null} if none exists
     */
    private static String findNearestInterchange(List<Station> lineStations, Station start) {
        int startIndex = findIndexByName(lineStations, start.getName());
        if (startIndex == -1) return null;

        return INTERCHANGE_STATIONS.stream()
                .map(i -> new AbstractMap.SimpleEntry<>(i, findIndexByName(lineStations, i)))
                .filter(e -> e.getValue() != -1)
                .min(Comparator.comparingInt(e -> Math.abs(e.getValue() - startIndex)))
                .map(Map.Entry::getKey)
                .orElse(null);
    }


    /**
     * Extracts a segment of stations between two points (start → end) on a line.
     * <p>
     * - If {@code startIndex <= endIndex}, returns a forward segment. <br>
     * - Otherwise, returns a reversed segment.
     * </p>
     *
     * @param lineStations list of stations on the line
     * @param start        the starting station
     * @param end          the ending station
     * @return a list of stations forming the journey segment,
     *         or an empty list if either station is not found
     */
    private static List<Station> getLinePart(List<Station> lineStations, Station start, Station end) {
        int startIndex = findIndexByName(lineStations, start.getName());
        int endIndex = findIndexByName(lineStations, end.getName());
        if (startIndex == -1 || endIndex == -1) return Collections.emptyList();

        List<Station> segment = (startIndex <= endIndex)
                ? new ArrayList<>(lineStations.subList(startIndex, endIndex + 1))
                : reverseSublist(lineStations, endIndex, startIndex);

        log.debug("Line segment from '{}' to '{}' with {} stations", start.getName(), end.getName(), segment.size());
        return segment;
    }


    /**
     * Extracts a reversed sublist between two indices of a line.
     *
     * @param stations list of stations on the line
     * @param from     starting index
     * @param to       ending index
     * @return a reversed sublist of stations
     */
    private static List<Station> reverseSublist(List<Station> stations, int from, int to) {
        List<Station> sublist = new ArrayList<>(stations.subList(from, to + 1));
        Collections.reverse(sublist);
        return sublist;
    }

    /**
     * Finds the index of a station by name within a line.
     *
     * @param lineStations list of stations on a line
     * @param name         the station name
     * @return the index of the station, or -1 if not found
     */
    private static int findIndexByName(List<Station> lineStations, String name) {
        for (int i = 0; i < lineStations.size(); i++) {
            if (lineStations.get(i).getName().equalsIgnoreCase(name)) return i;
        }
        return -1;
    }

    /**
     * Finds a {@link Station} object by its name within a line.
     *
     * @param lineStations list of stations on a line
     * @param name         the station name
     * @return the matching {@link Station}, or {@code null} if not found
     */
    private static Station findStationByName(List<Station> lineStations, String name) {
        return lineStations.stream()
                .filter(s -> s.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}