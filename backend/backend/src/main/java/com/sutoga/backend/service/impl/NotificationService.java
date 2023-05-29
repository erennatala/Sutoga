package com.sutoga.backend.service.impl;

import com.corundumstudio.socketio.SocketIOServer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sutoga.backend.entity.*;
import com.sutoga.backend.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SocketIOServer server;
    private final ObjectMapper objectMapper;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository, SocketIOServer server, ObjectMapper objectMapper) {
        this.notificationRepository = notificationRepository;
        this.server = server;
        this.objectMapper = objectMapper;
    }

    public NotificationService(NotificationRepository notificationRepository, SocketIOServer server) {
        this(notificationRepository, server, new ObjectMapper());
    }

    public List<Notification> getNotifications(User user) {
        return notificationRepository.findAllByReceiver(user);
    }

    public void createAndSendNotification(Notification notification) {
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);

        try {
            String jsonNotification = objectMapper.writeValueAsString(notification);
            server.getBroadcastOperations().sendEvent("notification", jsonNotification);
        } catch (JsonProcessingException e) {
            System.out.println("gönderilemedi " + e);
        }
    }

    public void deleteNotificationByFriendRequestId(FriendRequest friendRequestId) {
        Notification notification = notificationRepository.findByFriendRequestActivity(friendRequestId);
        if (notification != null) {
            notificationRepository.delete(notification);

            try {
                String jsonNotification = objectMapper.writeValueAsString(notification);
                server.getBroadcastOperations().sendEvent("cancelNotification", jsonNotification);
            } catch (JsonProcessingException e) {
                System.out.println("Bildirim geri çekilemedi " + e);
            }
        }
    }

    public void deleteNotificationByLike(Like like) {
        Notification notification = notificationRepository.findByLikeActivity(like);
        if (notification != null) {
            try {
                String jsonNotification = objectMapper.writeValueAsString(notification);
                server.getBroadcastOperations().sendEvent("cancelNotification", jsonNotification);
            } catch (JsonProcessingException e) {
                System.out.println("Bildirim geri çekilemedi " + e);
            }
        }
    }

    public void deleteNotificationByComment(Comment comment) {
        Notification notification = notificationRepository.findByCommentActivity(comment);
        if (notification != null) {
            try {
                String jsonNotification = objectMapper.writeValueAsString(notification);
                server.getBroadcastOperations().sendEvent("cancelNotification", jsonNotification);
            } catch (JsonProcessingException e) {
                System.out.println("Bildirim geri çekilemedi " + e);
            }
        }
    }
}