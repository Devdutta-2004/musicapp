package com.music_app.repository;

import com.music_app.model.LikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<LikeEntity, Long> {

    // already-used method (you may already have this)
    Optional<LikeEntity> findByUserIdAndSongId(Long userId, Long songId);

    // NEW: return how many likes a song has
    int countBySongId(Long songId);

    // NEW: list all LikeEntity rows for a user
    List<LikeEntity> findByUserId(Long userId);

    // optional convenience delete (used in some examples)
    void deleteByUserIdAndSongId(Long userId, Long songId);
}
