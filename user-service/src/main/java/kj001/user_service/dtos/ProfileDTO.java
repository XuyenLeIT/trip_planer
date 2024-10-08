package kj001.user_service.dtos;

import kj001.user_service.models.MaritalStatus;
import lombok.Data;

@Data
public class ProfileDTO {
    private long id;
    private String hobbies;
    private String address;
    private int age;
    private String avatar;
    private MaritalStatus maritalStatus;
    private String description;
}
