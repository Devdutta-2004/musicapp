package com.music_app.controller;

import com.music_app.model.Lyrics;
import com.music_app.repository.LyricsRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Clean Lyrics REST controller.
 *
 * Endpoints:
 *  GET /api/lyrics?songId=123          -> returns { songId, lyrics, source }
 *  GET /api/lyrics/{songId}            -> same as above
 *  POST /api/lyrics                    -> create or update; body: { songId, lyrics, source? }
 *  DELETE /api/lyrics/{songId}         -> delete lyrics for songId
 */
@RestController
@RequestMapping("/api/lyrics")
@CrossOrigin(origins = "*")
@Validated
public class LyricsController {

    private final LyricsRepository lyricsRepository;

    public LyricsController(LyricsRepository lyricsRepository) {
        this.lyricsRepository = lyricsRepository;
    }

    // --- DTO for POST request ---
    public static class LyricsRequest {
        public Long songId;
        public String lyrics;      // text content
        public String source;      // optional metadata

        // empty constructor needed by Jackson
        public LyricsRequest() {}
    }

    // --- Helper to build stable response ---
    private Map<String, Object> buildResponse(Long songId, String lyricsText, String source) {
        Map<String, Object> res = new HashMap<>();
        res.put("songId", songId);
        res.put("lyrics", lyricsText == null ? "" : lyricsText);
        res.put("source", source);
        return res;
    }

    /**
     * GET /api/lyrics?songId=...
     * Returns a stable JSON object - lyrics always present (empty string when missing).
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getByQuery(@RequestParam Long songId) {
        try {
            Optional<Lyrics> opt = lyricsRepository.findById(songId);
            if (opt.isPresent()) {
                Lyrics l = opt.get();
                // support both possible field names (lyrics_text / lyrics) in entity getters
                String text = l.getLyrics() != null ? l.getLyrics() :
                              (l.getLyricsText() != null ? l.getLyricsText() : "");
                return ResponseEntity.ok(buildResponse(songId, text, l.getSource()));
            } else {
                // stable empty response when missing
                return ResponseEntity.ok(buildResponse(songId, "", null));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "internal", "message", ex.getMessage()));
        }
    }

    /**
     * GET /api/lyrics/{songId}
     * Same as query version.
     */
    @GetMapping("/{songId}")
    public ResponseEntity<Map<String, Object>> getByPath(@PathVariable Long songId) {
        return getByQuery(songId);
    }

    /**
     * POST /api/lyrics
     * Body: { songId, lyrics, source? }
     * Creates or updates the lyrics entry for the given songId.
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrUpdate(@RequestBody LyricsRequest req) {
        if (req == null || req.songId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "songId required"));
        }

        try {
            String incomingText = req.lyrics == null ? "" : req.lyrics;
            String source = req.source == null ? "manual" : req.source;

            Optional<Lyrics> existing = lyricsRepository.findById(req.songId);

            Lyrics saved;
            if (existing.isPresent()) {
                Lyrics e = existing.get();
                // Try both setter names to match your entity (safe if one exists)
                e.setLyrics(incomingText);
                // if your entity had lyricsText field, setter above might not exist — but earlier code used setLyrics so it's OK
                e.setSource(source);
                // optional: set updated time if your entity supports it
                saved = lyricsRepository.save(e);
            } else {
                // assume Lyrics has constructor Lyrics(Long songId, String lyrics, String source)
                Lyrics l = new Lyrics(req.songId, incomingText, source);
                saved = lyricsRepository.save(l);
            }

            // Normalize output: prefer lyrics_text field if exists; otherwise lyrics
            String text = saved.getLyrics() != null ? saved.getLyrics()
                          : (saved.getLyricsText() != null ? saved.getLyricsText() : "");

            Map<String, Object> response = new HashMap<>();
            response.put("ok", true);
            response.put("entry", buildResponse(saved.getSongId(), text, saved.getSource()));
            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "internal", "message", ex.getMessage()));
        }
    }

    /**
     * DELETE /api/lyrics/{songId}
     */
    @DeleteMapping("/{songId}")
    public ResponseEntity<Map<String, Object>> deleteByPath(@PathVariable Long songId) {
        try {
            if (lyricsRepository.existsById(songId)) {
                lyricsRepository.deleteById(songId);
            }
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "internal", "message", ex.getMessage()));
        }
    }
}
