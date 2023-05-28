package com.sutoga.backend.entity.response;

import lombok.Data;

@Data
public class GameResponse {
    private Long id;
    private String gameTitle;
    private String gameDescription;
    private Long playtime;
    private String gamePhotoUrl;
    private String publisher;
    private String developer;
    private String releaseDate;

}
