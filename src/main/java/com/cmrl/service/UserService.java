package com.cmrl.service;

import com.cmrl.constants.AppConstants;
import com.cmrl.exceptions.EmptyInputException;
import com.cmrl.exceptions.InvalidCredentialsException;
import com.cmrl.model.User;
import com.cmrl.utils.Validator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


/**
 * Service class for managing user operations such as signup and login.
 * <p>
 * This class handles persistence of users in a JSON file, password hashing,
 * input validation, and authentication of users. It uses Jackson for JSON
 * serialization/deserialization and Log4j2 for logging.
 * </p>
 *
 * <p><b>Features:</b></p>
 * <ul>
 *     <li>Signup with validation (username uniqueness, password rules, etc.).</li>
 *     <li>Secure password hashing using SHA-256.</li>
 *     <li>Login with credential verification.</li>
 *     <li>Persistence of user data into a JSON file.</li>
 * </ul>
 *
 * @author Ezhil
 */
public class UserService {

    /** Logger instance for logging signup and login events. */
    private static final Logger log = LogManager.getLogger(UserService.class);

    /** ObjectMapper for handling JSON read/write of user data. */
    static final ObjectMapper mapper = new ObjectMapper();

    /** Currently logged-in user (singleton session state). */
    private static  User loggedInUser;

    /** Scanner for reading input from the console. */
    private final Scanner sc;

    /**
     * Constructor to initialize UserService with a console input scanner.
     *
     * @param sc Scanner instance for reading user input.
     */
    public UserService(Scanner sc) {
        this.sc = sc;
    }

    // ======================= SIGNUP =======================

    /**
     * Handles the user signup process.
     * <p>
     * Steps performed:
     * <ul>
     *     <li>Collects username and password from the console.</li>
     *     <li>Validates input (username uniqueness, password strength, confirmation).</li>
     *     <li>Hashes the password securely using SHA-256.</li>
     *     <li>Saves the new user to the JSON file.</li>
     * </ul>
     *
     * Logs each step for auditing and debugging. Displays user-friendly
     * messages in case of errors or validation failures.
     */
    public void signup() {
        log.info("Signup process initiated.");

        System.out.println("\n===== 📝 Signup Module =====\n");

        boolean exitLoop = false;

        while (!exitLoop) {
            try {
                System.out.print("👤 Enter username : ");
                String username = sc.next();

                System.out.print("🔑 Enter password : ");
                String password = sc.next();

                System.out.print("✅ Confirm password : ");
                String confirmPassword = sc.next();

                List<User> users = loadUsers();
                log.debug("Loaded {} existing users from file.", users.size());

                Validator.validateUsername(username, users);
                Validator.validatePassword(password);
                Validator.validateConfirmPassword(password, confirmPassword);
                log.info("User input validated successfully for username '{}'.", username);

                String hashedPassword = hashPassword(password);
                log.debug("Password hashed for user '{}'.", username);

                User user = User.builder()
                        .setUsername(username)
                        .setPassword(hashedPassword)
                        .setCreatedDate(LocalDate.now().toString())
                        .build();

                users.add(user);
                saveUsers(users);
                log.info("User '{}' saved successfully.", username);

                System.out.println("\n🎉 Signup successful! Your account has been created. Please login to continue 🚀");
                exitLoop = true;  // instead of break

            } catch (IOException e) {
                log.error("Error saving user: {}", e.getMessage(), e);
                System.out.println("❌ Error saving user: " + e.getMessage());
                exitLoop = true;  // instead of break

            } catch (IllegalArgumentException | EmptyInputException e) {
                log.warn("Validation failed during signup: {}", e.getMessage());
                System.err.println(e.getMessage());
                System.out.println("🔄 Please try again...\n");

            }
        }

    }

    // ======================= LOGIN =======================

    /**
     * Handles the user login process.
     * <p>
     * Steps performed:
     * <ul>
     *     <li>Collects username and password from the console.</li>
     *     <li>Hashes the entered password using SHA-256.</li>
     *     <li>Validates credentials against stored users in the JSON file.</li>
     * </ul>
     *
     * @return true if login is successful, false if a critical error occurs.
     */
    public boolean login() {
        log.info("Login process initiated.");

        System.out.println("\n===== 📝 Login Module =====\n");

        while (true) {
            try {
                System.out.print("Enter username : ");
                String username = sc.next();
                System.out.print("Enter password : ");
                String password = sc.next();

                List<User> users = loadUsers();
                log.debug("Loaded {} users from file for login.", users.size());

                String hashedPassword = hashPassword(password);
                loggedInUser = users.stream()
                        .filter(u -> u.getUsername().equalsIgnoreCase(username) &&
                                u.getPassword().equals(hashedPassword))
                        .findFirst()
                        .orElseThrow(() -> new InvalidCredentialsException("⚠️ Invalid username or password!"));

                log.info("User '{}' logged in successfully.", loggedInUser.getUsername());
                System.out.println("\n🚀 Welcome aboard, " + loggedInUser.getUsername() + "! You’re now logged in ✅");
                return true;

            } catch (InvalidCredentialsException e) {
                log.warn("Login failed: {}", e.getMessage());
                System.out.println(e.getMessage());
                System.out.println("🔄 Please try again...\n");
            } catch (IOException e) {
                log.error("Error reading users: {}", e.getMessage(), e);
                System.out.println("❌ Error reading users: " + e.getMessage());
                return false;
            }
        }
    }

    /**
     * Loads users from the JSON file.
     *
     * @return List of {@link User} objects loaded from the file.
     *         Returns an empty list if no users are found.
     * @throws IOException if an error occurs while reading the JSON file.
     */
    private List<User> loadUsers() throws IOException {
        File file = new File(AppConstants.USERS_JSONPATH);
        if (file.exists() && file.length() != 0) {
            List<User> users = mapper.readValue(file, new TypeReference<List<User>>() {});
            log.debug("Loaded {} users from file '{}'.", users.size(), AppConstants.USERS_JSONPATH);
            return users;
        }
        log.info("No users found in file '{}'. Returning empty list.", AppConstants.USERS_JSONPATH);
        return new ArrayList<>();
    }


    /**
     * Saves a list of users into the JSON file.
     *
     * @param users List of {@link User} objects to be saved.
     * @throws IOException if an error occurs while writing to the JSON file.
     */
    private void saveUsers(List<User> users) throws IOException {
        File file = new File(AppConstants.USERS_JSONPATH);
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, users);
        log.debug("Saved {} users to file '{}'.", users.size(), AppConstants.USERS_JSONPATH);
    }


    /**
     * Hashes a password securely using the SHA-256 algorithm.
     *
     * @param password Plain text password.
     * @return Hashed password in hexadecimal string format.
     * @throws RuntimeException if SHA-256 algorithm is not available.
     */
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) sb.append(String.format("%02x", b));
            log.debug("Password hashed successfully.");
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("Password hashing failed: {}", e.getMessage(), e);
            throw new IllegalStateException("Password hashing failed due to missing algorithm.", e);
        }
    }


    /**
     * Returns the currently logged-in user.
     *
     * @return The logged-in {@link User}, or {@code null} if no user is logged in.
     */
    public static User getLoggedInUser() {
        return loggedInUser;
    }
}


