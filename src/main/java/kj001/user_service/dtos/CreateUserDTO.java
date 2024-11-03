package kj001.user_service.dtos;

import kj001.user_service.models.Gender;
import kj001.user_service.models.Roles;
import lombok.Data;

@Data
public class CreateUserDTO {
    private String fullName;
    private String email;
    private String password;
    private Roles role;
    private boolean status = true;
    private Gender gender;

}
