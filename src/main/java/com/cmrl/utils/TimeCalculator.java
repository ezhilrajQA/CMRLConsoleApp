package com.cmrl.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Map;

/**
 * Utility class to calculate travel time between metro stations
 * and determine ticket validity duration.
 * <p>
 * This class provides static methods to:
 * <ul>
 *   <li>Estimate journey time based on stops and interchanges.</li>
 *   <li>Return ticket validity duration for different ticket types.</li>
 * </ul>
 * 🚇 Example: Can be used in metro ticketing or route-planning systems.
 *
 * <p><b>Thread Safety:</b> Since this class only contains static methods and
 * immutable constants, it is thread-safe.</p>
 */
public class TimeCalculator {

    private TimeCalculator(){}

    /** Logger instance for debugging and monitoring. */
    private static final Logger log = LogManager.getLogger(TimeCalculator.class);

    /** Average travel time per stop in minutes. */
    private static final double TIME_PER_STOP = 1.5;  // minutes per stop

    /** Extra delay (in minutes) when interchanging between lines. */
    private static final int INTERCHANGE_DELAY = 3;   // minutes


    /**
     * Predefined validity durations (in minutes) for different ticket types.
     * <ul>
     *     <li>SJT → 120 mins (2 hours)</li>
     *     <li>RJT → 180 mins (3 hours)</li>
     *     <li>FAMILY → 300 mins (5 hours)</li>
     *     <li>GROUP → 300 mins (5 hours)</li>
     *     <li>SVP → 1440 mins (1 day)</li>
     * </ul>
     */
    private static final Map<String, Integer> VALIDITY_MAP = Map.of(
            "SJT", 120,     // 2 hours
            "RJT", 180,     // 3 hours
            "FAMILY", 300,  // 5 hours
            "GROUP", 300,   // 5 hours
            "SVP", 1440     // 1 day
    );

    /**
     * Calculate estimated travel time in minutes based on number of stops
     * and whether the journey includes an interchange.
     *
     * @param stops       number of stops (must be >= 0)
     * @param interchange {@code true} if journey includes an interchange, {@code false} otherwise
     * @return estimated travel time in minutes
     *
     * <p><b>Example:</b>
     * <pre>{@code
     * int time = TimeCalculator.calculateTime(10, true);
     * // returns ~18 minutes (15 for stops + 3 for interchange)
     * }</pre>
     */
    public static int calculateTime(int stops, boolean interchange) {
        int time = (int) Math.round(stops * TIME_PER_STOP);
        if (interchange) {
            time += INTERCHANGE_DELAY;
        }
        log.debug("Calculated travel time: {} mins for {} stops (interchange: {})", time, stops, interchange);
        return time;
    }

    /**
     * Get ticket validity duration (in minutes) for a given ticket type.
     * If the type is not recognized, default validity = 120 minutes.
     *
     * @param ticketType type of ticket (e.g., SJT, RJT, FAMILY, GROUP, SVP)
     * @return validity in minutes
     *
     * <p><b>Example:</b>
     * <pre>{@code
     * int validity = TimeCalculator.getValidity("SJT");
     * // returns 120
     * }</pre>
     */
    public static int getValidity(String ticketType) {
        int validity = VALIDITY_MAP.getOrDefault(ticketType.toUpperCase(), 120);
        log.debug("Ticket type: {} → validity: {} mins", ticketType, validity);
        return validity;
    }
}

