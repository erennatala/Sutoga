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
    private LocalDate birthDate;
    private String phoneNumber;
    private String description;
    private MultipartFile media;
}

