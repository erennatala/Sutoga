package com.sutoga.backend.entity.response;

import lombok.Data;

@Data
public class RecommendationResponse {
    private Long id;
    private String gameTitle;
    private String gameDescription;
    private String gamePhotoUrl;
    private String publisher;
    private String developer;
    private String releaseDate;
    private String score;
}
