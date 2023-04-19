package com.sutoga.backend.repository;

import com.sutoga.backend.entity.FriendRequest;
import com.sutoga.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    List<FriendRequest> findFriendRequestByReceiver(User receiver);
}
