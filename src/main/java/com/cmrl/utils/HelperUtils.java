package com.cmrl.utils;

import com.cmrl.constants.AppConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.UUID;

import static com.cmrl.constants.AppConstants.BASE_PATH;


/**
 * Utility class providing helper methods for common operations used
 * across the metro ticketing and travel planning system.
 * <p>
 * Includes functionalities such as:
 * <ul>
 *     <li>Ticket ID generation</li>
 *     <li>Formatted date and time retrieval</li>
 *     <li>Directory creation for reports and QR tickets</li>
 * </ul>
 * </p>
 *
 * <p><b>Dependencies:</b> Log4j2 for logging, Java Time API for date/time operations,
 * and Java IO for directory handling.</p>
 *
 * @author Ezhil
 */
public class HelperUtils {

    private HelperUtils(){}

    /** Logger instance for logging debug and error information. */
    private static final Logger log = LogManager.getLogger(HelperUtils.class);


    /**
     * Generates a unique ticket ID using UUID.
     * <p>
     * Only the first 8 characters are used, converted to uppercase
     * for readability.
     * </p>
     *
     * @return a unique ticket ID string (8-character uppercase).
     */
    public static String generateTicketID() {
        String ticketID = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.debug("Generated Ticket ID: {}", ticketID);
        return ticketID;
    }


    /**
     * Returns the current date in a human-friendly format.
     * <p>
     * Format example: <code>21st September, Sunday</code>
     * </p>
     *
     * @return today's formatted date with suffix (e.g., "st", "nd", "rd", "th").
     */
    public static String gettodayDate() {
        LocalDate today = LocalDate.now();

        int day = today.getDayOfMonth();
        String dayWithSuffix = getDayWithSuffix(day);
        String month = today.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        String dayOfWeek = today.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        String formattedDate = dayWithSuffix + " " + month + ", " + dayOfWeek;
        log.debug("Today's date calculated: {}", formattedDate);
        return formattedDate;
    }


    /**
     * Returns the current system time formatted as hh:mm AM/PM.
     * <p>
     * Example: <code>07:45 PM</code>
     * </p>
     *
     * @return formatted current time string.
     */
    public static String getCurrentTime() {
        LocalTime now = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        String formattedTime = now.format(formatter);
        log.debug("Current time calculated: {}", formattedTime);
        return formattedTime;
    }

    /**
     * Creates the required directories for the application:
     * <ul>
     *     <li>Reports directory (Excel reports)</li>
     *     <li>QR Tickets directory</li>
     * </ul>
     * <p>
     * Logs success or failure of directory creation.
     * </p>
     */
    public static void createDirectories() {

        log.debug("📂 Checking/creating application directories...");
        File base = new File(BASE_PATH);
        File reports = new File(base, AppConstants.EXCELREPORTS_PATH);
        File qrTickets = new File(base, AppConstants.TICKETSQR_PATH);

        try {
            if (!base.exists() && !base.mkdirs()) {
                throw new IOException("⚠️ Failed to create base folder: " + base.getAbsolutePath());
            }
            if (!reports.exists() && !reports.mkdirs()) {
                throw new IOException("⚠️ Failed to create Reports folder: " + reports.getAbsolutePath());
            }
            if (!qrTickets.exists() && !qrTickets.mkdirs()) {
                throw new IOException("⚠️ Failed to create QRTickets folder: " + qrTickets.getAbsolutePath());
            }

            log.info("Directories created or already exist: Base={}, Reports={}, QRTickets={}",
                    base.getAbsolutePath(), reports.getAbsolutePath(), qrTickets.getAbsolutePath());

        } catch (IOException e) {
            log.error("Directory creation failed: {}", e.getMessage(), e);
            System.out.println(e.getMessage());
        }
    }


    /**
     * Returns the day of the month with its ordinal suffix.
     * <p>
     * Examples:
     * <ul>
     *     <li>1 → 1st</li>
     *     <li>2 → 2nd</li>
     *     <li>3 → 3rd</li>
     *     <li>4 → 4th</li>
     *     <li>11–13 → always "th"</li>
     * </ul>
     * </p>
     *
     * @param day the day of the month (1–31).
     * @return the day number with suffix.
     */
    private static String getDayWithSuffix(int day) {
        if (day >= 11 && day <= 13) return day + "th";
        switch (day % 10) {
            case 1: return day + "st";
            case 2: return day + "nd";
            case 3: return day + "rd";
            default: return day + "th";
        }
    }


}
