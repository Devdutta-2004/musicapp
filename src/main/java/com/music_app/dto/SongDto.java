package com.music_app.dto;

import com.music_app.model.Song;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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


    // keep the old convenience method (backward compatible)
    public static SongDto fromEntity(Song s) {
        return fromEntity(s, "", false, 0);
    }

    // existing method you had (if you want to keep calling the /api/ endpoints)
    public static SongDto fromEntity(Song s, boolean liked, int likeCount) {
        SongDto d = new SongDto();

        if (s.getCoverPath() != null && !s.getCoverPath().isEmpty()) {
            d.coverUrl = "/api/cover/" + s.getCoverPath();
        } else {
            d.coverUrl = null;
        }

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

        d.liked = liked;
        d.likeCount = likeCount;
        return d;
    }


    // ---------- NEW: build public URLs directly using filesBaseUrl ----------
    // Use this when you want the frontend to stream directly from Cloudflare R2
    public static SongDto fromEntity(Song s, String filesBaseUrl, boolean liked, int likeCount) {
        SongDto d = new SongDto();

        d.id = s.getId();
        d.title = s.getTitle();
        d.album = s.getAlbum();
        d.durationSeconds = s.getDurationSeconds();
        d.mimeType = s.getMimeType();
        d.artistName = s.getArtist() != null ? s.getArtist().getName() : null;

        // Build streamUrl: if filesBaseUrl is provided use it + song.filePath,
        // otherwise fallback to existing API audio route (server streaming)
        if (filesBaseUrl != null && !filesBaseUrl.isBlank() && s.getFilePath() != null && !s.getFilePath().isBlank()) {
            d.streamUrl = buildPublicUrl(filesBaseUrl, s.getFilePath());
        } else {
            d.streamUrl = "/api/audio/" + s.getId();
        }

        // cover
        if (s.getCoverPath() != null && !s.getCoverPath().isEmpty()) {
            if (filesBaseUrl != null && !filesBaseUrl.isBlank()) {
                d.coverUrl = buildPublicUrl(filesBaseUrl, s.getCoverPath());
            } else {
                d.coverUrl = "/api/cover/" + s.getCoverPath();
            }
        } else {
            d.coverUrl = null;
        }

        // artist image
        if (s.getArtist() != null && s.getArtist().getImagePath() != null && !s.getArtist().getImagePath().isEmpty()) {
            if (filesBaseUrl != null && !filesBaseUrl.isBlank()) {
                d.artistImageUrl = buildPublicUrl(filesBaseUrl, s.getArtist().getImagePath());
            } else {
                d.artistImageUrl = "/api/cover/" + s.getArtist().getImagePath();
            }
        } else {
            d.artistImageUrl = null;
        }

        d.liked = liked;
        d.likeCount = likeCount;

        return d;
    }

    // helper: join base + path and URL-encode path segments (preserves /)
    private static String buildPublicUrl(String base, String path) {
        if (path == null || path.isEmpty()) return null;
        String b = base.trim();
        if (b.endsWith("/")) b = b.substring(0, b.length() - 1);
        if (path.startsWith("/")) path = path.substring(1);

        // encode each segment separately so slashes remain separators
        String[] parts = path.split("/");
        StringBuilder encoded = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String segment = parts[i];
            String e = URLEncoder.encode(segment, StandardCharsets.UTF_8);
            // URLEncoder encodes spaces as +; browsers prefer %20 — replace + with %20
            e = e.replace("+", "%20");
            encoded.append(e);
            if (i < parts.length - 1) encoded.append("/");
        }
        return b + "/" + encoded.toString();
    }
}
