package com.sutoga.backend.entity.request;

import com.sutoga.backend.entity.User;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CreatePostRequest {

    private String description;

    private User user;

    private MultipartFile media;
}
