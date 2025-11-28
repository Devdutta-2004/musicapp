package com.music_app.model;


import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "artists")
public class Artist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String bio;
    @Column(name = "image_path")
    private String imagePath;      // NEW


    @Column(columnDefinition = "TEXT")


    private Instant createdAt = Instant.now();

    public Artist() {}

    public Artist(String name, String bio) {
        this.name = name;
        this.bio = bio;
    }

    // Getters & Setters
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
