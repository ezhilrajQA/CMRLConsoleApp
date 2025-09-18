package com.cmrl.model;

import java.util.List;

public class JourneyResult {

    private final List<Station> journeyStations;
    private final int stops;
    private final String interchangeStation;

    public JourneyResult(List<Station> journeyStations, int stops, String interchangeStation) {
        this.journeyStations = journeyStations;
        this.stops = stops;
        this.interchangeStation = interchangeStation;
    }

    public List<Station> getJourneyStations() {
        return journeyStations;
    }

    public int getStops() {
        return stops;
    }

    public String getInterchangeStation() {
        return interchangeStation;
    }
}

