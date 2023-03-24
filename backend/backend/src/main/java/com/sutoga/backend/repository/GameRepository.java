package com.sutoga.backend.repository;

import com.sutoga.backend.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Long> {
}
