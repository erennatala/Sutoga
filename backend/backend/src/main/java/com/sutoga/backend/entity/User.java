package com.sutoga.backend.entity;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import lombok.Data;

@Entity
@Table(name="user")
@Data
@Getter
@Setter
public class User {
    @jakarta.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    private String firstName;

    private String lastName;

    private String username;

    private String email;

    private String password;

    private String phoneNumber;

    private String gender;

    private LocalDate birthDate;

    @ElementCollection
    private List<Long> friends;

}