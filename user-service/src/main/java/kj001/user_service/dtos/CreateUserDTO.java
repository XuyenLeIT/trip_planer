package kj001.user_service.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kj001.user_service.models.Gender;
import kj001.user_service.models.Roles;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateUserDTO {
    @NotNull(message = "Full name is required.")
    private String fullName;

    @NotBlank(message = "Password is required.")
    @Size(min = 6, message = "Password must be at least 6 character long")
    private String password;

    @NotBlank(message = "Password is required.")
    @Size(min = 6, message = "Password must be at least 6 character long")
    private String confirmPassword;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotNull(message = "Role is required.")
    private Roles role;

    private String otpCode;
    private LocalDateTime expiryTime;
    private Gender gender;
    private LocalDateTime createAt;
}
