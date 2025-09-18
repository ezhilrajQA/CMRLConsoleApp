package com.cmrl.service;

import com.cmrl.constants.AppConstants;
import com.cmrl.enums.TicketStatus;
import com.cmrl.enums.TicketType;
import com.cmrl.exceptions.EmptyInputException;
import com.cmrl.model.JourneyResult;
import com.cmrl.model.Station;
import com.cmrl.model.Ticket;
import com.cmrl.model.User;
import com.cmrl.utils.FareCalculator;
import com.cmrl.utils.RouteHelper;
import com.cmrl.utils.StationLoader;
import com.cmrl.utils.Validator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.WriterException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.cmrl.utils.HelperUtils.*;
import static com.cmrl.utils.TicketQRGenerator.generateTicketQR;
import static com.cmrl.utils.TimeCalculator.getValidity;


/**
 * 🎟️ TicketService manages all ticket-related operations in the Metro Booking System.
 * <p>
 * Core features:
 * <ul>
 *   <li>📂 Load and Save tickets to JSON (persistence layer)</li>
 *   <li>🎫 Book tickets with station validation, fare calculation, and QR code generation</li>
 *   <li>👀 View all tickets or filter by status (BOOKED / CANCELLED)</li>
 *   <li>❌ Cancel booked tickets with proper logging and audit trail</li>
 * </ul>
 *
 * <p>
 * Uses <b>Jackson ObjectMapper</b> for JSON persistence and <b>Log4j2</b> for logging.
 * The class relies on supporting services like {@link Validator}, {@link StationLoader},
 * {@link RouteHelper}, and {@link UserService}.
 * </p>
 *
 * <p>🚀 Designed for SDET-level testing with clear logs, console output, and JSON persistence.</p>
 *
 * @author EzhilRaj
 */
public class TicketService {

    /** Logger instance for capturing ticket operations */
    private static final Logger log = LogManager.getLogger(TicketService.class);

    /** Thread-safe list of tickets maintained in memory */
    private static final List<Ticket> tickets = Collections.synchronizedList(new ArrayList<>());

    /** Jackson ObjectMapper for serializing and deserializing ticket data */
    private static final ObjectMapper mapper = new ObjectMapper();

    /** Scanner instance for reading user input from console */
    private final Scanner sc;

    /**
     * Constructor for injecting Scanner dependency (for testability and user input).
     *
     * @param sc Scanner instance to read console input
     */
    public TicketService(Scanner sc) {
        this.sc = sc; // dependency injection
    }


    /*
     * Static initializer to load tickets into memory at application startup.
     */
    static {
        loadTicketsFromJson(); // Load tickets on app start
    }


    // ===================== Save & Load =====================

