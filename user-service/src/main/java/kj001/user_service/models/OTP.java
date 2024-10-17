package kj001.user_service.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class OTP {
    @Id
    @GeneratedValue
    private Long id;
    private String email;
    private String subject;
    private String content;
    private String otpCode;
    private LocalDateTime expiryTime;  //Thời gian hết hạn của Otp
    private boolean isUsed;            //Otp đã được sử dụng chưa

    //Limit số lần gửi Otp trong ngày
    private int sendAttemps;
    private LocalDateTime lastSend;     //OTP được gửi cuối cùng
}
