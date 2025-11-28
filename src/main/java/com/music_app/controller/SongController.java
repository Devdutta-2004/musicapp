//
//package com.music_app.controller;
//
//import com.music_app.dto.SongDto;
//import com.music_app.model.Artist;
//import com.music_app.model.LikeEntity;
//import com.music_app.model.Song;
//import com.music_app.repository.ArtistRepository;
//import com.music_app.repository.LikeRepository;
//import com.music_app.repository.SongRepository;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.MediaType;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//import org.springframework.web.server.ResponseStatusException;
//import org.springframework.http.HttpStatus;
//
//import java.io.IOException;
//import java.nio.file.*;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@RestController
//@RequestMapping("/api/songs")
//public class SongController {
//
//    private final SongRepository songRepository;
//    private final LikeRepository likeRepository;
//    private final ArtistRepository artistRepository;
//
//    @Value("${app.media.base-path}")
//    private String mediaBasePath;
//
//    public SongController(SongRepository songRepository, LikeRepository likeRepository, ArtistRepository artistRepository) {
//        this.songRepository = songRepository;
//        this.likeRepository = likeRepository;
//        this.artistRepository = artistRepository;
//    }
//
//    // Helper: safely parse user header (may be null)
//    private Long parseUserId(Long headerUserId) {
//        return headerUserId == null ? null : headerUserId;
//    }
//
//    @GetMapping("/search")
//    public List<SongDto> search(@RequestParam("q") String q,
//                                @RequestHeader(value = "X-User-Id", required = false) Long userIdHeader) {
//        String ql = q == null ? "" : q.toLowerCase();
//        List<Song> matched = songRepository.findAll().stream()
//                .filter(s -> (s.getTitle() != null && s.getTitle().toLowerCase().contains(ql)) ||
//                        (s.getArtist() != null && s.getArtist().getName() != null && s.getArtist().getName().toLowerCase().contains(ql)))
//                .collect(Collectors.toList());
//
//        Long userId = parseUserId(userIdHeader);
//
//        return matched.stream()
//                .map(s -> {
//                    int count = likeRepository.countBySongId(s.getId());
//                    boolean liked = (userId != null) && likeRepository.findByUserIdAndSongId(userId, s.getId()).isPresent();
//                    return SongDto.fromEntity(s, liked, count);
//                })
//                .collect(Collectors.toList());
//    }
//
//    @GetMapping
//    public List<SongDto> list(@RequestHeader(value = "X-User-Id", required = false) Long userIdHeader) {
//        Long userId = parseUserId(userIdHeader);
//
//        return songRepository.findAll().stream()
//                .map(s -> {
//                    int count = likeRepository.countBySongId(s.getId());
//                    boolean liked = (userId != null) && likeRepository.findByUserIdAndSongId(userId, s.getId()).isPresent();
//                    return SongDto.fromEntity(s, liked, count);
//                })
//                .collect(Collectors.toList());
//    }
//
//    @GetMapping("/{id}")
//    public SongDto getOne(@PathVariable Long id, @RequestHeader(value = "X-User-Id", required = false) Long userIdHeader) {
//        Song s = songRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
//        Long userId = parseUserId(userIdHeader);
//        int count = likeRepository.countBySongId(s.getId());
//        boolean liked = (userId != null) && likeRepository.findByUserIdAndSongId(userId, s.getId()).isPresent();
//        return SongDto.fromEntity(s, liked, count);
//    }
//
//    /**
//     * Like a song (creates LikeEntity).
//     * Header: X-User-Id required
//     */
//    @PostMapping("/{id}/like")
//    public Map<String, Object> like(@PathVariable Long id, @RequestHeader("X-User-Id") Long userId) {
//        if (!songRepository.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
//
//        var existing = likeRepository.findByUserIdAndSongId(userId, id);
//        if (existing.isPresent()) {
//            // already liked -> idempotent
//            int count = likeRepository.countBySongId(id);
//            return Map.of("liked", true, "likeCount", count);
//        }
//
//        LikeEntity l = new LikeEntity();
//        l.setUserId(userId);
//        l.setSongId(id);
//        likeRepository.save(l);
//        int count = likeRepository.countBySongId(id);
//        return Map.of("liked", true, "likeCount", count);
//    }
//
//    /**
//     * Unlike (delete like).
//     * Header: X-User-Id required
//     */
//    @DeleteMapping("/{id}/like")
//    public Map<String, Object> unlike(@PathVariable Long id, @RequestHeader("X-User-Id") Long userId) {
//        if (!songRepository.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
//
//        var existing = likeRepository.findByUserIdAndSongId(userId, id);
//        if (existing.isPresent()) {
//            likeRepository.delete(existing.get());
//        }
//        int count = likeRepository.countBySongId(id);
//        return Map.of("liked", false, "likeCount", count);
//    }
//
//    /**
//     * Upload endpoint: saves file to app.media.base-path and creates a Song row.
//     * Fields: file (multipart), title, optional artistId
//     */
//    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public SongDto upload(@RequestParam("file") MultipartFile file,
//                          @RequestParam("title") String title,
//                          @RequestParam(value = "artistId", required = false) Long artistId) {
//
//        if (file.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Empty file");
//
//        try {
//            // create media base if missing
//            Path base = Paths.get(mediaBasePath);
//            if (!Files.exists(base)) Files.createDirectories(base);
//
//            String original = file.getOriginalFilename();
//            String ext = original != null && original.contains(".") ? original.substring(original.lastIndexOf('.')) : ".mp3";
//            String savedName = java.util.UUID.randomUUID().toString() + ext;
//            Path target = base.resolve(savedName);
//
//            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
//
//            Song s = new Song();
//            s.setTitle(title);
//            s.setFilePath(savedName); // relative name
//            s.setMimeType(file.getContentType());
//            if (artistId != null && artistRepository.existsById(artistId)) {
//                Artist a = artistRepository.findById(artistId).get();
//                s.setArtist(a);
//            }
//            songRepository.save(s);
//
//            // New song — 0 likes, not liked by default
//            return SongDto.fromEntity(s, false, 0);
//
//        } catch (IOException e) {
//            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save file", e);
//        }
//    }
//
//}
package com.music_app.controller;

