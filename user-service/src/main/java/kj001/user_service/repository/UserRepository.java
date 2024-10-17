package kj001.user_service.repository;

import jakarta.transaction.Transactional;
import kj001.user_service.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailAndOtpCode(String email, String code);

    @Modifying
    @Transactional
    @Query("DELETE FROM User u WHERE u.email = :email AND u.isActive = false AND u.id <> :currentUserId")    //<> : ký hiệu là != id hiện tại
    void deleteInactiveUsersByEmail(@Param("email") String email, @Param("currentUserId") Long currentUserId);
}
