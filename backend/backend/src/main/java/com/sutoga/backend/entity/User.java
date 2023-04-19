package com.sutoga.backend.entity;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="user")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    private String firstName;

    private String lastName;

    private String username;

    private String email;

    private String password;

    private String phoneNumber;

    private LocalDate birthDate;

    @OneToMany
    private List<User> friends;

    private Long steamId;

    private String profileDescription;

    private String title;

    private Boolean isPrivate;
}