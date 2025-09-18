package com.cmrl.utils;

import com.cmrl.constants.AppConstants;
import com.cmrl.model.Station;
import com.cmrl.model.StationsData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Singleton class responsible for loading and providing metro station data.
 * <p>
 * Features:
 * <ul>
 *     <li>Implements Singleton pattern to ensure only one instance exists</li>
 *     <li>Loads station data from a JSON file (configured via {@link AppConstants#STATIONJSON_PATH})</li>
 *     <li>Provides read-only access to the list of loaded stations</li>
 *     <li>Uses Log4j2 for structured logging</li>
 * </ul>
 *
 * Example usage:
 * <pre>
 *     StationLoader loader = StationLoader.getInstance();
 *     loader.loadStations();
 *     List&lt;Station&gt; stations = loader.getStations();
 * </pre>
 *
 * Thread-safe: {@link #getInstance()} is synchronized to handle concurrent access safely.
 *
 * @author Ezhil
 */
public class StationLoader {

    /** Logger instance for logging station load and access operations. */
    private static final Logger log = LogManager.getLogger(StationLoader.class);

    /** Singleton instance of {@code StationLoader}. */
    private static StationLoader instance; // Singleton instance

    /** Holds the list of metro stations after loading from JSON. */
    private final List<Station> metroStations = new ArrayList<>();

    /**
     * Private constructor to enforce Singleton pattern.
     * Prevents direct instantiation from outside.
     */
    private StationLoader() {}


    /**
     * Provides the global access point for obtaining the singleton instance.
     * <p>
     * - If no instance exists, a new one is created. <br>
     * - If an instance already exists, the existing one is returned.
     * </p>
     *
     * @return the singleton {@code StationLoader} instance
     */
    public static synchronized StationLoader getInstance() {
        if (instance == null) {
            log.info("Creating new StationLoader instance...");
            instance = new StationLoader();
        } else {
            log.debug("StationLoader instance already exists.");
        }
        return instance;
    }

    /**
     * Loads metro station data from a JSON file into memory.
     * <p>
     * - The JSON file path is defined in {@link AppConstants#STATIONJSON_PATH}. <br>
     * - Blue line and Green line stations are merged into a single list. <br>
     * - Data is only loaded once. Subsequent calls are ignored.
     * </p>
     *
     * @throws IOException if there is an error while reading the JSON file
     */
    public void loadStations() throws IOException {
        if (!metroStations.isEmpty()) {
            log.info("⚡ Stations already loaded ({} stations). Skipping reload.", metroStations.size());
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        try {
            log.info("Loading stations from JSON file: {}", AppConstants.STATIONJSON_PATH);
            StationsData data = mapper.readValue(new File(AppConstants.STATIONJSON_PATH), StationsData.class);

            metroStations.addAll(data.getBlueLineStations());
            metroStations.addAll(data.getGreenLineStations());

            log.info("✅ Loaded {} stations successfully!", metroStations.size());

        } catch (IOException e) {
            log.error("⚠️ Failed to load stations: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Provides read-only access to the loaded station list.
     * <p>
     * The returned list is unmodifiable to ensure immutability and prevent
     * external modifications to the internal station data.
     * </p>
     *
     * @return an unmodifiable list of metro stations
     */
    public List<Station> getStations() {
        log.debug("Returning list of {} stations.", metroStations.size());
        return Collections.unmodifiableList(metroStations);
    }
}


