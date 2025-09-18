import com.cmrl.exceptions.StationLoadException;
import com.cmrl.service.AdminService;
import com.cmrl.service.TicketService;
import com.cmrl.service.TravelPlannerService;
import com.cmrl.service.UserService;
import com.cmrl.utils.MenuPrinter;
import com.cmrl.utils.StationLoader;
import com.google.zxing.WriterException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.util.Scanner;
import static com.cmrl.utils.HelperUtils.createDirectories;

/**
 * <b>Main Application Class</b> 🚇
 * <p>
 * Entry point for the Chennai Metro System application.
 * This class initializes the application, loads resources,
 * and provides the main navigation menus for:
 * <ul>
 *   <li>👤 User signup and login</li>
 *   <li>👨‍💼 Admin login</li>
 *   <li>🗺️ Travel planning</li>
 *   <li>🎟️ Ticket booking and management</li>
 * </ul>
 *
 * <p><b>Responsibilities:</b></p>
 * <ul>
 *     <li>Handles startup initialization (station data loading, directory creation).</li>
 *     <li>Provides Main Menu and User Dashboard navigation.</li>
 *     <li>Delegates to service layer classes (UserService, AdminService, TravelPlannerService, TicketService).</li>
 *     <li>Manages application lifecycle (startup → menu loop → shutdown).</li>
 * </ul>
 *
 * <p><b>Thread Safety:</b> Not thread-safe, intended for single-user console execution.</p>
 */
public class Main {

    /** Global scanner for console input. */
    private static final Scanner sc = new Scanner(System.in);

    /** Logger instance for application events and errors. */
    private static final Logger logger = LogManager.getLogger(Main.class);

    // ✅ Service layer instances (non-static calls avoided for better design)
    private static final UserService userService = new UserService(sc);
    private static final AdminService adminService = new AdminService(sc);
    private static final TravelPlannerService travelPlannerService = new TravelPlannerService(sc);
    private static final TicketService ticketService = new TicketService(sc);

    /**
     * ✅ Static initializer block.
     * <p>
     * Executes only once when the class is loaded.
     * <ul>
     *   <li>Creates required application directories.</li>
     *   <li>Initializes and loads metro station data via {@link StationLoader}.</li>
     * </ul>
     *
     * @throws RuntimeException if station data fails to load
     */
    static {
        createDirectories();
        StationLoader loader = StationLoader.getInstance();
        try {
            loader.loadStations();
            logger.info("🚉 Station data loaded successfully at application startup.");
        } catch (IOException e) {
            logger.error("❌ Failed to load stations during startup", e);
            throw new StationLoadException("Failed to load metro stations from JSON",e);
        }
    }

    /**
     * ✅ Main entry point of the Chennai Metro System.
     * <p>
     * Displays a welcome banner, initializes menus, and
     * continuously listens for user input until exit.
     *
     * @param args command-line arguments (unused)
     * @throws IOException      if an I/O error occurs
     * @throws WriterException  if QR code generation fails
     */
    public static void main(String[] args) throws IOException, WriterException {
        try {
            // 🎉 Welcome Banner
            System.out.println("=======================================");
            System.out.println("   🚇 Welcome to Chennai Metro System 🚇");
            System.out.println("=======================================");
            System.out.println("✨ Plan your journey, book tickets, and travel with ease! ✨");

            logger.info("✅ Application started successfully.");

            boolean exit = false;

            // 🏠 Main Menu Loop
            while (!exit) {
                MenuPrinter.printMenu("🏠 Main Menu",
                        "📝 Sign Up (New User)",
                        "🔑 Login (Existing User)",
                        "👨‍💼 Admin Login",
                        "🗺️ Travel Planner",
                        "❌ Exit Application");

                int choice = getChoice();

                switch (choice) {
                    case 1 -> {
                        logger.info("🆕 User attempting to sign up.");
                        userService.signup();
                    }
                    case 2 -> {
                        logger.info("🔑 User attempting to login.");
                        handleLogin();
                    }
                    case 3 -> {
                        logger.info("Admin attempting to login.");
                        adminService.adminLogin();
                    }
                    case 4 -> {
                        logger.info("🗺️ Travel planner option selected.");
                        travelPlannerService.planJourney();
                    }
                    case 5 -> {
                        exit = true;
                        logger.info("🚪 Application exit initiated by user.");
                        System.out.println("👋 Thank you for traveling with Chennai Metro! 🚇");
                    }
                    default -> {
                        logger.warn("⚠️ Invalid option entered in Main Menu.");
                        System.out.println("⚠️ Invalid option, try again!");
                    }
                }
            }

        } catch (Exception e) {
            logger.error("❌ Unexpected application error occurred!", e);
            e.printStackTrace();
        } finally {
            sc.close();
            logger.info("🔒 Scanner resource closed. Application shutting down.");
        }
    }

    /**
     * ✅ Handles user login flow.
     * <p>
     * After successful login, displays the <b>User Dashboard</b>
     * with options for ticket booking, viewing, cancellation, and logout.
     *
     * @throws IOException     if an I/O error occurs
     * @throws WriterException if QR code generation fails during booking
     */
    private static void handleLogin() throws IOException, WriterException {
        if (!userService.login()) {
            logger.warn("⚠️ User login failed.");
            System.out.println("⚠️ Login failed! Please login to book tickets 🎟️");
            return;
        }

        boolean isLogout = false;

        // 👤 User Dashboard Loop
        while (!isLogout) {
            MenuPrinter.printMenu("👤 User Dashboard",
                    "🎟️ Book QR Ticket",
                    "📂 View My Tickets",
                    "❌ Cancel Tickets",
                    "🚪 Logout");

            int choice = getChoice();

            switch (choice) {
                case 1 -> {
                    logger.info("🎟️ User '{}' is booking a ticket.",
                            UserService.getLoggedInUser().getUsername());
                    ticketService.bookTicket();
                }
                case 2 -> {
                    logger.info("📂 User '{}' is viewing tickets.", UserService.getLoggedInUser().getUsername());
                    TicketService.viewTickets();
                }
                case 3 -> {
                    logger.info("❌ User '{}' is canceling tickets.",
                            UserService.getLoggedInUser().getUsername());
                    ticketService.cancelTicket();
                }
                case 4 -> {
                    isLogout = true;
                    logger.info("🚪 User '{}' logged out successfully.",
                            UserService.getLoggedInUser().getUsername());
                    System.out.println("🚪 You have logged out successfully, "
                            + UserService.getLoggedInUser().getUsername()
                            + "! 👋 See you on your next journey 🚇✨");
                }
                default -> {
                    logger.warn("⚠️ Invalid option entered in User Dashboard by '{}'.",
                            UserService.getLoggedInUser().getUsername());
                    System.out.println("⚠️ Invalid choice! Please enter a valid option from the menu. 🔢");
                }
            }
        }
    }

    /**
     * ✅ Reads and validates numeric input safely.
     * <p>
     * Ensures user enters a valid integer for menu selection.
     *
     * @return menu choice (valid integer) or -1 if invalid
     */
    private static int getChoice() {
        try {
            return Integer.parseInt(sc.next());
        } catch (NumberFormatException e) {
            logger.warn("⚠️ Invalid number format entered in menu selection.");
            System.out.println("⚠️ Please enter a valid number!");
            return -1; // Invalid choice
        }
    }
}


