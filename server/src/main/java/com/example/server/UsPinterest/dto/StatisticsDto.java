package com.example.server.UsPinterest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsDto {
    private int totalUsers;
    private int totalPins;
    private int totalComments;
    private int totalLikes;
    private int activeUsersLastWeek;
    private int newUsersLastWeek;
    private int newPinsLastWeek;
    private double averageRating;
} 