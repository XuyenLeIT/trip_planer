package kj001.user_service.service;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import kj001.user_service.dtos.*;
import kj001.user_service.helpers.FileUpload;
import kj001.user_service.models.OTP;
import kj001.user_service.models.Profile;
import kj001.user_service.models.User;
import kj001.user_service.repository.OtpRepository;
import kj001.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import javax.swing.text.html.Option;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class UserService {
    private final UserRepository userRepository;
    private final OtpRepository otpRepository;
    private final MailResetPass mailResetPass;
    private final ObjectMapper objectMapper;   //Là 1 lớp thư viện trong java , nhằm để chuyển đổi dạng Object sang JSON
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();   //Mã hóa mật khẩu
    private final FileUpload fileUpload;
    private String subFolder = "avatarImage";
    String uploadFolder = "uploads";
    private String rootUrl = "http://localhost:8080/";
    private String urlImage = rootUrl + uploadFolder + File.separator + subFolder;
    private static final int OTP_EXPIRATION_MINUTES = 5;   //Đặt thời gian hết hạn cho mã OTP

    //Phương thức Show Data
    public List<UserResponseDTO> getAllUser() {
        List<User> users = userRepository.findAll();
        return users.stream().map(user -> objectMapper.convertValue(user, UserResponseDTO.class))
                .collect(Collectors.toList());
    }

    //Phương thức Register User
    public User createUser(CreateUserDTO createUserDTO) {
        User user = objectMapper.convertValue(createUserDTO, User.class);  //Chuyển đổi đối tượng createUserDTO<dữ liệu người dùng nhập vào> sang User để lưu vào dữ liệu . Điều này có nghĩa là dữ liệu createUserDTO sẽ được sao chép vào User và lưu vào database vì DTO sẽ không trực tiếp lưu vào DB
        user.setPassword(passwordEncoder.encode(createUserDTO.getPassword()));  //Mã hóa password
        user.setCreateAt(LocalDateTime.now());    //Thiết lập time tạo user
        return userRepository.save(user);
    }

    //Phương thức Login
    public Optional<UserResponseDTO> login(LoginRequestDTO loginRequestDTO) {
        Optional<User> user = userRepository.findByEmail(loginRequestDTO.getEmail());
        if (user.isPresent() && passwordEncoder.matches(loginRequestDTO.getPassword(), user.get().getPassword())) {
            return Optional.of(convertToUserResponseDTO(user.get()));
        }
        return Optional.empty();

    }

    //Phương thức Update User
    public Optional<UserResponseDTO> updateUser(Long userId, UserAndProfileUpdateDTO userAndProfileUpdateDTO) throws IOException {
        Optional<User> existingUser = userRepository.findById(userId);
        if (existingUser.isPresent()) {
            User user = existingUser.get();

            //Sử dụng objectMapper để update dữ liệu dựa trên UserAndProfileUpdateDTO
            objectMapper.updateValue(user, userAndProfileUpdateDTO);

            //Xử lý thông tin profile
            Profile profile = user.getProfile();
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
                String imageName = fileUpload.storeImage(subFolder, avatarFile);

                //Tạo đường dẫn đầy đủ cho file
                String exacImagePath = urlImage + File.separator + imageName;

                //Cập nhật URL của hình đại diện trong profile
                userAndProfileUpdateDTO.setAvatar(exacImagePath.replace("\\", "/"));
            } else {
                userAndProfileUpdateDTO.setAvatar(profile.getAvatar());
            }
            //Cập nhật các thông tin profile từ DTO
            objectMapper.updateValue(profile, userAndProfileUpdateDTO);
            user.setProfile(profile);

            //Lưu thông tin User và Profile
            userRepository.save(user);
            return Optional.of(convertToUserResponseDTO(user));
        }
        return Optional.empty();
    }

    public UserResponseDTO deleteUser(long userId) {
        Optional<User> existingUser = userRepository.findById(userId);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            userRepository.deleteById(userId);
            return objectMapper.convertValue(user, UserResponseDTO.class);
        }
        return null;
    }

    //Phương thức chuyển đổi đối tượng User sang UserResponseDTO
    private UserResponseDTO convertToUserResponseDTO(User user) {
        UserResponseDTO userResponseDTO = objectMapper.convertValue(user, UserResponseDTO.class);
        //Kiểm tra user có profile hay không
        if (user.getProfile() != null) {
            //Chuyển đổi đối tương profile sang profileDTO
            ProfileDTO profileDTO = objectMapper.convertValue(user.getProfile(), ProfileDTO.class);
            //Thiết lập avatar cho profileDTO
            profileDTO.setAvatar(user.getProfile().getAvatar());
            //Gán profileDTO vào userResponseDTO thông qua phương thức Set
            userResponseDTO.setProfileDTO(profileDTO);
        }
        return userResponseDTO;
    }

    public boolean changePassword(ChangePasswordRequestDTO changePasswordRequestDTO) {
        User user = userRepository.findByEmail(changePasswordRequestDTO.getEmail()).orElseThrow(() -> new RuntimeException("UserNotFound"));

        //Kiểm tra xem oldPassword có khớp hay không
        if (!passwordEncoder.matches(changePasswordRequestDTO.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("OldPasswordDoesNotMatch");
        }
        //KIểm tra xem newPassword có trùng với oldPassword hay không
        if (passwordEncoder.matches(changePasswordRequestDTO.getNewPassword(), user.getPassword())) {
            throw new RuntimeException("NewPasswordMustBeDifferenceFormOldPassword");
        }
        //KIểm tra xem newPassword có trùng với confirmPassword hay không
        if (!changePasswordRequestDTO.getNewPassword().equals(changePasswordRequestDTO.getConfirmPassword())) {
            throw new RuntimeException("ConfirmPasswordMustBeEqualNewPassword");
        }

        //Mã hóa newPassword và update
        user.setPassword(passwordEncoder.encode(changePasswordRequestDTO.getNewPassword()));
        userRepository.save(user);
        return true;
    }

    public void sendOTP(SendOtpRequestDTO sendOtpRequestDTO) throws MessagingException, UnsupportedEncodingException {
        userRepository.findByEmail(sendOtpRequestDTO.getEmail()).orElseThrow(() -> new RuntimeException("UserNotFound"));
        String otpCode = generateOtp();
        //Đặt thời gian hết hạn của OTP là 5 phút
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES);

        //Tìm OTP mới nhất được gửi cho mail này
        Optional<OTP> existingOTP = otpRepository.findLastOtpByEmail(sendOtpRequestDTO.getEmail());
        if (existingOTP.isPresent()) {
            OTP otpExisting = existingOTP.get();
            if (otpExisting.getLastSend().toLocalDate().isEqual(LocalDateTime.now().toLocalDate())) {
                if (otpExisting.getSendAttemps() >= 3) {
                    throw new RuntimeException("LimitOTPDay");
                }
            }
        }
        //Lưu mã OTP vào trong CSDL table OTP
        OTP otp = new OTP();
        otp.setOtpCode(otpCode);
        otp.setEmail(sendOtpRequestDTO.getEmail());
        otp.setSubject("Reset Password OTP");
        otp.setContent("Your OTP code is " + otpCode + ".It expries in 5 minutes");
        otp.setUsed(false);
        otp.setSendAttemps(existingOTP.map(otp1 -> otp1.getSendAttemps() + 1).orElse(1));    //Số lần gửi OTP
        otp.setExpiryTime(expiryTime);
        otp.setLastSend(LocalDateTime.now());
        otpRepository.save(otp);

        //Login send mail mã OTP
        MailEntity mailEntity = new MailEntity();
        mailEntity.setEmail(sendOtpRequestDTO.getEmail());
        mailEntity.setSubject(otp.getSubject());
        mailEntity.setContent(otp.getContent());
        mailResetPass.sendResetPass(mailEntity);
    }

    //phương thức tạo mã OTP ngẫu nhiên
    private String generateOtp() {
        Random random = new Random();
        //Tạo ngẫu nhiên 6 chữ số
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    //Logic ResetPassword
    public boolean resetPassword(ResetPasswordRequestDTO resetPasswordRequestDTO) throws MessagingException, UnsupportedEncodingException {
        //Tìm OTP theo email và mã OTP
        OTP otp = otpRepository.findByEmailAndOtpCode(resetPasswordRequestDTO.getEmail(),
                resetPasswordRequestDTO.getOtpCode()).orElseThrow(() -> new RuntimeException("UserNotFound"));

        //Kiểm tra OTP đã hết hạn chưa
        if (otp.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTPHasExpired");
        }
        //Kiểm tra nếu OTP đã được sử dụng hãy chưa
        if (otp.isUsed()) {
            throw new RuntimeException("OTPIsUsed");
        }
        //Tạo 1 random Password và gửi qua mail
        String newPassword = generateRandomPassword();
        User user = userRepository.findByEmail(resetPasswordRequestDTO.getEmail()).orElseThrow(() -> new RuntimeException("UserNotFound"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        //Đánh dấu là OTP đã được sử dụng
        otp.setUsed(true);
        otpRepository.save(otp);

        //Gửi newPassword qua mail
        MailEntity mailEntity = new MailEntity();
        mailEntity.setEmail(resetPasswordRequestDTO.getEmail());
        mailEntity.setSubject("Reset password OTP");
        mailEntity.setContent("Your password id: " + newPassword);
        mailResetPass.sendResetPass(mailEntity);
        return true;
    }

    //Tạo 1 hàm password random
    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }
}
