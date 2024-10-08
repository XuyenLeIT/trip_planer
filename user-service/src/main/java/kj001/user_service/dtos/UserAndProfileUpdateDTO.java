package kj001.user_service.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import kj001.user_service.models.Gender;
import kj001.user_service.models.MaritalStatus;
import kj001.user_service.models.Roles;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UserAndProfileUpdateDTO {
    //Update User
    private String fullName;
    private Roles role;
    private Gender gender;
    private boolean status;
    private String phone;

    //Update Profile
    private String hobbies;
    private String address;
    private int age;
    private String avatar;
    private MaritalStatus maritalStatus;
    private String description;

    @JsonIgnore
    private MultipartFile file;
}
