package kj001.user_service.service;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kj001.user_service.dtos.*;
import kj001.user_service.helpers.FileUpload;
import kj001.user_service.models.Profile;
import kj001.user_service.models.User;
import kj001.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import javax.swing.text.html.Option;
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
   private  PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private String subFolder = "avatarImage";
    String uploadFolder = "uploads";
    private String rootUrl = "http://localhost:8080/";
    private String urlImage = rootUrl + uploadFolder + File.separator + subFolder;

   public List<UserResponseDTO> getAllUser() {
       List<User> users = userRepository.findAll();
       return users.stream().map(user -> objectMapper.convertValue(user, UserResponseDTO.class))
                                                     .collect(Collectors.toList());
   }

   public User createUser(CreateUserDTO createUserDTO) {
       User user = objectMapper.convertValue(createUserDTO, User.class);
       user.setPassword(passwordEncoder.encode(createUserDTO.getPassword()));  //Mã hóa password
       user.setCreateAt(LocalDateTime.now());    //Set time tạo user
       return userRepository.save(user);
   }

    public Optional<UserResponseDTO> login(LoginRequestDTO loginRequestDTO){
        Optional<User> user = userRepository.findByEmail(loginRequestDTO.getEmail());
        if (user.isPresent() && passwordEncoder.matches(loginRequestDTO.getPassword(), user.get().getPassword())){
            return Optional.of(convertToUserResponseDTO(user.get()));
        }
        return Optional.empty();

    }

    public Optional<UserResponseDTO> updateUser(Long userId, UserAndProfileUpdateDTO userAndProfileUpdateDTO) throws IOException {
       Optional<User> existingUser = userRepository.findById(userId);
       if (existingUser.isPresent()) {
           User user = existingUser.get();

           //Sử dụng objectMapper để update dữ liệu dựa trên UserAndProfileUpdateDTO
           objectMapper.updateValue(user,userAndProfileUpdateDTO);

           //Xử lý thông tin profile
           Profile profile= user.getProfile();
           if (profile == null) {
               profile = new Profile();   //Tạo 1 đối tượng rỗng
               profile.setUser(user);
           }
           //Xử lý Upload avatar nếu có
           MultipartFile avatarFile = userAndProfileUpdateDTO.getFile();
           if (avatarFile != null && !avatarFile.isEmpty()) {
               String avatarUrl = profile.getAvatar();
               if (avatarUrl != null) {
                   fileUpload.deleteImage(avatarUrl.substring(rootUrl.length()));
               }

               //Lưu file và lấy file
               String imageName = fileUpload.storeImage(subFolder,avatarFile);

               //Tạo đường dẫn đầy đủ cho file
               String exacImagePath = urlImage + File.separator + imageName;

               //Cập nhật URL của hình đại diện trong profile
               userAndProfileUpdateDTO.setAvatar(exacImagePath.replace("\\","/"));
           }else {
               userAndProfileUpdateDTO.setAvatar(profile.getAvatar());
           }
           //Cập nhật các thông tin profile từ DTO
           objectMapper.updateValue(profile,userAndProfileUpdateDTO);
           user.setProfile(profile);

           //Lưu thông tin User và Profile
           userRepository.save(user);
           return Optional.of(convertToUserResponseDTO(user));
       }
       return Optional.empty();
   }

    private UserResponseDTO convertToUserResponseDTO(User user) {
       UserResponseDTO userResponseDTO = objectMapper.convertValue(user, UserResponseDTO.class);
      if (user.getProfile() != null) {
          ProfileDTO profileDTO = objectMapper.convertValue(user.getProfile(), ProfileDTO.class);
          profileDTO.setAvatar(user.getProfile().getAvatar());
          userResponseDTO.setProfileDTO(profileDTO);
      }
       return userResponseDTO;
    }
 }
