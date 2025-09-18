package com.cmrl.utils;

import com.cmrl.model.Ticket;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import static com.cmrl.constants.AppConstants.BASE_PATH;
import static com.cmrl.constants.AppConstants.EXCELREPORTS_PATH;


/**
 * Utility class responsible for exporting ticket booking details
 * from a JSON file into an Excel (.xlsx) report.
 * <p>
 * This class leverages:
 * <ul>
 *     <li>Jackson {@link ObjectMapper} for reading ticket data from JSON</li>
 *     <li>Apache POI for generating Excel workbooks</li>
 *     <li>Log4j2 for logging</li>
 * </ul>
 * </p>
 *
 * <p>
 * The generated Excel report contains the following columns:
 * <b>Ticket IDs, Type, From, To, No. of Tickets, Stops,
 * Fare, Validity (mins), Booking Date, QR Code,
 * Status, Cancel Date, Booked By</b>
 * </p>
 *
 * <p><b>Output file name format:</b>
 * <code>Tickets_Report_YYYY-MM-DD.xlsx</code></p>
 *
 * @author Ezhil
 */
public class ReportExporter {

    private ReportExporter() {}

    /** Logger instance for logging export progress and errors. */
    private static final Logger log = LogManager.getLogger(ReportExporter.class);

    /** Jackson object mapper for reading tickets from JSON. */
    private static final ObjectMapper mapper = new ObjectMapper();

    /** Headers used for the Excel sheet columns. */
    private static final String[] HEADERS = {
            "Ticket IDs", "Type", "From", "To", "No. of Tickets",
            "Stops", "Fare", "Validity (mins)", "Booking Date",
            "QR Code", "Status", "Cancel Date", "Booked By"
    };


    /**
     * Exports tickets from the given JSON file into an Excel report.
     * <p>
     * Steps performed:
     * <ol>
     *     <li>Read ticket data from JSON using Jackson</li>
     *     <li>Create a new Excel workbook and sheet</li>
     *     <li>Add a styled header row</li>
     *     <li>Populate ticket data rows</li>
     *     <li>Auto-size all columns</li>
     *     <li>Write workbook to disk</li>
     * </ol>
     * </p>
     *
     * @param jsonPath the file path of the JSON file containing tickets
     * @throws IOException if the JSON file cannot be read or the Excel file cannot be written
     */
    public static void exportTicketsToExcel(String jsonPath) throws IOException {

        String excelPath = BASE_PATH + File.separator + EXCELREPORTS_PATH + File.separator
                + "Tickets_Report_" + LocalDate.now().toString() + ".xlsx";

        try {
            List<Ticket> tickets = mapper.readValue(new File(jsonPath), new TypeReference<List<Ticket>>() {});
            log.info("Loaded {} tickets from JSON: {}", tickets.size(), jsonPath);

            try (Workbook workbook = new XSSFWorkbook();
                 FileOutputStream fileOut = new FileOutputStream(excelPath)) {

                Sheet sheet = workbook.createSheet("Tickets");

                // 🔹 Header Style
                CellStyle headerStyle = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                headerStyle.setFont(font);

                // 🔹 Create Header Row
                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < HEADERS.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(HEADERS[i]);
                    cell.setCellStyle(headerStyle);
                }
                log.debug("Excel header row created successfully.");

                // 🔹 Data Rows
                int rowNum = 1;
                for (Ticket t : tickets) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(String.join(",", t.getTicketId()));
                    row.createCell(1).setCellValue(t.getTicketType());
                    row.createCell(2).setCellValue(t.getFromStation());
                    row.createCell(3).setCellValue(t.getToStation());
                    row.createCell(4).setCellValue(t.getNoofTickets());
                    row.createCell(5).setCellValue(t.getStops());
                    row.createCell(6).setCellValue(t.getFare());
                    row.createCell(7).setCellValue(t.getValidityMinutes());
                    row.createCell(8).setCellValue(t.getBookingDate());
                    row.createCell(9).setCellValue(t.getQrCode());
                    row.createCell(10).setCellValue(t.getStatus());
                    row.createCell(11).setCellValue(t.getCancelDate() != null ? t.getCancelDate() : "-");
                    row.createCell(12).setCellValue(t.getBookedBy());
                }
                log.info("Excel data rows created successfully for {} tickets.", tickets.size());

                // 🔹 Auto-size columns
                for (int i = 0; i < HEADERS.length; i++) {
                    sheet.autoSizeColumn(i);
                }
                log.debug("Excel columns auto-sized.");

                workbook.write(fileOut);
            }

            log.info("Tickets exported to Excel successfully: {}", excelPath);
            System.out.println("✅ Tickets exported to Excel successfully: " + excelPath);

        } catch (Exception e) {
            log.error("Error exporting tickets to Excel from JSON {}: {}", jsonPath, e.getMessage(), e);
            System.err.println("⚠️ Error exporting to Excel: " + e.getMessage());
            throw e;
        }
    }
}


