package com.sutoga.backend.repository;

import com.sutoga.backend.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Notification findByLikeActivity(Like like);
    Notification findByFriendRequestActivity(FriendRequest friendRequestActivity);
    Notification findByCommentActivity(Comment comment);
    List<Notification> findAllByReceiver(User user);
    List<Notification> findAllByLikeActivity(Like like);
}
