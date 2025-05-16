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

    // Агрегированные поля для основного изображения
    @Column(length = 1000, name = "image_url")
    private String imageUrl;

    @Column(name = "image_width")
    private Integer imageWidth;

    @Column(name = "image_height")
    private Integer imageHeight;

    @Column(length = 1000, name = "fullhd_image_url")
    private String fullhdImageUrl;

    @Column(name = "fullhd_width")
    private Integer fullhdWidth;

    @Column(name = "fullhd_height")
    private Integer fullhdHeight;

    @Column(length = 1000, name = "thumbnail_image_url")
    private String thumbnailImageUrl;

    @Column(name = "thumbnail_width")
    private Integer thumbnailWidth;

    @Column(name = "thumbnail_height")
    private Integer thumbnailHeight;

    // Пять оригинальных изображений
    @Column(length = 1000, name = "image_1")
    private String imageUrl1;
    @Column(length = 1000, name = "image_2")
    private String imageUrl2;
    @Column(length = 1000, name = "image_3")
    private String imageUrl3;
    @Column(length = 1000, name = "image_4")
    private String imageUrl4;
    @Column(length = 1000, name = "image_5")
    private String imageUrl5;

    // Пять FullHD изображений
    @Column(length = 1000, name = "fullhd_image_1")
    private String fullhdImageUrl1;
    @Column(length = 1000, name = "fullhd_image_2")
    private String fullhdImageUrl2;
    @Column(length = 1000, name = "fullhd_image_3")
    private String fullhdImageUrl3;
    @Column(length = 1000, name = "fullhd_image_4")
    private String fullhdImageUrl4;
    @Column(length = 1000, name = "fullhd_image_5")
    private String fullhdImageUrl5;

    // Пять миниатюр
    @Column(length = 1000, name = "thumbnail_image_1")
    private String thumbnailImageUrl1;
    @Column(length = 1000, name = "thumbnail_image_2")
    private String thumbnailImageUrl2;
    @Column(length = 1000, name = "thumbnail_image_3")
    private String thumbnailImageUrl3;
    @Column(length = 1000, name = "thumbnail_image_4")
    private String thumbnailImageUrl4;
    @Column(length = 1000, name = "thumbnail_image_5")
    private String thumbnailImageUrl5;

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

    public String getFullhdImageUrl() {
        return fullhdImageUrl;
    }

    public void setFullhdImageUrl(String fullhdImageUrl) {
        this.fullhdImageUrl = fullhdImageUrl;
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

    public String getThumbnailImageUrl() {
        return thumbnailImageUrl;
    }

    public void setThumbnailImageUrl(String thumbnailImageUrl) {
        this.thumbnailImageUrl = thumbnailImageUrl;
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

    public String getImageUrl1() {
        return imageUrl1;
    }

    public void setImageUrl1(String imageUrl1) {
        this.imageUrl1 = imageUrl1;
    }

    public String getImageUrl2() {
        return imageUrl2;
    }

    public void setImageUrl2(String imageUrl2) {
        this.imageUrl2 = imageUrl2;
    }

    public String getImageUrl3() {
        return imageUrl3;
    }

    public void setImageUrl3(String imageUrl3) {
        this.imageUrl3 = imageUrl3;
    }

    public String getImageUrl4() {
        return imageUrl4;
    }

    public void setImageUrl4(String imageUrl4) {
        this.imageUrl4 = imageUrl4;
    }

    public String getImageUrl5() {
        return imageUrl5;
    }

    public void setImageUrl5(String imageUrl5) {
        this.imageUrl5 = imageUrl5;
    }

    public String getFullhdImageUrl1() {
        return fullhdImageUrl1;
    }

    public void setFullhdImageUrl1(String fullhdImageUrl1) {
        this.fullhdImageUrl1 = fullhdImageUrl1;
    }

    public String getFullhdImageUrl2() {
        return fullhdImageUrl2;
    }

    public void setFullhdImageUrl2(String fullhdImageUrl2) {
        this.fullhdImageUrl2 = fullhdImageUrl2;
    }

    public String getFullhdImageUrl3() {
        return fullhdImageUrl3;
    }

    public void setFullhdImageUrl3(String fullhdImageUrl3) {
        this.fullhdImageUrl3 = fullhdImageUrl3;
    }

    public String getFullhdImageUrl4() {
        return fullhdImageUrl4;
    }

    public void setFullhdImageUrl4(String fullhdImageUrl4) {
        this.fullhdImageUrl4 = fullhdImageUrl4;
    }

    public String getFullhdImageUrl5() {
        return fullhdImageUrl5;
    }

    public void setFullhdImageUrl5(String fullhdImageUrl5) {
        this.fullhdImageUrl5 = fullhdImageUrl5;
    }

    public String getThumbnailImageUrl1() {
        return thumbnailImageUrl1;
    }

    public void setThumbnailImageUrl1(String thumbnailImageUrl1) {
        this.thumbnailImageUrl1 = thumbnailImageUrl1;
    }

    public String getThumbnailImageUrl2() {
        return thumbnailImageUrl2;
    }

    public void setThumbnailImageUrl2(String thumbnailImageUrl2) {
        this.thumbnailImageUrl2 = thumbnailImageUrl2;
    }

    public String getThumbnailImageUrl3() {
        return thumbnailImageUrl3;
    }

    public void setThumbnailImageUrl3(String thumbnailImageUrl3) {
        this.thumbnailImageUrl3 = thumbnailImageUrl3;
    }

    public String getThumbnailImageUrl4() {
        return thumbnailImageUrl4;
    }

    public void setThumbnailImageUrl4(String thumbnailImageUrl4) {
        this.thumbnailImageUrl4 = thumbnailImageUrl4;
    }

    public String getThumbnailImageUrl5() {
        return thumbnailImageUrl5;
    }

    public void setThumbnailImageUrl5(String thumbnailImageUrl5) {
        this.thumbnailImageUrl5 = thumbnailImageUrl5;
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