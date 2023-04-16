package com.sutoga.backend.service.impl;

import com.sutoga.backend.entity.Genre;
import com.sutoga.backend.repository.GenreRepository;
import com.sutoga.backend.service.GenreService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class GenreServiceImpl implements GenreService {

    private final GenreRepository genreRepository;

    @Override
    public Genre createGenre(Genre genre) {
        return genreRepository.save(genre);
    }

    @Override
    public Optional<Genre> getGenreById(Long genreId) {
        return genreRepository.findById(genreId);
    }

    @Override
    public List<Genre> getAllGenres() {
        return genreRepository.findAll();
    }

    @Override
    public void deleteGenre(Long genreId) {

        genreRepository.deleteById(genreId);
    }
}
