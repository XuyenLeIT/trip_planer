package kj001.user_service.services;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kj001.user_service.dtos.*;
import kj001.user_service.helpers.FileUpload;
import kj001.user_service.models.Profile;
import kj001.user_service.models.User;
import kj001.user_service.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final FileUpload fileUpload;
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private String subFolder = "avatarImage";
    String uploadFolder = "uploads";
    private String rootUrl = "http://localhost:8080/";
    private String urlImage = rootUrl + uploadFolder + File.separator + subFolder;
    public List<UserResponseDTO> getAllUser(){
        List<User> users = userRepository.findAll();
        return users.stream().map(user->objectMapper.convertValue(user,UserResponseDTO.class))
                                                    .collect(Collectors.toList());
    }
    public User createUser(CreateUserDTO createUserDTO){
        User user = objectMapper.convertValue(createUserDTO,User.class);
        user.setPassword(passwordEncoder.encode(createUserDTO.getPassword()));
        user.setCreateAt(LocalDateTime.now());
        return userRepository.save(user);
    }
    public Optional<UserResponseDTO> login(LoginRequestDTO loginRequestDTO){
        Optional<User> user = userRepository.findByEmail(loginRequestDTO.getEmail());
        if(user.isPresent() && passwordEncoder
                .matches(loginRequestDTO.getPassword(), user.get().getPassword())){
            return Optional.of(convertToUserResponseDTO(user.get()));
        }
        return Optional.empty();
    }

    public Optional<UserResponseDTO> updateUser(Long userId,
                                                UserAndProfileUpdateDTO userAndProfileUpdateDTO) throws IOException {
        Optional<User> exsitingUser = userRepository.findById(userId);
        if(exsitingUser.isPresent()){
            User user = exsitingUser.get();
        //Su dung objectMapper de update du lieu user dua tren userAndProfileUpdateDTO
            objectMapper.updateValue(user,userAndProfileUpdateDTO);
        //Xu ly thong tin profile
            Profile profile = user.getProfile();
            if(profile ==  null){
                profile = new Profile();
                profile.setUser(user);
            }
        // Xu ly upload avatar neu co
            MultipartFile avatarFile = userAndProfileUpdateDTO.getFile();
            if(avatarFile != null && !avatarFile.isEmpty()){
                String avatarUrl = profile.getAvatar();
                if(avatarUrl != null){
                    fileUpload.deleteImage(avatarUrl.substring(rootUrl.length()));
                }
            //Luu file va lay ten file
                String imageName = fileUpload.storeImage(subFolder,avatarFile);
            //Tao duong dan day du cho file
                String exactImagePath = urlImage + File.separator + imageName;
            // Cập nhật URL của hình đại diện trong profile
                userAndProfileUpdateDTO.setAvatar(exactImagePath.replace("\\","/"));
            }else{
                userAndProfileUpdateDTO.setAvatar(profile.getAvatar());
            }
        // Cập nhật các thông tin profile tư DTO
            objectMapper.updateValue(profile,userAndProfileUpdateDTO);
            user.setProfile(profile);
        //Lưu thông tin User va Profile
            userRepository.save(user);
            return Optional.of(convertToUserResponseDTO(user));
        }
        return Optional.empty();
    }
    private UserResponseDTO convertToUserResponseDTO(User user){
        UserResponseDTO userResponseDTO = objectMapper.convertValue(user,UserResponseDTO.class);
        if(user.getProfile() != null){
            ProfileDTO profileDTO = objectMapper.convertValue(user.getProfile(),ProfileDTO.class);
            profileDTO.setAvatar(user.getProfile().getAvatar());
            userResponseDTO.setProfileDTO(profileDTO);
        }
        return  userResponseDTO;
    }
}
