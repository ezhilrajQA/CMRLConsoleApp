package com.cmrl.service;

import com.cmrl.constants.AppConstants;
import com.cmrl.exceptions.EmptyInputException;
import com.cmrl.model.Station;
import com.cmrl.model.StationsData;
import com.cmrl.model.User;
import com.cmrl.utils.ReportExporter;
import com.cmrl.utils.StationLoader;
import com.cmrl.utils.Validator;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import static com.cmrl.service.ManageStationsService.loadStationsData;
import static com.cmrl.service.ManageStationsService.saveStationsData;
import static com.cmrl.service.UserService.mapper;

/**
 * 🚨 AdminService class handles all administrative operations in the Metro Bus Booking System.
 * It provides functionalities such as Admin login, station management, ticket viewing,
 * user management, and exporting reports.
 * <p>
 * Uses Log4j2 for logging all admin activities at INFO, WARN, DEBUG, and ERROR levels.
 * Validates inputs and interacts with other service classes such as {@link TicketService}
 * and {@link Validator}.
 * </p>
 *
 * @author
 */
public class AdminService {

    /** Logger instance for logging admin operations */
    private static final Logger log = LogManager.getLogger(AdminService.class);

    /** Scanner instance for reading console inputs */
    private final Scanner sc;


    /**
     * Constructor to initialize AdminService with a Scanner object.
     *
     * @param sc Scanner instance to read user input
     */
    public AdminService(Scanner sc) {
        this.sc = sc;
    }

    /**
     * Handles admin login by accepting username and password inputs,
     * validating credentials, and redirecting to the Admin Menu if successful.
     * Logs attempts, successes, and failures.
     *
     * @return true if login is successful, false otherwise
     * @throws IOException in case of any I/O errors during the login process
     */
    public boolean adminLogin() throws IOException {
        System.out.println("\n===================================");
        System.out.println("         📝 Admin Login Portal      ");
        System.out.println("===================================\n");

        System.out.print("👨‍💼 Please enter your Admin Username : ");
        String username = sc.next().trim();
        System.out.print("🔑 Please enter your Admin Password : ");
        String password = sc.next().trim();

        log.info("Admin login attempt for username: {}", username);

        try {
            Validator.validateAdminLogin(username, password);
            log.info("Admin '{}' logged in successfully", username);

            System.out.println("\n-----------------------------------");
            System.out.println("🎉 Welcome back, " + username + "!");
            System.out.println("✅ Login Successful! You now have access to the Admin Dashboard.");
            System.out.println("-----------------------------------\n");

            adminMenu();
            return true;

        } catch (EmptyInputException e) {
            log.warn("Admin login failed for '{}': {}", username, e.getMessage());
            System.out.println("\n⚠️  Login Failed: " + e.getMessage());
            System.out.println("👉 Please try again with valid credentials.\n");
            return false;
        }
    }



    /**
     * Displays the Admin Dashboard menu with options to manage stations,
     * view tickets, manage users, export reports, and logout.
     * Handles user selections and performs appropriate actions.
     *
     * @throws IOException if an I/O error occurs during menu operations
     */
    public void adminMenu() throws IOException {
        boolean back = false;
        log.info("Entered Admin Dashboard menu");

        while (!back) {
            System.out.println("\n===================================");
            System.out.println("        🛠️  Admin Dashboard");
            System.out.println("===================================");
            System.out.println("1️⃣  Manage Stations");
            System.out.println("2️⃣  View Booked Tickets");
            System.out.println("3️⃣  View Cancelled Tickets");
            System.out.println("4️⃣  View Registered Users");
            System.out.println("5️⃣  Export Ticket Report (Excel)");
            System.out.println("6️⃣  🚪 Logout");
            System.out.println("===================================\n");

            int choice = getIntInput("👉 Please select an option (1-6): ");

            switch (choice) {
                case 1 -> {
                    log.info("Navigating to Manage Stations");
                    System.out.println("\n🔧 Redirecting to *Manage Stations*...");
                    manageStations();
                }
                case 2 -> {
                    log.info("Viewing booked tickets");
                    System.out.println("\n📄 Fetching *Booked Tickets*...");
                    viewTickets("BOOKED");
                }
                case 3 -> {
                    log.info("Viewing cancelled tickets");
                    System.out.println("\n❌ Fetching *Cancelled Tickets*...");
                    viewTickets("CANCELLED");
                }
                case 4 -> {
                    log.info("Viewing all users");
                    System.out.println("\n👥 Displaying list of *Registered Users*...");
                    viewAllUsers();
                }
                case 5 -> {
                    log.info("Exporting ticket report to Excel");
                    System.out.println("\n📊 Exporting *Ticket Report* to Excel...");
                    ReportExporter.exportTicketsToExcel(AppConstants.TICKETS_JSONPATH);
                    System.out.println("✅ Report successfully exported!");
                }
                case 6 -> {
                    log.info("Admin logged out");
                    System.out.println("\n👋 Logging out from Admin Dashboard...");
                    back = true;
                }
                default -> {
                    log.warn("Invalid menu choice: {}", choice);
                    System.out.println("\n⚠️ Invalid choice! Please select between 1 and 6.\n");
                }
            }
        }
    }


