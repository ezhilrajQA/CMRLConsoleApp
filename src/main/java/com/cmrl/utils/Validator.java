package com.cmrl.utils;

import com.cmrl.constants.AppConstants;
import com.cmrl.enums.TicketType;
import com.cmrl.exceptions.EmptyInputException;
import com.cmrl.exceptions.UserAlreadyExistsException;
import com.cmrl.model.Station;
import com.cmrl.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.List;


/**
 * Utility class providing various validation methods
 * for user inputs, authentication, ticket booking,
 * and metro station details.
 * <p>
 * This class centralizes input validation to ensure:
 * <ul>
 *     <li>User credentials (username, password, confirm password) are valid.</li>
 *     <li>Admin login is properly authenticated.</li>
 *     <li>Station names, IDs, and lines follow predefined formats.</li>
 *     <li>Ticket types and counts meet business rules (Family, Group, etc.).</li>
 * </ul>
 *
 * <p><b>Usage:</b>
 * All methods are static and throw {@link EmptyInputException}
 * or {@link IllegalArgumentException} if validation fails.</p>
 *
 * <p><b>Thread Safety:</b> Methods are stateless and thread-safe.</p>
 */
public class Validator {

    private  Validator(){}

    /** Logger instance for debugging and validation logs. */
    private static final Logger log = LogManager.getLogger(Validator.class);

    // ===================== Username Validation =====================

    /**
     * Validates a username based on length, allowed characters,
     * uniqueness, and format rules.
     *
     * @param username  the username to validate
     * @param userdata  existing list of users to check for duplicates
     * @throws EmptyInputException   if username is null, empty, too short,
     *                               too long, invalid format, or contains spaces
     * @throws IllegalArgumentException if username already exists
     */
    public static void validateUsername(String username, List<User> userdata) throws EmptyInputException {
        log.debug("Validating username: {}", username);

        if (username == null || username.trim().isEmpty()) {
            log.warn("Username validation failed: empty input");
            throw new EmptyInputException("⚠️ Username cannot be empty. Please provide a valid username.");
        }

        if (username.length() < 7) {
            log.warn("Username too short: {}", username);
            throw new EmptyInputException("⚠️ Username is too short! It must contain at least 7 characters.");
        }

        if (username.length() > 15) {
            log.warn("Username too long: {}", username);
            throw new EmptyInputException("⚠️ Username is too long! It must not exceed 15 characters.");
        }

        if (!username.matches("^\\w+$")) {
            log.warn("Username contains invalid characters: {}", username);
            throw new EmptyInputException("⚠️ Invalid username! Only letters, numbers, and underscores (_) are allowed.");
        }

        if (username.contains(" ")) {
            log.warn("Username contains spaces: {}", username);
            throw new EmptyInputException("⚠️ Username must not contain spaces. Use underscore (_) instead.");
        }

        boolean exists = userdata.stream().anyMatch(u -> u.getUsername().equalsIgnoreCase(username));
        if (exists) {
            log.warn("Username already exists: {}", username);
            throw new UserAlreadyExistsException("❌ Username already exists! Please choose another one.");
        }

        log.debug("Username '{}' passed validation", username);
    }

    // ===================== Password Validation =====================

    /**
     * Validates a password to ensure it meets security requirements:
     * <ul>
     *     <li>Minimum length of 8</li>
     *     <li>Contains uppercase, lowercase, digit, and special character</li>
     * </ul>
     *
     * @param password password string to validate
     * @throws EmptyInputException if password does not meet requirements
     */
    public static void validatePassword(String password) throws EmptyInputException {
        log.debug("Validating password");

        if (password == null || password.trim().isEmpty()) {
            log.warn("Password validation failed: empty input");
            throw new EmptyInputException("⚠️ Password cannot be empty. Please enter a valid password.");
        }

        if (password.length() < 8) {
            log.warn("Password too short");
            throw new EmptyInputException("⚠️ Weak password! It must be at least 8 characters long.");
        }

        if (!password.matches(".*[A-Z].*")) {
            log.warn("Password missing uppercase letter");
            throw new EmptyInputException("⚠️ Password must include at least one uppercase letter (A–Z).");
        }

        if (!password.matches(".*[a-z].*")) {
            log.warn("Password missing lowercase letter");
            throw new EmptyInputException("⚠️ Password must include at least one lowercase letter (a–z).");
        }

        if (!password.matches(".*\\d.*")) {
            log.warn("Password missing digit");
            throw new EmptyInputException("⚠️ Password must include at least one number (0–9).");
        }

        if (!password.matches(".*[@$!%*?&].*")) {
            log.warn("Password missing special character");
            throw new EmptyInputException("⚠️ Password must include at least one special character (@ $ ! % * ? &).");
        }

        log.debug("Password passed validation");
    }

