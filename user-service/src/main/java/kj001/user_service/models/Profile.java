package kj001.user_service.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "profiles")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Profile {
    @Id
    @GeneratedValue
    private Long id;
    private String hobbies;
    private String address;
    private int age;
    private String avatar;
    @Enumerated(EnumType.STRING)
    private MaritalStatus maritalStatus;
    private String description;
    @OneToOne
    @JoinColumn(name="user_id",referencedColumnName = "id")
    @JsonIgnore
    private User user;
}
