package kj001.user_service.dtos;

import jakarta.persistence.*;
import kj001.user_service.models.Gender;
import kj001.user_service.models.Profile;
import kj001.user_service.models.Roles;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponseDTO {
    private long id;

    @Column(nullable = false)
    private String fullName;

    @Column(unique=true, nullable = false)
    private String email;

    private boolean isActive;
    private String phone;

    @Enumerated(EnumType.STRING)
    private Roles role;


    @Enumerated(EnumType.STRING)
    private Gender gender;

    private ProfileDTO profileDTO;
}
