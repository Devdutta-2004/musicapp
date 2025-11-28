package com.music_app.controller;

import com.music_app.model.LikeEntity;
import com.music_app.model.Song;
import com.music_app.model.User;
import com.music_app.repository.LikeRepository;
import com.music_app.repository.SongRepository;
import com.music_app.repository.UserRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/likes")
public class LikeController {

    private final LikeRepository likeRepo;
    private final UserRepository userRepo;
    private final SongRepository songRepo;

    public LikeController(LikeRepository likeRepo, UserRepository userRepo, SongRepository songRepo) {
        this.likeRepo = likeRepo;
        this.userRepo = userRepo;
        this.songRepo = songRepo;
    }

    // FIX USER FOR NOW — ALWAYS USER 1
    private Long getUserId() {
        return 1L;
    }

    @PostMapping("/{songId}")
    public ResponseEntity<?> likeSong(@PathVariable Long songId) {
        Long userId = getUserId();

        if (likeRepo.findByUserIdAndSongId(userId, songId).isPresent()) {
            return ResponseEntity.ok("Already liked");
        }

        LikeEntity like = new LikeEntity();
        like.setUserId(userId);
        like.setSongId(songId);
        like.setCreatedAt(Instant.now());

        likeRepo.save(like);
        return ResponseEntity.ok("Liked");
    }

    @DeleteMapping("/{songId}")
    public ResponseEntity<?> unlikeSong(@PathVariable Long songId) {
        Long userId = getUserId();

        Optional<LikeEntity> like = likeRepo.findByUserIdAndSongId(userId, songId);
        like.ifPresent(likeRepo::delete);

        return ResponseEntity.ok("Unliked");
    }

    @GetMapping("/{songId}")
    public int getLikeCount(@PathVariable Long songId) {
        return likeRepo.countBySongId(songId);
    }

    @GetMapping("/user")
    public List<Long> getLikedSongs() {
        Long userId = getUserId();
        return likeRepo.findByUserId(userId)
                .stream()
                .map(LikeEntity::getSongId)
                .toList();
    }
}
