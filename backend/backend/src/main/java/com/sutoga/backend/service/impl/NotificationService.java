package com.sutoga.backend.service.impl;

import com.corundumstudio.socketio.SocketIOServer;
import com.sutoga.backend.entity.Notification;
import com.sutoga.backend.entity.User;
import com.sutoga.backend.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SocketIOServer server;

    public NotificationService(NotificationRepository notificationRepository, SocketIOServer server) {
        this.notificationRepository = notificationRepository;
        this.server = server;
    }

    public List<Notification> getNotifications(User user) {
        return notificationRepository.findAllByReceiver(user);
    }

    public void createAndSendNotification(Notification notification) {
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
        System.out.println("Sending notification: " + notification);
        server.getBroadcastOperations().sendEvent("notification", notification);
    }

}