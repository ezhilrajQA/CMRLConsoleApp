package com.cmrl.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(setterPrefix = "set")
public class Station {

    private String id;
    private String name;
    private String line;
    private boolean hasParking;
    private boolean hasFeeder;
}