package kj001.user_service.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VerifyOTPRequestDTO {
    @NotNull
    @Email
    private String email;
    @NotNull
    private String otpCode;
}
