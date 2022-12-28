package com.sutoga.backend.repository;

import com.sutoga.backend.entity.Game;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GameRepository extends MongoRepository<Game, Long> {
}
