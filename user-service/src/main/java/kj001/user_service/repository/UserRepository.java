package kj001.user_service.repository;

import jakarta.transaction.Transactional;
import kj001.user_service.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    // Lấy tất cả người dùng có email và đang hoạt động
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isActive = true")
    List<User> findByEmailAndIsActiveTrue(@Param("email") String email);
    Optional<User> findByEmailAndOtpCode(String email,String code);
    // Lấy tất cả người dùng có email và ngày đăng ký trong khoảng thời gian
    @Query("SELECT u FROM User u WHERE u.email = :email" +
            " AND u.createAt BETWEEN :startDate AND :endDate")
    List<User> findByEmailAndRegistrationDateBetween(
            @Param("email") String email,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    // Xóa tất cả các bản ghi cho email trước thời gian nhất định với isActive = false
    @Modifying
    @Query("DELETE FROM User u WHERE u.email = :email AND u.createAt < :dateTime AND u.isActive = false")
    void deleteByEmailAndRegistrationDateBeforeAndIsActiveFalse(@Param("email")
                                                                String email, @Param("dateTime") LocalDateTime dateTime);

    @Modifying
    @Transactional
    @Query("DELETE FROM User u WHERE u.email = :email AND u.isActive = false AND u.id <> :currentUserId")    //<> : ký hiệu là != id hiện tại
    void deleteInactiveUsersByEmail(@Param("email") String email, @Param("currentUserId") Long currentUserId);
}
