package com.moviebooking.controller;

import com.moviebooking.entity.Genre;
import com.moviebooking.service.GenreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/genres")
public class GenreController {
    @Autowired private GenreService genreService;

    @GetMapping
    public ResponseEntity<?> getAllGenres() {
        return ResponseEntity.ok(genreService.findAll());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Genre> createGenre(@RequestBody Genre genre) {
        return ResponseEntity.ok(genreService.save(genre));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Genre> updateGenre(@PathVariable Long id, @RequestBody Genre genre) {
        return ResponseEntity.ok(genreService.update(id, genre));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> deleteGenre(@PathVariable Long id) {
        genreService.delete(id);
        return ResponseEntity.ok("Genre deleted");
    }
}