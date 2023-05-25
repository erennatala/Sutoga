package com.sutoga.backend.entity.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
public class UpdateRequest {

    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private String birthDate;
    private String phoneNumber;
    private String description;
    private MultipartFile media;

    public LocalDate getBirthDate() {
        String[] dateParts = birthDate.split(",");
        int year = Integer.parseInt(dateParts[0]);
        int month = Integer.parseInt(dateParts[1]);
        int day = Integer.parseInt(dateParts[2]);
        return LocalDate.of(year, month, day);
    }

}

