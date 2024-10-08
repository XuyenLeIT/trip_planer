package kj001.user_service.dtos;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import kj001.user_service.models.Gender;
import kj001.user_service.models.Roles;
import lombok.Data;

@Data
public class UserResponseDTO {
    private Long id;
    private String fullName;
    private String email;
    private boolean status;
    private String phone;
    private Roles role;
    private Gender gender;
    private ProfileDTO profileDTO;

}
