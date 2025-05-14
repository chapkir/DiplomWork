package com.example.server.UsPinterest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "pictures")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Picture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 1000)
    private String imageUrl;

    @Column(length = 1000, name = "fullhd_image_url")
    private String fullhdImageUrl;

    @Column(length = 1000, name = "thumbnail_image_url")
    private String thumbnailImageUrl;

    @Column(name = "image_width")
    private Integer imageWidth;

    @Column(name = "image_height")
    private Integer imageHeight;

    @Column(name = "fullhd_width")
    private Integer fullhdWidth;

    @Column(name = "fullhd_height")
    private Integer fullhdHeight;

    @Column(name = "thumbnail_width")
    private Integer thumbnailWidth;

    @Column(name = "thumbnail_height")
    private Integer thumbnailHeight;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pin_id", nullable = false)
    @JsonIgnoreProperties("pictures")
    private Pin pin;

    public Picture() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getFullhdImageUrl() {
        return fullhdImageUrl;
    }

    public void setFullhdImageUrl(String fullhdImageUrl) {
        this.fullhdImageUrl = fullhdImageUrl;
    }

    public String getThumbnailImageUrl() {
        return thumbnailImageUrl;
    }

    public void setThumbnailImageUrl(String thumbnailImageUrl) {
        this.thumbnailImageUrl = thumbnailImageUrl;
    }

    public Integer getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(Integer imageWidth) {
        this.imageWidth = imageWidth;
    }

    public Integer getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(Integer imageHeight) {
        this.imageHeight = imageHeight;
    }

    public Integer getFullhdWidth() {
        return fullhdWidth;
    }

    public void setFullhdWidth(Integer fullhdWidth) {
        this.fullhdWidth = fullhdWidth;
    }

    public Integer getFullhdHeight() {
        return fullhdHeight;
    }

    public void setFullhdHeight(Integer fullhdHeight) {
        this.fullhdHeight = fullhdHeight;
    }

    public Integer getThumbnailWidth() {
        return thumbnailWidth;
    }

    public void setThumbnailWidth(Integer thumbnailWidth) {
        this.thumbnailWidth = thumbnailWidth;
    }

    public Integer getThumbnailHeight() {
        return thumbnailHeight;
    }

    public void setThumbnailHeight(Integer thumbnailHeight) {
        this.thumbnailHeight = thumbnailHeight;
    }

    public Pin getPin() {
        return pin;
    }

    public void setPin(Pin pin) {
        this.pin = pin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Picture picture = (Picture) o;
        return Objects.equals(id, picture.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
} 