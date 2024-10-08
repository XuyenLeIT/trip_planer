package kj001.user_service.controllers;

import kj001.user_service.dtos.CreateUserDTO;
import kj001.user_service.dtos.LoginRequestDTO;
import kj001.user_service.dtos.UserResponseDTO;
import kj001.user_service.models.User;
import kj001.user_service.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    @PostMapping("/register")
    public ResponseEntity<?> createOneUser(@RequestBody CreateUserDTO createUserDTO){
        User createdUser  = userService.createUser(createUserDTO);
        return ResponseEntity.ok(createdUser);
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequestDTO){
        Optional<UserResponseDTO> user = userService.login(loginRequestDTO);
        if(user.isPresent()){
            return ResponseEntity.ok(user.get());
        }
        return ResponseEntity.status(404).body(null);
    }
}