    /**
     * Displays the Station Management menu with options to view, add, update,
     * and delete stations. Handles input validation and updates station data.
     */
    public void manageStations() {
        boolean back = false;
        log.info("Entered Station Management menu");

        while (!back) {
            System.out.println("\n===================================");
            System.out.println("       🚉 Station Management");
            System.out.println("===================================");
            System.out.println("1️⃣  View All Stations");
            System.out.println("2️⃣  ➕ Add New Station");
            System.out.println("3️⃣  ✏️  Update Existing Station");
            System.out.println("4️⃣  🗑️ Delete Station");
            System.out.println("5️⃣  🔙 Back to Admin Menu");
            System.out.println("===================================\n");

            int choice = getIntInput("👉 Please select an option (1-5): ");

            switch (choice) {
                case 1 -> {
                    log.info("Viewing all stations");
                    viewStations();
                }
                case 2 -> {
                    log.info("Adding new station");
                    addStation();
                }
                case 3 -> {
                    log.info("Updating existing station");
                    updateStation();
                }
                case 4 -> {
                    log.info("Deleting a station");
                    deleteStation();
                }
                case 5 -> {
                    log.info("Returning to Admin Menu");
                    back = true;
                }
                default -> {
                    log.warn("Invalid station menu choice: {}", choice);
                    System.out.println("\n⚠️ Invalid choice! Please select between 1 and 5.\n");
                }
            }
        }
    }


    /**
     * Displays a list of all stations in both Blue and Green lines
     * in a formatted tabular structure.
     */
    private static void viewStations() {
        StationsData data = loadStationsData();
        log.info("Viewing all stations");

        System.out.println("\n===============================================");
        System.out.println("              🚇 Blue Line Stations");
        System.out.println("===============================================");
        System.out.printf("%-5s | %-25s | %-10s | %-10s%n", "ID", "Station Name", "Parking", "Feeder");
        System.out.println("---------------------------------------------------------------");

        data.getBlueLineStations().forEach(s -> System.out.printf("%-5s | %-25s | %-10s | %-10s%n",
                s.getId(),
                s.getName(),
                (s.isHasParking() ? "✅ Yes" : "❌ No"),
                (s.isHasFeeder() ? "✅ Yes" : "❌ No")));

        System.out.println("\n===============================================");
        System.out.println("              🚇 Green Line Stations");
        System.out.println("===============================================");
        System.out.printf("%-5s | %-25s | %-10s | %-10s%n", "ID", "Station Name", "Parking", "Feeder");
        System.out.println("---------------------------------------------------------------");

        data.getGreenLineStations().forEach(s -> System.out.printf("%-5s | %-25s | %-10s | %-10s%n",
                s.getId(),
                s.getName(),
                (s.isHasParking() ? "✅ Yes" : "❌ No"),
                (s.isHasFeeder() ? "✅ Yes" : "❌ No")));

        log.info("Total stations displayed successfully");
        System.out.println("\n✅ Total Stations Displayed Successfully!");
    }


