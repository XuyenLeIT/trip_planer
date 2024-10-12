package kj001.user_service.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequestDTO {
    @NotNull
    @Email
    private String email;
    @NotNull
    private String oldPassword;
    @NotNull
    @Size(min = 6, message = "New password must be at least 6 characters long")
    private String newPassword;
    @NotNull
    private String confirmPassword;
}