import com.music_app.dto.SongDto;
import com.music_app.model.Artist;
import com.music_app.model.LikeEntity;
import com.music_app.model.Song;
import com.music_app.repository.ArtistRepository;
import com.music_app.repository.LikeRepository;
import com.music_app.repository.SongRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/songs")
public class SongController {

    private final SongRepository songRepository;
    private final LikeRepository likeRepository;
    private final ArtistRepository artistRepository;

    @Value("${app.media.base-path}")
    private String mediaBasePath;

    public SongController(SongRepository songRepository, LikeRepository likeRepository, ArtistRepository artistRepository) {
        this.songRepository = songRepository;
        this.likeRepository = likeRepository;
        this.artistRepository = artistRepository;
    }

    // Helper: safely parse user header (may be null)
    private Long parseUserId(Long headerUserId) {
        return headerUserId == null ? null : headerUserId;
    }

    @GetMapping("/search")
    public List<SongDto> search(@RequestParam("q") String q,
                                @RequestHeader(value = "X-User-Id", required = false) Long userIdHeader) {
        String ql = q == null ? "" : q.toLowerCase();
        List<Song> matched = songRepository.findAll().stream()
                .filter(s -> (s.getTitle() != null && s.getTitle().toLowerCase().contains(ql)) ||
                        (s.getArtist() != null && s.getArtist().getName() != null && s.getArtist().getName().toLowerCase().contains(ql)))
                .collect(Collectors.toList());

        Long userId = parseUserId(userIdHeader);

        return matched.stream()
                .map(s -> {
                    int count = likeRepository.countBySongId(s.getId());
                    boolean liked = (userId != null) && likeRepository.findByUserIdAndSongId(userId, s.getId()).isPresent();
                    return SongDto.fromEntity(s, liked, count);
                })
                .collect(Collectors.toList());
    }

