package com.sutoga.backend.repository;

import com.sutoga.backend.entity.Like;
import com.sutoga.backend.entity.Notification;
import com.sutoga.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Notification findByLikeActivity(Like like);
    List<Notification> findAllByReceiver(User user);
}
