package com.sutoga.backend.repository;

import com.sutoga.backend.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GenreRepository extends JpaRepository<Genre,Long> {

    Optional<Genre> findByName(String name);
}
