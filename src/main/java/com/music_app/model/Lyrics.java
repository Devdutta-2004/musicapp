package com.music_app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "lyrics")
public class Lyrics {

    @Id
    @Column(name = "song_id", nullable = false)
    private Long songId;

    @Lob
    @Column(name = "lyrics", columnDefinition = "LONGTEXT")
    private String lyrics;

    @Column(name = "source", length = 64)
    private String source;

    // We allow DB to set default timestamps; map as LocalDateTime
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Lyrics() {}

    public Lyrics(Long songId, String lyrics, String source) {
        this.songId = songId;
        this.lyrics = lyrics;
        this.source = source;
    }

    // getters / setters
    public Long getSongId() { return songId; }
    public void setSongId(Long songId) { this.songId = songId; }

    public String getLyrics() { return lyrics; }
    public void setLyrics(String lyrics) { this.lyrics = lyrics; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
