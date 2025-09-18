package com.cmrl.model;
import lombok.*;

import java.util.List;

@Data
@Builder(setterPrefix = "set")
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    private List<String> ticketId;
    private String ticketType;
    private String fromStation;
    private String toStation;
    private int noofTickets;
    private int stops;
    private int fare;
    private int validityMinutes;
    private String bookingDate;
    private String qrCode;
    private String status;
    private String cancelDate;
    private String bookedBy;

}

