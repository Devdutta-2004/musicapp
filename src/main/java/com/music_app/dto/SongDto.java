package com.music_app.dto;

import com.music_app.model.Song;

public class SongDto {

    public Long id;
    public String title;
    public String album;
    public Integer durationSeconds;
    public String artistName;
    public String streamUrl;
    public String mimeType;

    public String coverUrl;
    public String artistImageUrl;

    // ⭐ NEW FIELDS FOR LIKE SYSTEM ⭐
    public boolean liked;      // whether current user liked this song
    public int likeCount;      // total likes on this song


    // ⭐ BACKWARD-COMPATIBLE METHOD (fixes your compile error)
    public static SongDto fromEntity(Song s) {
        return fromEntity(s, false, 0); // default values
    }


    // ⭐ FULL METHOD — called by SongController for real likes
    public static SongDto fromEntity(Song s, boolean liked, int likeCount) {
        SongDto d = new SongDto();

        // Song cover
        if (s.getCoverPath() != null && !s.getCoverPath().isEmpty()) {
            d.coverUrl = "/api/cover/" + s.getCoverPath();
        } else {
            d.coverUrl = null;
        }

        // Artist image fallback
        if (s.getArtist() != null &&
                s.getArtist().getImagePath() != null &&
                !s.getArtist().getImagePath().isEmpty()) {

            d.artistImageUrl = "/api/cover/" + s.getArtist().getImagePath();
        } else {
            d.artistImageUrl = null;
        }

        d.id = s.getId();
        d.title = s.getTitle();
        d.album = s.getAlbum();
        d.durationSeconds = s.getDurationSeconds();
        d.mimeType = s.getMimeType();
        d.artistName = s.getArtist() != null ? s.getArtist().getName() : null;
        d.streamUrl = "/api/audio/" + s.getId();

        // Like data
        d.liked = liked;
        d.likeCount = likeCount;

        return d;
    }
}
