package com.music_app.repository;

import com.music_app.model.Lyrics;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LyricsRepository extends JpaRepository<Lyrics, Long> {
    // Basic CRUD: findById, save, deleteById etc.
}