    /**
     * Adds a new station to the system after validating input, ensuring no duplicates,
     * and assigning a unique ID.
     */
    private void addStation() {
        StationsData data = loadStationsData();

        System.out.println("\n===================================");
        System.out.println("       ➕ Add New Station");
        System.out.println("===================================");

        System.out.print("🚉 Enter Line (Blue/Green): ");
        String line = sc.next().trim();
        System.out.print("🏷️  Enter Station Name: ");
        String name = sc.next().trim();

        log.info("Adding new station '{}' on line '{}'", name, line);

        try {
            Validator.validateDuplicateStation(name, StationLoader.getInstance().getStations());
            Validator.validateStationLine(line);
            Validator.validateStationName(name);
        } catch (EmptyInputException e) {
            log.warn("Failed to add station: {}", e.getMessage());
            System.out.println("\n⚠️  Validation Failed: " + e.getMessage());
            return;
        }

        boolean hasParking = getBooleanInput("🅿️  Does this station have Parking? (true/false): ");
        boolean hasFeeder = getBooleanInput("🚌 Does this station have a Feeder service? (true/false): ");

        String prefix = line.equalsIgnoreCase("Blue") ? "B" : "G";
        int nextId = getNextStationId(data, prefix);

        Station station = Station.builder()
                .setId(prefix + nextId)
                .setName(name)
                .setLine(line)
                .setHasParking(hasParking)
                .setHasFeeder(hasFeeder)
                .build();

        if (prefix.equals("B")) data.getBlueLineStations().add(station);
        else data.getGreenLineStations().add(station);

        saveStationsData(data);

        log.info("Station '{}' added successfully with ID: {}", station.getName(), station.getId());
        System.out.println("\n✅ Station Successfully Added!");
    }


    /**
     * Generates the next station ID for a new station based on the line prefix (B/G)
     *
     * @param data  StationsData object containing all stations
     * @param prefix Line prefix, either "B" for Blue or "G" for Green
     * @return the next unique integer ID for the station
     */
    private static int getNextStationId(StationsData data, String prefix) {
        List<Station> targetList = prefix.equals("B") ? data.getBlueLineStations() : data.getGreenLineStations();
        return targetList.stream()
                .mapToInt(s -> Integer.parseInt(s.getId().substring(1)))
                .max()
                .orElse(0) + 1;
    }


    /**
     * Updates an existing station's details such as name, parking availability, and feeder service.
     * Validates inputs and ensures no duplicate station names.
     */
    private void updateStation() {
        StationsData data = loadStationsData();

        System.out.println("\n===================================");
        System.out.println("       ✏️ Update Station Details");
        System.out.println("===================================");

        System.out.print("🔎 Enter Station ID to update (e.g., B1 / G1): ");
        String id = sc.next().trim();

        log.info("Updating station with ID: {}", id);

        try {
            Validator.validateStationId(id);
        } catch (EmptyInputException e) {
            log.warn("Invalid Station ID '{}': {}", id, e.getMessage());
            System.out.println("\n⚠️ Invalid Input: " + e.getMessage());
            return;
        }

        List<Station> targetList = id.startsWith("B") ? data.getBlueLineStations() : data.getGreenLineStations();

        Station station = targetList.stream()
                .filter(s -> s.getId().equalsIgnoreCase(id))
                .findFirst()
                .orElse(null);

        if (station == null) {
            log.warn("Station with ID '{}' not found", id);
            System.out.println("\n❌ Station with ID " + id + " not found!");
            return;
        }

        System.out.println("\n📋 Current Station Details:");
        System.out.println("📌 ID: " + station.getId());
        System.out.println("🏷️  Name: " + station.getName());

        System.out.print("✏️ Enter new Station Name : ");
        String newName = sc.next().trim();

        if (!newName.isBlank()) {
            try {
                Validator.validateDuplicateStation(newName, StationLoader.getInstance().getStations());
                station.setName(newName);
            } catch (EmptyInputException e) {
                log.warn("Failed to update station name for '{}': {}", id, e.getMessage());
                System.out.println("\n⚠️ Validation Failed: " + e.getMessage());
                return;
            }
        }

        String parkingInput = getOptionalInput("🅿️  Update Parking? (true/false, leave blank to skip): ");
        if (!parkingInput.isBlank()) station.setHasParking(Boolean.parseBoolean(parkingInput));

        String feederInput = getOptionalInput("🚌 Update Feeder? (true/false, leave blank to skip): ");
        if (!feederInput.isBlank()) station.setHasFeeder(Boolean.parseBoolean(feederInput));

        saveStationsData(data);

        log.info("Station '{}' updated successfully", station.getId());
        System.out.println("\n✅ Station Updated Successfully!");
    }


