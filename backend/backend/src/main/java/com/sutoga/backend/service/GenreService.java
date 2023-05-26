package com.sutoga.backend.service;

import com.sutoga.backend.entity.Genre;

import java.util.List;
import java.util.Optional;

public interface GenreService {

    Genre createGenre(Genre genre);

    Optional<Genre> getGenreById(Long genreId);

    List<Genre> getAllGenres();

    void deleteGenre(Long genreId);


}
