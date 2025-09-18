package com.cmrl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StationsData {

    private List<Station> blueLineStations;
    private List<Station> greenLineStations;
}