    // ===================== Confirm Password Validation =====================

    /**
     * Ensures that password and confirm password match.
     *
     * @param password        the original password
     * @param confirmPassword the confirmation password
     * @throws EmptyInputException if passwords do not match
     */
    public static void validateConfirmPassword(String password, String confirmPassword) throws EmptyInputException {
        log.debug("Validating confirm password");

        if (!password.equals(confirmPassword)) {
            log.warn("Password and confirm password do not match");
            throw new EmptyInputException("❌ Password and Confirm Password do not match. Please try again.");
        }

        log.debug("Confirm password matches");
    }

    // ===================== Station Validation =====================

    /**
     * Validates station details, ensuring:
     * <ul>
     *     <li>Station name is not empty and only contains valid characters</li>
     *     <li>Source and destination stations are not the same</li>
     * </ul>
     *
     * @param stationName station name to validate
     * @param fromStation source station
     * @param toStation   destination station
     * @throws EmptyInputException if validation fails
     */
    public static void validateStation(String stationName, String fromStation, String toStation) throws EmptyInputException {
        log.debug("Validating station: {}", stationName);

        if (stationName == null || stationName.trim().isEmpty()) {
            log.warn("Station name empty");
            throw new EmptyInputException("⚠️ Station name cannot be empty. Please enter a valid station name.");
        }

        if (!stationName.matches("^[a-zA-Z0-9 ]+$")) {
            log.warn("Station name contains invalid characters: {}", stationName);
            throw new EmptyInputException("⚠️ Station name contains invalid characters. Only letters, and spaces are allowed.");
        }

        if (fromStation.equalsIgnoreCase(toStation)) {
            log.warn("'From' and 'To' stations are the same: {}", fromStation);
            throw new EmptyInputException("⚠️ 'From Station' and 'To Station' cannot be the same. Please choose different stations.");
        }

        log.debug("Station '{}' passed validation", stationName);
    }


    /**
     * Validates and converts ticket type input into {@link TicketType}.
     *
     * @param typeInput ticket type string (e.g., SJT, RJT, FAMILY, GROUP)
     * @return corresponding {@link TicketType}
     * @throws EmptyInputException if ticket type is empty or invalid
     */
    public static TicketType validateTicketType(String typeInput) throws EmptyInputException {
        log.debug("Validating ticket type: {}", typeInput);

        if (typeInput == null || typeInput.trim().isEmpty()) {
            log.warn("Ticket type empty");
            throw new EmptyInputException("⚠️ Ticket type cannot be empty. Please enter SJT / RJT / FAMILY / GROUP.");
        }
        try {
            TicketType type = TicketType.valueOf(typeInput.toUpperCase());
            log.debug("Ticket type '{}' valid", typeInput);
            return type;
        } catch (IllegalArgumentException e) {
            log.warn("Invalid ticket type entered: {}", typeInput);
            throw new EmptyInputException("❌ Invalid ticket type entered. Allowed values: SJT, RJT, FAMILY, GROUP.");
        }
    }


    /**
     * Validates number of tickets based on ticket type.
     * <ul>
     *     <li>Must be greater than 0</li>
     *     <li>Family ticket requires at least 2 persons</li>
     *     <li>Group ticket requires at least 20 persons</li>
     * </ul>
     *
     * @param noOfTickets number of tickets
     * @param ticketType  type of ticket
     * @throws EmptyInputException if validation fails
     */
    public static void validateTicketCount(int noOfTickets, TicketType ticketType) throws EmptyInputException {
        log.debug("Validating ticket count: {} for ticket type: {}", noOfTickets, ticketType);

        if (noOfTickets <= 0) {
            log.warn("Ticket count <= 0");
            throw new EmptyInputException("⚠️ Number of tickets must be greater than 0.");
        }
        if (ticketType == TicketType.FAMILY && noOfTickets < 2) {
            log.warn("Family ticket requires at least 2 tickets");
            throw new EmptyInputException("⚠️ Family ticket requires at least 2 persons. Please re-enter.");
        }
        if (ticketType == TicketType.GROUP && noOfTickets < 20) {
            log.warn("Group ticket requires at least 20 tickets");
            throw new EmptyInputException("⚠️ Group ticket requires a minimum of 20 persons. Please re-enter.");
        }

        log.debug("Ticket count valid");
    }

