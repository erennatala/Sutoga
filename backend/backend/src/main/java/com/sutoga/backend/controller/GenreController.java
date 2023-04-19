package com.sutoga.backend.controller;


import com.sutoga.backend.entity.Category;
import com.sutoga.backend.entity.Genre;
import com.sutoga.backend.service.CategoryService;
import com.sutoga.backend.service.GenreService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/genre")
@CrossOrigin
@RequiredArgsConstructor
public class GenreController {

    private final GenreService genreService;


    @PostMapping
    public Genre createGenre(@RequestBody Genre genre) {
        return genreService.createGenre(genre);
    }

    @GetMapping("/{genreId}")
    public Genre getGenreById(@PathVariable Long genreId) {
        return genreService.getGenreById(genreId).orElse(null);
    }

    @GetMapping
    public List<Genre> getAllGenres() {
        return genreService.getAllGenres();
    }

    @DeleteMapping("/{genreId}")
    public void deleteGenre(@PathVariable Long genreId){
        genreService.deleteGenre(genreId);
    }

}
