package com.sutoga.backend.repository;
import org.springframework.data.jpa.repository.JpaRepository;
        import org.springframework.stereotype.Repository;
        import com.sutoga.backend.entity.Game;
        import com.sutoga.backend.entity.User;
        import com.sutoga.backend.entity.UserGame;

@Repository
public interface UserGameRepository extends JpaRepository<UserGame, Long> {
    UserGame findByUserAndGame(User user, Game game);


}
