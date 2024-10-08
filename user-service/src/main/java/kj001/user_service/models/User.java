package kj001.user_service.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name="users")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class User {
    @Id
    @GeneratedValue
    private long id;

    @Column(nullable = false)
    private String fullName;

    @Column(unique=true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;
    private boolean status;
    private String phone;

    @Enumerated(EnumType.STRING)
    private Roles role;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Profile profile;

    private LocalDateTime createAt;

    @Enumerated(EnumType.STRING)
    private Gender gender;
}
