package com.cmrl.service;

import com.cmrl.exceptions.EmptyInputException;
import com.cmrl.model.JourneyResult;
import com.cmrl.model.Station;
import com.cmrl.utils.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * 🚇 TravelPlannerService provides functionalities for planning a journey
 * between metro stations in the system. It validates user input, finds stations,
 * calculates journey details (stops, fare, time), and displays the journey path
 * with interchanges if any.
 * <p>
 * Uses {@link StationLoader} to fetch available stations and delegates journey
 * calculation to {@link RouteHelper}, {@link FareCalculator}, and {@link TimeCalculator}.
 * </p>
 * <p>
 * Logs all actions for debugging and provides user-friendly console outputs.
 * </p>
 *
 * ✅ Key Features:
 * <ul>
 *   <li>Searches for stations by name</li>
 *   <li>Validates "From" and "To" inputs with retry mechanism</li>
 *   <li>Calculates fare, stops, and estimated time</li>
 *   <li>Displays journey path with interchanges</li>
 * </ul>
 */
public class TravelPlannerService {

    /** Logger instance for logging travel planning activities */
    private static final Logger log = LogManager.getLogger(TravelPlannerService.class);


    /** Scanner for reading user input */
    private final Scanner sc;


    /**
     * Constructor with dependency injection for user input.
     *
     * @param sc {@link Scanner} object for reading console input
     */
    public TravelPlannerService(Scanner sc) {
        this.sc = sc; // dependency injection
    }


    /**
     * Finds a station by its name from the loaded stations list.
     * <p>
     * If the station is found, logs success with station details.
     * Otherwise, logs a warning.
     * </p>
     *
     * @param name the station name to search
     * @return an {@link Optional} containing the found {@link Station},
     *         or empty if not found
     */
    public Optional<Station> findStationByName(String name) {
        Optional<Station> station = StationLoader.getInstance().getStations().stream()
                .filter(s -> s.getName().equalsIgnoreCase(name))
                .findFirst();
        station.ifPresentOrElse(
                s -> log.info("Station found: {} ({})", s.getName(), s.getLine()),
                () -> log.warn("Station not found: {}", name)
        );
        return station;
    }


    /**
     * Plans a journey between two stations provided by the user.
     * <p>
     * Steps performed:
     * <ol>
     *   <li>Prompts user for "From" and "To" stations</li>
     *   <li>Validates input using {@link Validator}</li>
     *   <li>Ensures valid stations exist, re-prompts if invalid</li>
     *   <li>Calculates journey using {@link RouteHelper}</li>
     *   <li>Computes fare and estimated time</li>
     *   <li>Prints journey details to console</li>
     * </ol>
     * </p>
     */
    public void planJourney() {
        log.info("Travel Planner initiated by user.");

        System.out.println("\n=======================================");
        System.out.println("      🗺️ Welcome to Travel Planner 🗺️     ");
        System.out.println("=======================================\n");

        Station start = null;
        Station end = null;

        System.out.print("🚉 Enter From Station: ");
        sc.nextLine();
        String from = sc.nextLine().trim();

        System.out.print("🚉 Enter To Station: ");
        String to = sc.nextLine().trim();

        try {
            Validator.validateStation(from, from, to);
            Validator.validateStation(to, from, to);
        } catch (EmptyInputException e) {
            log.warn("Station validation failed: {}", e.getMessage());
            System.out.println(e.getMessage());
            return;
        }

        // Ensure valid start station
        while (start == null) {
            start = findStationByName(from).orElse(null);
            if (start == null) {
                log.warn("Invalid 'From Station' entered: {}", from);
                System.out.print("⚠️ Invalid 'From Station' ❌ Please re-enter: ");
                from = sc.nextLine().trim();
                try {
                    Validator.validateStation(from, from, to);
                } catch (EmptyInputException e) {
                    System.out.println(e.getMessage());
                    from = null; // force retry
                }
            }
        }

        // Ensure valid end station
        while (end == null) {
            end = findStationByName(to).orElse(null);
            if (end == null) {
                log.warn("Invalid 'To Station' entered: {}", to);
                System.out.print("⚠️ Invalid 'To Station' ❌ Please re-enter: ");
                to = sc.nextLine().trim();
                try {
                    Validator.validateStation(to, from, to);
                } catch (EmptyInputException e) {
                    System.out.println(e.getMessage());
                    to = null; // force retry
                }
            }
        }

        FareCalculator fareCalculator = new FareCalculator();
        JourneyResult result = RouteHelper.calculateJourney(StationLoader.getInstance().getStations(), start, end);

        List<Station> journey = result.getJourneyStations();
        int stops = result.getStops();
        boolean hasInterchange = result.getInterchangeStation() != null;
        int fare = fareCalculator.getFare(stops);
        int estimatedTime = TimeCalculator.calculateTime(stops, hasInterchange);

        log.info("Journey planned from '{}' to '{}'. Stops: {}, Fare: {}, Estimated Time: {} mins, Interchange: {}",
                start.getName(), end.getName(), stops, fare, estimatedTime, hasInterchange);

        printJourneyDetails(start, end, result, journey, stops, fare, estimatedTime, hasInterchange);
    }


    /**
     * Prints the journey details to the console.
     * <p>
     * Displays:
     * <ul>
     *   <li>Start and End stations</li>
     *   <li>Interchange station (if applicable)</li>
     *   <li>Total stops</li>
     *   <li>Fare and estimated time</li>
     *   <li>Full journey path</li>
     * </ul>
     * </p>
     *
     * @param start the starting {@link Station}
     * @param end the destination {@link Station}
     * @param result the {@link JourneyResult} containing journey metadata
     * @param journey the ordered list of {@link Station} objects representing the path
     * @param stops total number of stops
     * @param fare calculated fare in INR
     * @param estimatedTime estimated travel time in minutes
     * @param hasInterchange whether the journey includes an interchange
     */
    private void printJourneyDetails(Station start, Station end, JourneyResult result,
                                     List<Station> journey, int stops, int fare,
                                     int estimatedTime, boolean hasInterchange) {

        log.info("Printing journey details for {} → {}", start.getName(), end.getName());

        System.out.println("\n=======================================");
        System.out.println("        🚇 CMRL Journey Planner 🚇       ");
        System.out.println("=======================================\n");

        System.out.println("📍 From: " + start.getName() + " (" + start.getLine() + ")");
        if (hasInterchange) {
            System.out.println("🔁 Interchange at: " + result.getInterchangeStation() + " 🚉 (Change Line)");
        }
        System.out.println("🏁 To: " + end.getName() + " (" + end.getLine() + ")");
        System.out.println("⏹ Stops: " + stops);
        System.out.println("💰 Fare: ₹" + fare);
        System.out.println("⏱ Estimated Time: " + estimatedTime + " mins\n");

        System.out.print("🛤 Journey Path: ");
        for (int i = 0; i < journey.size(); i++) {
            System.out.print(journey.get(i).getName());
            if (i < journey.size() - 1) System.out.print(" → ");
        }
        System.out.println("\n");

        System.out.println("=======================================");
        System.out.println("       🙌 Have a Safe Journey! 🙌       ");
        System.out.println("=======================================\n");
    }
}

