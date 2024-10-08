package kj001.user_service.dtos;

import kj001.user_service.models.Gender;
import kj001.user_service.models.Roles;
import lombok.Data;

@Data
public class CreateUserDTO {
    private String fullName;
    private String password;
    private String email;
    private Roles roles;
    private Gender gender;
    private boolean status = true;
}
