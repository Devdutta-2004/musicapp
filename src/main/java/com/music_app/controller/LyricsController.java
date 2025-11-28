package com.music_app.controller;

import com.music_app.model.Lyrics;
import com.music_app.repository.LyricsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/lyrics")
@CrossOrigin(origins = "*")
public class LyricsController {

    @Autowired
    private LyricsRepository lyricsRepository;

    // GET /api/lyrics?songId=123
    @GetMapping
    public ResponseEntity<Map<String, Object>> getLyrics(@RequestParam Long songId) {
        Optional<Lyrics> entry = lyricsRepository.findById(songId);

        Map<String, Object> response = new HashMap<>();
        response.put("songId", songId);
        response.put("entry", entry.orElse(null));
        return ResponseEntity.ok(response);
    }

    // POST /api/lyrics  body: { songId, lyrics, source? }
    @PostMapping
    public ResponseEntity<Map<String, Object>> saveLyrics(@RequestBody Map<String, Object> body) {
        try {
            Object sid = body.get("songId");
            if (sid == null) return ResponseEntity.badRequest().body(Map.of("error", "songId required"));
            Long songId = Long.parseLong(String.valueOf(sid));

            String lyricsText = body.get("lyrics") != null ? String.valueOf(body.get("lyrics")) : "";
            String source = body.getOrDefault("source", "manual") != null ? String.valueOf(body.getOrDefault("source", "manual")) : "manual";

            Optional<Lyrics> existing = lyricsRepository.findById(songId);
            Lyrics lyrics;
            if (existing.isPresent()) {
                // update existing
                lyrics = existing.get();
                lyrics.setLyrics(lyricsText);
                lyrics.setSource(source);
            } else {
                // create new
                lyrics = new Lyrics(songId, lyricsText, source);
            }
            Lyrics saved = lyricsRepository.save(lyrics);

            Map<String, Object> res = new HashMap<>();
            res.put("ok", true);
            res.put("entry", saved);
            return ResponseEntity.ok(res);
        } catch (NumberFormatException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", "invalid songId"));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "internal"));
        }
    }

    // DELETE /api/lyrics?songId=123
    @DeleteMapping
    public ResponseEntity<Map<String, Object>> deleteLyrics(@RequestParam Long songId) {
        try {
            if (lyricsRepository.existsById(songId)) {
                lyricsRepository.deleteById(songId);
            }
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "internal"));
        }
    }
}