    // ===================== Admin Validation =====================

    /**
     * Validates admin login credentials.
     *
     * @param username admin username
     * @param password admin password
     * @throws EmptyInputException if credentials are empty or invalid
     */
    public static void validateAdminLogin(String username, String password) throws EmptyInputException {
        log.debug("Validating admin login for username: {}", username);

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            log.warn("Admin credentials empty");
            throw new EmptyInputException("⚠️ Admin credentials cannot be empty. Please enter both username and password.");
        }

        if (!username.equals(AppConstants.getADMIN_USER()) || !password.equals(AppConstants.getADMIN_PASS())) {
            log.warn("Invalid admin credentials: {}", username);
            throw new EmptyInputException("❌ Invalid Admin credentials. Access denied!");
        }

        log.debug("Admin login successful");
    }

    // ===================== Station Line & Name Validations =====================

    /**
     * Validates station line input.
     * <ul>
     *     <li>Must not be empty</li>
     *     <li>Must be either "Blue" or "Green"</li>
     * </ul>
     *
     * @param line station line input
     * @throws EmptyInputException if validation fails
     */
    public static void validateStationLine(String line) throws EmptyInputException {
        log.debug("Validating station line: {}", line);

        if (line == null || line.isBlank()) {
            log.warn("Station line empty");
            throw new EmptyInputException("⚠️ Station line cannot be empty! ❌ Please enter either 'Blue' or 'Green'.");
        }

        if (!(line.equalsIgnoreCase("Blue") || line.equalsIgnoreCase("Green"))) {
            log.warn("Invalid station line: {}", line);
            throw new EmptyInputException("⚠️ Invalid Station Line! 🚉 Allowed options: 'Blue' or 'Green'.");
        }

        log.debug("Station line '{}' valid", line);
    }


    /**
     * Validates station name format.
     *
     * @param name station name
     * @throws EmptyInputException if name is empty or contains invalid characters
     */
    public static void validateStationName(String name) throws EmptyInputException {
        log.debug("Validating station name: {}", name);

        if (name == null || name.isBlank()) {
            log.warn("Station name empty");
            throw new EmptyInputException("⚠️ Station name cannot be empty! 📝 Please enter a valid name.");
        }

        if (!name.matches("^[a-zA-Z0-9 ]+$")) {
            log.warn("Invalid characters in station name: {}", name);
            throw new EmptyInputException("⚠️ Station name contains invalid characters! 🚫 Only letters, numbers, and spaces are allowed.");
        }

        log.debug("Station name '{}' valid", name);
    }


    /**
     * Ensures no duplicate station exists with the given name.
     *
     * @param name     station name to validate
     * @param stations existing list of stations
     * @throws EmptyInputException if a duplicate is found
     */
    public static void validateDuplicateStation(String name, List<Station> stations) throws EmptyInputException {
        log.debug("Checking duplicate station: {}", name);

        if (stations.stream().anyMatch(s -> s.getName().equalsIgnoreCase(name))) {
            log.warn("Duplicate station found: {}", name);
            throw new EmptyInputException("⚠️ Duplicate Station Found! 🚫 The station '" + name + "' already exists. Please choose a different name.");
        }

        log.debug("No duplicate station found for '{}'", name);
    }


    /**
     * Validates station ID format.
     * <ul>
     *     <li>Must start with "B" (Blue) or "G" (Green)</li>
     *     <li>Must be followed by digits</li>
     * </ul>
     *
     * @param id station ID
     * @throws EmptyInputException if validation fails
     */
    public static void validateStationId(String id) throws EmptyInputException {
        log.debug("Validating station ID: {}", id);

        if (id == null || id.isBlank()) {
            log.warn("Station ID empty");
            throw new EmptyInputException("⚠️ Station ID cannot be empty!");
        }

        if (!(id.startsWith("B") || id.startsWith("G"))) {
            log.warn("Station ID must start with B or G: {}", id);
            throw new EmptyInputException("⚠️ Station ID must start with 'B' (Blue) or 'G' (Green).");
        }

        if (!id.substring(1).matches("\\d+")) {
            log.warn("Station ID numeric part invalid: {}", id);
            throw new EmptyInputException("⚠️ Station ID must be followed by numbers (e.g., B1, G2).");
        }

        log.debug("Station ID '{}' valid", id);
    }

}