    /**
     * Deletes a station from the system after confirming the deletion with the admin.
     * Validates station ID and ensures proper logging of actions.
     */
    private void deleteStation() {
        StationsData data = loadStationsData();

        System.out.println("\n===================================");
        System.out.println("       🗑️ Delete Station");
        System.out.println("===================================");

        System.out.print("🔎 Enter Station ID to delete (e.g., B1 / G1): ");
        String id = sc.next().trim();

        log.info("Deleting station with ID: {}", id);

        try {
            Validator.validateStationId(id);
        } catch (EmptyInputException e) {
            log.warn("Invalid Station ID '{}': {}", id, e.getMessage());
            System.out.println("\n⚠️ Invalid Input: " + e.getMessage());
            return;
        }

        List<Station> targetList = id.startsWith("B") ? data.getBlueLineStations() : data.getGreenLineStations();

        Station station = targetList.stream()
                .filter(s -> s.getId().equalsIgnoreCase(id))
                .findFirst()
                .orElse(null);

        if (station == null) {
            log.warn("Station with ID '{}' not found", id);
            System.out.println("\n❌ Station with ID " + id + " not found!");
            return;
        }

        System.out.print("\n⚠️ Are you sure you want to delete this station? (Y/N): ");
        String confirm = sc.next().trim();

        if (!confirm.equalsIgnoreCase("Y")) {
            log.info("Station deletion cancelled for ID '{}'", id);
            System.out.println("\n🔙 Deletion cancelled.");
            return;
        }

        boolean removed = targetList.removeIf(s -> s.getId().equalsIgnoreCase(id));

        if (removed) {
            saveStationsData(data);
            log.info("Station '{}' deleted successfully", id);
            System.out.println("\n✅ Station deleted successfully!");
        } else {
            log.error("Unexpected error: Station '{}' could not be deleted", id);
            System.out.println("\n⚠️ Unexpected error: Station could not be deleted.");
        }
    }


    /**
     * Views tickets based on their status (BOOKED/CANCELLED)
     * by delegating the action to {@link TicketService}.
     *
     * @param status ticket status to filter (BOOKED/CANCELLED)
     */
    private static void viewTickets(String status) {
        log.info("Viewing tickets with status: {}", status);
        TicketService.viewTickets(status);
    }


    /**
     * Displays all registered users in a formatted tabular structure.
     * Reads user data from the system's JSON file.
     */
    private static void viewAllUsers() {
        File file = new File(AppConstants.USERS_JSONPATH);
        log.info("Fetching all registered users");

        if (!file.exists() || file.length() == 0) {
            log.info("No users found in the system");
            System.out.println("\n⚠️ No users found in the system.\n");
            return;
        }

        try {
            List<User> users = mapper.readValue(file, new TypeReference<List<User>>() {});
            log.info("Total users displayed: {}", users.size());

            // Header
            System.out.println("\n==============================================");
            System.out.println(" 📋 Registered Users (" + users.size() + ")");
            System.out.println("==============================================");
            System.out.printf(" %-5s | %-20s | %-15s%n", "ID", "👤 Username", "📅 Created On");
            System.out.println("------------------------------------------------");

            // Rows
            int id = 1;
            for (User u : users) {
                System.out.printf(" %-5d | %-20s | %-15s%n", id++, u.getUsername(), u.getCreatedDate());
            }

            System.out.println("==============================================");
            System.out.println("✅ User list displayed successfully!\n");

        } catch (IOException e) {
            log.error("Error reading users: {}", e.getMessage(), e);
            System.out.println("\n❌ Error reading users: " + e.getMessage());
        }
    }



    /**
     * Prints the details of a single user in a formatted manner.
     *
     * @param u User object to display
     */
    private static void printUser(User u) {
        System.out.printf("%-20s | %-20s%n",
                u.getUsername(),
                (u.getCreatedDate() != null ? u.getCreatedDate() : "N/A"));
    }



    /**
     * Reads and returns an integer input from the admin, ensuring valid integer entry.
     *
     * @param msg message prompt to display to the user
     * @return valid integer input
     */
    private int getIntInput(String msg) {
        System.out.print(msg);
        while (!sc.hasNextInt()) {
            log.warn("Invalid integer input entered");
            System.out.println("⚠️ Please enter a valid number!");
            sc.next();
        }
        int input = sc.nextInt();
        log.debug("User entered int input: {}", input);
        return input;
    }


    /**
     * Reads and returns a boolean input from the admin (true/false).
     *
     * @param msg message prompt to display to the user
     * @return boolean value entered by user
     */
    private boolean getBooleanInput(String msg) {
        System.out.print(msg);
        boolean input = Boolean.parseBoolean(sc.next().trim());
        log.debug("User entered boolean input: {}", input);
        return input;
    }


    /**
     * Reads an optional string input from the admin. Can be blank to skip updates.
     *
     * @param msg message prompt to display to the user
     * @return user input string
     */
    private String getOptionalInput(String msg) {
        System.out.print(msg);
        String input = sc.next().trim();
        log.debug("User entered optional input: {}", input);
        return input;
    }
}