    /**
     * Saves all tickets in memory to the JSON file defined in {@link AppConstants#TICKETS_JSONPATH}.
     * <p>Data is written in a human-readable, pretty-printed format.</p>
     *
     * Logs the total number of tickets saved. Prints error messages if saving fails.
     */
    private static void saveTicketsToJson() {
        try {
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(AppConstants.TICKETS_JSONPATH), tickets);
            log.info("Tickets saved successfully. Total tickets: {}", tickets.size());
        } catch (IOException e) {
            log.error("Error saving tickets to JSON: {}", e.getMessage(), e);
            System.out.println("⚠️ Error saving tickets: " + e.getMessage());
        }
    }


    /**
     * Loads tickets from the JSON file into memory.
     * <p>If no file exists, an empty list is initialized.</p>
     *
     * Logs success/failure and the number of tickets loaded.
     */
    private static void loadTicketsFromJson() {
        try {
            File file = new File(AppConstants.TICKETS_JSONPATH);
            if (file.exists()) {
                List<Ticket> savedTickets = mapper.readValue(file, new TypeReference<>() {});
                tickets.clear();
                tickets.addAll(savedTickets);
                log.info("Tickets loaded successfully. Total tickets: {}", tickets.size());
            } else {
                log.info("No tickets file found. Starting with an empty ticket list.");
            }
        } catch (IOException e) {
            log.error("Failed to load tickets from JSON: {}", e.getMessage(), e);
        }
    }

    // ===================== Book Ticket =====================
    /**
     * Books a new ticket for the user.
     * <p>
     * Steps performed:
     * <ol>
     *   <li>Accepts station inputs (from/to) and validates them.</li>
     *   <li>Validates ticket type (SJT, RJT, FAMILY, GROUP) and ticket count.</li>
     *   <li>Calculates journey details and fare using {@link RouteHelper} and {@link FareCalculator}.</li>
     *   <li>Generates unique ticket IDs and creates a {@link Ticket} object.</li>
     *   <li>Saves tickets to JSON and generates QR codes (if confirmed by user).</li>
     * </ol>
     *
     * Logs the entire booking lifecycle at INFO/WARN levels.
     *
     * @throws IOException if ticket persistence fails
     * @throws WriterException if QR code generation fails
     */
    public void bookTicket() throws IOException, WriterException {
        log.info("Booking process started by user.");
        System.out.println("\n====================================");
        System.out.println("   🎟️✨ Welcome to Ticket Booking ✨🎟️   ");
        System.out.println("====================================\n");

        System.out.print("🚉 Enter From Station : ");
        sc.nextLine();
        String from = sc.nextLine().trim();

        System.out.print("🚉 Enter To Station   : ");
        String to = sc.nextLine().trim();

        try {
            Validator.validateStation(from, from, to);
            Validator.validateStation(to, from, to);
        } catch (EmptyInputException e) {
            log.warn("Ticket booking validation failed: {}", e.getMessage());
            System.out.println(e.getMessage());
            return;
        }

        System.out.print("Enter Ticket Type (SJT / RJT / FAMILY / GROUP): ");
        String typeInput = sc.nextLine().trim();
        TicketType ticketType;

        try {
            ticketType = Validator.validateTicketType(typeInput);
        } catch (EmptyInputException e) {
            log.warn("Ticket type validation failed: {}", e.getMessage());
            System.out.println(e.getMessage());
            return;
        }

        int noOfTickets = getTicketCount(ticketType);

        try {
            Validator.validateTicketCount(noOfTickets, ticketType);
        } catch (EmptyInputException e) {
            log.warn("Ticket count validation failed: {}", e.getMessage());
            System.out.println(e.getMessage());
            return;
        }

        Station start = StationLoader.getInstance().getStations().stream()
                .filter(s -> s.getName().equalsIgnoreCase(from))
                .findFirst().orElse(null);
        Station end = StationLoader.getInstance().getStations().stream()
                .filter(s -> s.getName().equalsIgnoreCase(to))
                .findFirst().orElse(null);

        if (start == null || end == null) {
            log.warn("Invalid station names entered: from='{}', to='{}'", from, to);
            System.out.println("⚠️ Invalid station name, please try again!");
            return;
        }

        JourneyResult jr = RouteHelper.calculateJourney(StationLoader.getInstance().getStations(), start, end);
        if (Objects.isNull(jr)) {
            log.warn("Invalid journey between {} and {}", from, to);
            System.out.println("⚠️ Invalid journey, please try again!");
            return;
        }

        int fare = new FareCalculator().getFare(jr.getStops());
        int totalFare = (ticketType == TicketType.RJT) ? noOfTickets * fare * 2 : noOfTickets * fare;

        List<String> ticketIds = new ArrayList<>();
        for (int i = 0; i < noOfTickets; i++) ticketIds.add(generateTicketID());

        User currentUser = UserService.getLoggedInUser();
        Ticket ticket = Ticket.builder()
                .setTicketId(ticketIds)
                .setTicketType(ticketType.name())
                .setFromStation(from)
                .setToStation(to)
                .setNoofTickets(noOfTickets)
                .setStops(jr.getStops())
                .setFare(totalFare)
                .setValidityMinutes(getValidity(ticketType.name()))
                .setBookingDate(gettodayDate())
                .setQrCode("QR- will be Generated..")
                .setStatus(TicketStatus.BOOKED.name())
                .setBookedBy(currentUser != null ? currentUser.getUsername() : "GUEST")
                .setCancelDate(null)
                .build();

        System.out.print("\n❓ Confirm booking (Yes/No): ");
        String confirm = sc.nextLine();

        if (confirm.equalsIgnoreCase("Yes")) {
            tickets.add(ticket);
            saveTicketsToJson();
            for (String tid : ticketIds) generateTicketQR(ticket, tid);
            log.info("Ticket(s) booked successfully: {} by {}", ticketIds, ticket.getBookedBy());
            System.out.println("\n🎉✅ Ticket Booked Successfully! ✅🎉");
            printTicket(ticket);
        } else {
            log.info("Ticket booking cancelled by user.");
            System.out.println("\n↩️ Ticket booking cancelled by user.\n");
        }
    }



    // ===================== View Tickets =====================

    /**
     * Displays all tickets currently stored in the system.
     * <p>If no tickets exist, shows a warning message.</p>
     */
    public static void viewTickets() {
        log.info("User requested to view all tickets.");
        if (tickets.isEmpty()) {
            System.out.println("\n⚠️ No tickets booked yet!\n");
            return;
        }
        System.out.println("\n====================================");
        System.out.println("       🎫 All Tickets in System     ");
        System.out.println("====================================\n");
        tickets.forEach(TicketService::printTicket);
    }


    /**
     * Displays tickets filtered by status (BOOKED or CANCELLED).
     *
     * @param status Ticket status to filter (e.g., "BOOKED", "CANCELLED")
     */
    public static void viewTickets(String status) {
        log.info("User requested to view tickets with status: {}", status);
        if (tickets.isEmpty()) {
            System.out.println("\n⚠️ No tickets found in the system.\n");
            return;
        }
        List<Ticket> filtered = tickets.stream()
                .filter(t -> t.getStatus().equalsIgnoreCase(status))
                .toList();
        if (filtered.isEmpty()) {
            System.out.println("\n⚠️ No " + status.toLowerCase() + " tickets found.\n");
            return;
        }
        System.out.println("\n====================================");
        System.out.println("     🎫 " + status.toUpperCase() + " Tickets");
        System.out.println("====================================\n");
        filtered.forEach(TicketService::printTicket);
    }



    // ===================== Cancel Ticket =====================

    /**
     * Cancels a booked ticket selected by the user.
     * <p>
     * Workflow:
     * <ul>
     *   <li>Shows active tickets.</li>
     *   <li>User selects a ticket number to cancel.</li>
     *   <li>Updates ticket status to CANCELLED and saves to JSON.</li>
     *   <li>Logs the cancellation event.</li>
     * </ul>
     */

    public void cancelTicket() {
        log.info("User initiated ticket cancellation.");
        List<Ticket> activeTickets = tickets.stream()
                .filter(t -> t.getStatus().equals(TicketStatus.BOOKED.name()))
                .toList();

        if (activeTickets.isEmpty()) {
            System.out.println("\n⚠️ No active tickets to cancel!\n");
            return;
        }

        System.out.println("\n====================================");
        System.out.println("     ❌ Cancel Ticket Menu ❌     ");
        System.out.println("====================================\n");

        for (int i = 0; i < activeTickets.size(); i++) {
            System.out.println("#" + (i + 1));
            printTicket(activeTickets.get(i));
        }

        int choice = safeReadInt("👉 Enter booking number (#) to cancel or 0 to go back: ");

        if (choice == 0) {
            log.info("Ticket cancellation aborted by user.");
            System.out.println("\n↩️ Cancel request aborted by user.\n");
            return;
        }

        if (choice < 1 || choice > activeTickets.size()) {
            log.warn("Invalid cancellation choice entered: {}", choice);
            System.out.println("⚠️ Invalid choice! Please try again.");
            return;
        }

        Ticket cancelledTicket = activeTickets.get(choice - 1);
        cancelledTicket.setStatus(TicketStatus.CANCELLED.name());
        cancelledTicket.setCancelDate(gettodayDate() + " " + getCurrentTime());
        saveTicketsToJson();

        log.info("Ticket cancelled successfully: {} by {}", cancelledTicket.getTicketId(), cancelledTicket.getBookedBy());
        System.out.println("\n❌ Ticket Cancelled Successfully! ✅\n");
        printTicket(cancelledTicket);
    }

    // ===================== Helpers =====================

    /**
     * Reads number of tickets required from the user based on ticket type.
     *
     * @param type TicketType (SJT, RJT, FAMILY, GROUP)
     * @return Number of tickets requested by the user
     */
    private int getTicketCount(TicketType type) {
        return switch (type) {
            case SJT, RJT -> safeReadInt("How Many Tickets You want (Max 6): ");
            case FAMILY -> safeReadInt("How Many Tickets You want (Max 5): ");
            case GROUP -> safeReadInt("How Many Tickets You want (20–255): ");
        };
    }


    /**
     * Reads an integer input safely from console with validation.
     * Keeps prompting until a valid integer is entered.
     *
     * @param prompt message to display to the user
     * @return validated integer input
     */
    private int safeReadInt(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                int val = sc.nextInt();
                sc.nextLine();
                return val;
            } catch (InputMismatchException e) {
                log.warn("Invalid number input entered.");
                System.out.println("⚠️ Please enter a valid number.");
                sc.nextLine();
            }
        }
    }


    /**
     * Prints details of a single ticket in formatted output.
     *
     * @param t Ticket object to print
     */
    static void printTicket(Ticket t) {
        System.out.println("-----------------------------------");
        System.out.println("🎫 Ticket ID   : " + t.getTicketId());
        System.out.println("👤 Booked By   : " + t.getBookedBy());
        System.out.println("📍 From        : " + t.getFromStation());
        System.out.println("📍 To          : " + t.getToStation());
        System.out.println("🧾 Type        : " + t.getTicketType());
        System.out.println("🎟️ Count       : " + t.getNoofTickets());
        System.out.println("💰 Fare        : ₹" + t.getFare());
        System.out.println("📅 Booked Date : " + t.getBookingDate());
        System.out.println("⏳ Validity    : " + t.getValidityMinutes() + " mins");
        System.out.println("📌 Status      : " + t.getStatus());
        if (t.getCancelDate() != null) System.out.println("❌ Cancelled At: " + t.getCancelDate());
        System.out.println("-----------------------------------");
    }
}

