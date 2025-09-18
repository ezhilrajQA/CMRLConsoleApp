package com.cmrl.model;

import lombok.*;

@Builder(setterPrefix = "set")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class User {
    private String username;
    private String password;
    private String confirmPassword;
    private String createdDate;
}


