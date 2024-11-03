package kj001.user_service.controllers;

import kj001.user_service.dtos.CreateUserDTO;
import kj001.user_service.dtos.UserAndProfileUpdateDTO;
import kj001.user_service.dtos.UserResponseDTO;
import kj001.user_service.models.User;
import kj001.user_service.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateOneUser(@PathVariable Long id,
                 @ModelAttribute UserAndProfileUpdateDTO userAndProfileUpdateDTO) throws IOException {
        Optional<UserResponseDTO> updatedUser  = userService.updateUser(id,userAndProfileUpdateDTO);
        if(updatedUser.isPresent()){
            return ResponseEntity.ok(updatedUser);
        }
        return ResponseEntity.status(404).body("User not found");
    }
}
