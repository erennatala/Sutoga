package com.sutoga.backend.repository;

import com.sutoga.backend.entity.FriendRequest;
import com.sutoga.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    List<FriendRequest> findByReceiverIdAndConfirmedFalse(Long receiverId);
    FriendRequest findBySenderAndReceiver(User sender, User receiver);
    List<FriendRequest> findBySenderAndReceiverIn(User sender, List<User> receivers);
}
