package com.sutoga.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
@Entity
@Table(name="game")
@Data
@Getter
@Setter
public class Game {

}
