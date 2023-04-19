package com.sutoga.backend.entity.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String password;
    private String phoneNumber;
    private LocalDate birthDate;
}
