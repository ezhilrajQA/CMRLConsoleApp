package com.cmrl.service;

import com.cmrl.constants.AppConstants;
import com.cmrl.model.StationsData;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.File;
import java.util.ArrayList;

/**
 * 🛠️ ManageStationsService handles the loading and saving of station data
 * for the Metro Bus Booking System. It provides functionalities to read and write
 * station information to a JSON file using Jackson ObjectMapper.
 * <p>
 * This class ensures safe loading and saving of station data, logging all operations
 * using Log4j2. If the JSON file is missing or empty, it initializes a fresh dataset.
 * </p>
 *
 * <p>
 * All methods are static since station data management does not require instance state.
 * </p>
 *
 * @author Ezhil
 */
public class ManageStationsService {

    /** Logger instance for logging station operations */
    private static final Logger log = LogManager.getLogger(ManageStationsService.class);

    /** Jackson ObjectMapper for JSON serialization/deserialization */
    private static final ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /** JSON file storing station data */
    private static final File stationFile = new File(AppConstants.STATIONJSON_PATH);

    private ManageStationsService(){}

    /**
     * Loads station data from the JSON file.
     * <p>
     * If the file does not exist or is empty, a fresh {@link StationsData} object
     * is returned with empty Blue and Green line station lists.
     * </p>
     *
     * <p>Logs the number of stations loaded and prints messages for SDET-friendly console output.</p>
     *
     * @return {@link StationsData} containing Blue and Green line stations
     */
    public static StationsData loadStationsData() {
        try {
            if (!stationFile.exists() || stationFile.length() == 0) {
                log.info("No existing station data found. Starting with a fresh dataset.");
                System.out.println("\n📂 No existing station data found. Starting with a fresh dataset...\n");
                return new StationsData(new ArrayList<>(), new ArrayList<>());
            }

            StationsData data = mapper.readValue(stationFile, StationsData.class);
            log.info("Stations data loaded successfully. Blue Line: {}, Green Line: {}",
                    data.getBlueLineStations().size(), data.getGreenLineStations().size());
            System.out.println("\n📂 Stations data loaded successfully! ✅\n");
            return data;
        } catch (Exception e) {
            log.error("Failed to load stations data: {}", e.getMessage(), e);
            System.err.println("\n⚠️ Failed to load stations data: " + e.getMessage());
            System.out.println("➡️ A new empty dataset will be used.\n");
            return new StationsData(new ArrayList<>(), new ArrayList<>());
        }
    }

    /**
     * Saves the given {@link StationsData} object to the JSON file.
     * <p>
     * Writes data in a pretty-printed format for readability.
     * Logs success or failure and prints SDET-friendly messages.
     * </p>
     *
     * @param data {@link StationsData} containing Blue and Green line stations to save
     */
    public static void saveStationsData(StationsData data) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(stationFile, data);
            log.info("Stations data saved successfully. Blue Line: {}, Green Line: {}",
                    data.getBlueLineStations().size(), data.getGreenLineStations().size());
            System.out.println("\n💾 Stations data saved successfully! ✅\n");
        } catch (Exception e) {
            log.error("Failed to save stations data!", e);
            System.err.println("\n❌ Failed to save stations data!");
        }
    }

}

