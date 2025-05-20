package com.example.server.UsPinterest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityData {
    private String date;
    private int newUsers;
    private int newPins;
    private int newComments;
    private int newLikes;
} 