    @GetMapping
    public List<SongDto> list(@RequestHeader(value = "X-User-Id", required = false) Long userIdHeader) {
        Long userId = parseUserId(userIdHeader);

        return songRepository.findAll().stream()
                .map(s -> {
                    int count = likeRepository.countBySongId(s.getId());
                    boolean liked = (userId != null) && likeRepository.findByUserIdAndSongId(userId, s.getId()).isPresent();
                    return SongDto.fromEntity(s, liked, count);
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public SongDto getOne(@PathVariable Long id, @RequestHeader(value = "X-User-Id", required = false) Long userIdHeader) {
        Song s = songRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Long userId = parseUserId(userIdHeader);
        int count = likeRepository.countBySongId(s.getId());
        boolean liked = (userId != null) && likeRepository.findByUserIdAndSongId(userId, s.getId()).isPresent();
        return SongDto.fromEntity(s, liked, count);
    }

    /**
     * Like a song (creates LikeEntity).
     * Header: X-User-Id required
     */
    @PostMapping("/{id}/like")
    public Map<String, Object> like(@PathVariable Long id, @RequestHeader("X-User-Id") Long userId) {
        if (!songRepository.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        var existing = likeRepository.findByUserIdAndSongId(userId, id);
        if (existing.isPresent()) {
            // already liked -> idempotent
            int count = likeRepository.countBySongId(id);
            return Map.of("liked", true, "likeCount", count);
        }

        LikeEntity l = new LikeEntity();
        l.setUserId(userId);
        l.setSongId(id);
        likeRepository.save(l);
        int count = likeRepository.countBySongId(id);
        return Map.of("liked", true, "likeCount", count);
    }

    /**
     * Unlike (delete like).
     * Header: X-User-Id required
     */
    @DeleteMapping("/{id}/like")
    public Map<String, Object> unlike(@PathVariable Long id, @RequestHeader("X-User-Id") Long userId) {
        if (!songRepository.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        var existing = likeRepository.findByUserIdAndSongId(userId, id);
        if (existing.isPresent()) {
            likeRepository.delete(existing.get());
        }
        int count = likeRepository.countBySongId(id);
        return Map.of("liked", false, "likeCount", count);
    }

    /**
     * Upload endpoint: saves file to app.media.base-path and creates a Song row.
     * Fields: file (multipart), title, optional artistId
     *
     * Extended to accept:
     * - artistName (optional) -- create or lookup artist by name
     * - artistImage (optional) -- file to save as artist image
     * - coverImage (optional)  -- file to save as song cover
     * - album (optional)
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SongDto upload(@RequestParam("file") MultipartFile file,
                          @RequestParam("title") String title,
                          @RequestParam(value = "artistId", required = false) Long artistId,
                          @RequestParam(value = "artistName", required = false) String artistName,
                          @RequestParam(value = "artistImage", required = false) MultipartFile artistImage,
                          @RequestParam(value = "coverImage", required = false) MultipartFile coverImage,
                          @RequestParam(value = "album", required = false) String album) {

        if (file == null || file.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Empty file");

        try {
            // create media base if missing
            Path base = Paths.get(mediaBasePath);
            if (!Files.exists(base)) Files.createDirectories(base);

            // ---------------- save audio ----------------
            String original = file.getOriginalFilename();
            String ext = original != null && original.contains(".") ? original.substring(original.lastIndexOf('.')) : ".mp3";
            String savedName = java.util.UUID.randomUUID().toString() + ext;
            Path songsFolder = base.resolve("songs");
            Files.createDirectories(songsFolder);
            Path savedAudio = songsFolder.resolve(savedName);
            Files.copy(file.getInputStream(), savedAudio, StandardCopyOption.REPLACE_EXISTING);

            // ---------------- save cover image if provided ----------------
            String coverSavedName = null;
            if (coverImage != null && !coverImage.isEmpty()) {
                String origCover = coverImage.getOriginalFilename();
                String extCover = origCover != null && origCover.contains(".") ? origCover.substring(origCover.lastIndexOf('.')) : ".png";
                coverSavedName = java.util.UUID.randomUUID().toString() + extCover;
                Path coverFolder = base.resolve("covers");
                Files.createDirectories(coverFolder);
                Files.copy(coverImage.getInputStream(), coverFolder.resolve(coverSavedName), StandardCopyOption.REPLACE_EXISTING);
            }

            // ---------------- handle artist ----------------
            Artist artist = null;

            // Case A: artistId provided and exists -> use it (and update image if provided)
            if (artistId != null && artistRepository.existsById(artistId)) {
                artist = artistRepository.findById(artistId).get();
                if (artistImage != null && !artistImage.isEmpty()) {
                    String orig = artistImage.getOriginalFilename();
                    String extImg = orig != null && orig.contains(".") ? orig.substring(orig.lastIndexOf('.')) : ".png";
                    String imgName = java.util.UUID.randomUUID().toString() + extImg;
                    Path artistFolder = base.resolve("artists");
                    Files.createDirectories(artistFolder);
                    Files.copy(artistImage.getInputStream(), artistFolder.resolve(imgName), StandardCopyOption.REPLACE_EXISTING);
                    artist.setImagePath("artists/" + imgName);
                    artist = artistRepository.save(artist);
                }
            }
            // Case B: artistName provided -> lookup or create, and set image if present
            else if (artistName != null && !artistName.isBlank()) {
                Optional<Artist> maybe = artistRepository.findByName(artistName.trim());
                if (maybe.isPresent()) {
                    artist = maybe.get();
                    if (artistImage != null && !artistImage.isEmpty()) {
                        String orig = artistImage.getOriginalFilename();
                        String extImg = orig != null && orig.contains(".") ? orig.substring(orig.lastIndexOf('.')) : ".png";
                        String imgName = java.util.UUID.randomUUID().toString() + extImg;
                        Path artistFolder = base.resolve("artists");
                        Files.createDirectories(artistFolder);
                        Files.copy(artistImage.getInputStream(), artistFolder.resolve(imgName), StandardCopyOption.REPLACE_EXISTING);
                        artist.setImagePath("artists/" + imgName);
                    }
                    artist = artistRepository.save(artist);
                } else {
                    // create new artist with provided name
                    artist = new Artist();
                    artist.setName(artistName.trim());
                    if (artistImage != null && !artistImage.isEmpty()) {
                        String orig = artistImage.getOriginalFilename();
                        String extImg = orig != null && orig.contains(".") ? orig.substring(orig.lastIndexOf('.')) : ".png";
                        String imgName = java.util.UUID.randomUUID().toString() + extImg;
                        Path artistFolder = base.resolve("artists");
                        Files.createDirectories(artistFolder);
                        Files.copy(artistImage.getInputStream(), artistFolder.resolve(imgName), StandardCopyOption.REPLACE_EXISTING);
                        artist.setImagePath("artists/" + imgName);
                    }
                    artist = artistRepository.save(artist);
                }
            }
            // Case C (NEW): no artist name/id but artistImage PROVIDED -> create placeholder artist so image can be saved
            else if (artistImage != null && !artistImage.isEmpty()) {
                artist = new Artist();
                artist.setName("Unknown Artist");
                String orig = artistImage.getOriginalFilename();
                String extImg = orig != null && orig.contains(".") ? orig.substring(orig.lastIndexOf('.')) : ".png";
                String imgName = java.util.UUID.randomUUID().toString() + extImg;
                Path artistFolder = base.resolve("artists");
                Files.createDirectories(artistFolder);
                Files.copy(artistImage.getInputStream(), artistFolder.resolve(imgName), StandardCopyOption.REPLACE_EXISTING);
                artist.setImagePath("artists/" + imgName);
                artist = artistRepository.save(artist);
            }
            // else: no artist info and no artistImage -> leave artist null

            // ---------------- create Song entity and save ----------------
            Song s = new Song();
            s.setTitle(title != null ? title : (file.getOriginalFilename() == null ? "Unknown" : file.getOriginalFilename()));
            s.setFilePath("songs/" + savedName); // store relative path
            s.setMimeType(file.getContentType());
            if (album != null) s.setAlbum(album);
            if (coverSavedName != null) s.setCoverPath("covers/" + coverSavedName);
            if (artist != null) s.setArtist(artist);

            songRepository.save(s);

            // New song — 0 likes, not liked by default
            return SongDto.fromEntity(s, false, 0);

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save file", e);
        }
    }

}
