package com.sutoga.backend.entity.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateRequest {

    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String username;
    private String gender;
    private LocalDate birthDate;
    private String phoneNumber;
}
