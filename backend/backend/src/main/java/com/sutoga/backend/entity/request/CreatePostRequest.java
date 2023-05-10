package com.sutoga.backend.entity.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CreatePostRequest {

    private String description;

    private Long userId;

    private MultipartFile media;
}
