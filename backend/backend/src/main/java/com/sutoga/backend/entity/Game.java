package com.sutoga.backend.entity;

import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name="game")
@Getter
@Setter
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    private String name;
    private String description;
    private String genre;
}
