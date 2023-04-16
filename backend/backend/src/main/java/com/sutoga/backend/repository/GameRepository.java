package com.sutoga.backend.repository;

import com.sutoga.backend.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GameRepository extends JpaRepository<Game, Long> {

    @Query("SELECT g FROM Game g JOIN g.genres genre WHERE genre.name = :genreName")
    List<Game> findByGenreName(String genreName);

    @Query("SELECT g FROM Game g JOIN g.categories category WHERE category.name = :categoryName")
    List<Game> findByCategoryName(String categoryName);

    @Query("SELECT g FROM Game g JOIN g.genres genre JOIN g.categories category WHERE genre.name = :genreName AND category.name = :categoryName")
    List<Game> findByGenreNameAndCategoryName(String genreName, String categoryName);
}
