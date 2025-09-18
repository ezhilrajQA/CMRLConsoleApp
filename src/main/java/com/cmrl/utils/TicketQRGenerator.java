package com.cmrl.utils;

import com.cmrl.model.Ticket;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import static com.cmrl.constants.AppConstants.*;

/**
 * Utility class responsible for generating QR code–based ticket images.
 * <p>
 * Features:
 * <ul>
 *     <li>Generates a QR code for the given ticket ID</li>
 *     <li>Creates a visually styled ticket image containing:
 *         <ul>
 *             <li>QR Code</li>
 *             <li>Ticket details (ID, type, stations, fare, validity, etc.)</li>
 *             <li>Booking date and time</li>
 *             <li>Footer disclaimer notes</li>
 *         </ul>
 *     </li>
 *     <li>Saves the final ticket image as a JPEG file in the configured directory</li>
 *     <li>Uses ZXing for QR generation and Java AWT for image rendering</li>
 * </ul>
 *
 * Example usage:
 * <pre>
 *     Ticket ticket = new Ticket(...);
 *     TicketQRGenerator.generateTicketQR(ticket, "ABC12345");
 * </pre>
 *
 * Dependencies:
 * <ul>
 *     <li>{@link Ticket} - POJO holding ticket data</li>
 *     <li>{@code BASE_PATH}, {@code TICKETSQR_PATH} - constants for storage paths</li>
 *     <li>ZXing library for QR code generation</li>
 * </ul>
 *
 * Thread safety: This is a stateless utility class; all methods are static and thread-safe.
 *
 * @author Ezhil
 */
public class TicketQRGenerator {

    private TicketQRGenerator(){}

    /** Logger for structured logging of QR code and ticket generation process. */
    private static final Logger log = LogManager.getLogger(TicketQRGenerator.class);


    /**
     * Generates a QR code–based ticket image and saves it as a JPEG file.
     * <p>
     * Workflow:
     * <ol>
     *     <li>Generates a QR code for the given ticket ID</li>
     *     <li>Creates a ticket image canvas (400x550 px) with a white background</li>
     *     <li>Draws the title, QR code, ticket details, and footer notes</li>
     *     <li>Saves the ticket image in {@code BASE_PATH/TICKETSQR_PATH}</li>
     * </ol>
     *
     * @param ticket   The {@link Ticket} object containing ticket details (type, stations, fare, etc.)
     * @param ticketId Unique identifier for the ticket (used for QR code and filename)
     * @throws WriterException if QR code encoding fails
     * @throws IOException     if saving the ticket image to disk fails
     */
    public static void generateTicketQR(Ticket ticket, String ticketId) throws WriterException, IOException {
        log.info("Generating QR for Ticket ID: {}", ticketId);

        int qrSize = 250; // QR code size
        int width = 400;
        int height = 550;

        // 1. Generate QR Code
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(ticketId, BarcodeFormat.QR_CODE, qrSize, qrSize);
        BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
        log.debug("QR Code generated for Ticket ID: {}", ticketId);

        // 2. Create ticket image
        BufferedImage ticketImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = ticketImage.createGraphics();

        // White background
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        // Title text
        g.setColor(Color.BLUE);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        FontMetrics fm = g.getFontMetrics();
        String title = "CMRL CLONE TICKET";
        int titleX = (width - fm.stringWidth(title)) / 2;
        g.drawString(title, titleX, 30);

        // 3. Draw QR Code (centered)
        int qrX = (width - qrSize) / 2;
        int qrY = 50;
        g.drawImage(qrImage, qrX, qrY, null);

        // 4. Ticket details (aligned)
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        FontMetrics fmLabel = g.getFontMetrics();
        FontMetrics fmValue = g.getFontMetrics();
        int labelX = 40;
        int valueX = 150;
        int y = qrY + qrSize + 40;

        List<String[]> rows = Arrays.asList(
                new String[]{"Ticket ID", ticketId},
                new String[]{"Type", ticket.getTicketType()},
                new String[]{"From", ticket.getFromStation()},
                new String[]{"To", ticket.getToStation()},
                new String[]{"Tickets", String.valueOf(ticket.getNoofTickets())},
                new String[]{"Fare", "₹" + ticket.getFare()},
                new String[]{"Booked At", ticket.getBookingDate() + " " + getCurrentTime()},
                new String[]{"Validity", ticket.getValidityMinutes() + " mins"}
        );

        int lineHeight = Math.max(fmLabel.getHeight(), fmValue.getHeight());

        for (String[] kv : rows) {
            String label = kv[0] + " :";
            String value = kv[1] == null ? "-" : kv[1];
            g.setColor(Color.BLACK);
            g.drawString(label, labelX, y);
            g.drawString(value, valueX, y);
            y += lineHeight + 5;
        }

        // 5. Footer note
        g.setFont(new Font("Arial", Font.ITALIC, 10));
        g.setColor(Color.DARK_GRAY);
        g.drawString("Note: Ticket validity starts from booking time.", 40, height - 30);
        g.drawString("This is a system generated ticket - No signature required.", 40, height - 15);

        g.dispose();
        log.debug("Ticket image created for Ticket ID: {}", ticketId);

        // 6. Save as JPG
        String fileName = "Ticket_" + ticketId + ".jpg";
        String excelPath = BASE_PATH + File.separator + TICKETSQR_PATH + File.separator + fileName;

        File outputFile = new File(excelPath);
        ImageIO.write(ticketImage, "jpg", outputFile);

        log.info("✅ Ticket QR generated and saved: {}", outputFile.getAbsolutePath());
        System.out.println("✅ Ticket QR generated: " + outputFile.getAbsolutePath());
    }


    /**
     * Returns the current system time in a human-readable format.
     * <p>
     * Format: {@code hh:mm a} (e.g., {@code 10:45 AM})
     * </p>
     *
     * @return formatted current time as a {@link String}
     */
    private static String getCurrentTime() {
        return new SimpleDateFormat("hh:mm a").format(new Date());
    }
}
