package com.cmrl.utils;

import com.cmrl.constants.AppConstants;
import com.cmrl.model.FareRule;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Utility class for calculating metro fares based on predefined rules.
 * <p>
 * This class loads fare rules from a JSON configuration file and applies them
 * to determine the correct fare for a given number of stops.
 * </p>
 *
 * <p><b>Features:</b></p>
 * <ul>
 *     <li>Reads fare rules from a JSON file at application startup.</li>
 *     <li>Ensures rules are immutable once loaded.</li>
 *     <li>Calculates fare based on the number of stops.</li>
 *     <li>Uses Log4j2 for logging debug and error information.</li>
 * </ul>
 *
 * <p><b>Dependencies:</b> Jackson library for JSON parsing.</p>
 *
 * @author Ezhil
 */
public class FareCalculator {

    /** Logger instance for logging fare calculation and rule-loading events. */
    private static final Logger log = LogManager.getLogger(FareCalculator.class);

    /** Immutable list of fare rules loaded from the JSON configuration file. */
    private List<FareRule> fareRules = new ArrayList<>();

    /**
     * Constructor that initializes the fare calculator by loading fare rules
     * from the JSON file defined in {@link AppConstants#FARERULEJSON_PATH}.
     * <p>
     * If the rules cannot be loaded, an error is logged, and the calculator
     * may return a default value (-1) during fare calculation.
     * </p>
     */
    public FareCalculator() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<FareRule> rules = mapper.readValue(
                    new File(AppConstants.FARERULEJSON_PATH),
                    mapper.getTypeFactory().constructCollectionType(List.class, FareRule.class)
            );
            this.fareRules = Collections.unmodifiableList(rules);

            log.info("Fare rules loaded successfully. Total rules: {}", fareRules.size());

        } catch (Exception e) {
            log.error("Failed to load fare rules from '{}': {}", AppConstants.FARERULEJSON_PATH, e.getMessage(), e);
            System.out.println("⚠️ Failed to load fare rules: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * Retrieves the fare for a given number of stops.
     * <p>
     * The method looks for the first {@link FareRule} whose range
     * (minStops ≤ stops ≤ maxStops) matches the provided stop count.
     * </p>
     *
     * @param stops the number of stops traveled.
     * @return the fare amount if a matching rule is found,
     *         or {@code -1} if no rule matches the input.
     */
    public int getFare(int stops) {
        int fare = fareRules.stream()
                .filter(rule -> stops >= rule.getMinStops() && stops <= rule.getMaxStops())
                .map(FareRule::getFare)
                .findFirst()
                .orElse(-1); // or throw custom exception

        log.debug("Calculating fare for {} stops: {}", stops, fare);
        return fare;
    }
}
