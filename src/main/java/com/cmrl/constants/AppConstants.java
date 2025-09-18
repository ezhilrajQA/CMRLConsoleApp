package com.cmrl.constants;

import lombok.Getter;

public class AppConstants {

    private AppConstants(){}

    private static final String PROJECT_PATH = System.getProperty("user.dir");
    public static final  String STATIONJSON_PATH =PROJECT_PATH +"/src/main/resources/Database/stations.json";
    public static final  String FARERULEJSON_PATH =PROJECT_PATH+"/src/main/resources/Database/fareRules.json";
    public static final  String USERS_JSONPATH = PROJECT_PATH+"/src/main/resources/Database/users.json";
    public static final  String TICKETS_JSONPATH = PROJECT_PATH+"/src/main/resources/Database/tickets.json";
    public static final String BASE_PATH = "C:\\CMRL";
    public static final  String EXCELREPORTS_PATH = "Reports";
    public static final  String TICKETSQR_PATH = "QRTickets";

    @Getter
    private static final String ADMIN_USER = "admin";
    @Getter
    private static final String ADMIN_PASS = "admin123";
}
