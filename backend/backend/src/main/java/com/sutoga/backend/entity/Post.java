package com.sutoga.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "post")
public class Post {

    @Id
    private Long id;

    private String description;
    
    private User user;

    private List<Comment> comments;


}
