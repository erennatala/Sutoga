package com.sutoga.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne
    @JsonIgnore
    private User receiver;

    private String senderUsername;

    private String senderPhotoUrl;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private Like likeActivity;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private Comment commentActivity;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private FriendRequest friendRequestActivity;

    @Column
    private boolean seen;

    @Column
    private LocalDateTime createdAt;
}
