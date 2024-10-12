package kj001.user_service.repository;

import kj001.user_service.models.OTP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<OTP , Long> {
   Optional<OTP> findByEmailAndOtpCode(String otpCode,String email);

   @Query(value = "SELECT * FROM OTP o WHERE o.email = :email ORDER BY o.last_Send DESC LIMIT 1" , nativeQuery = true)
   Optional<OTP> findLastOtpByEmail(@Param("email") String email);
}
