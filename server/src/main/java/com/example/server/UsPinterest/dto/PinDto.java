package com.example.server.UsPinterest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PinDto {
    private Long id;
    private String title;
    private String description;
    private String thumbnailUrl;
    private int likesCount;
    private int commentsCount;
    private Double rating;
    private String createdAt;
    private Long authorId;
    private String authorUsername;
} 