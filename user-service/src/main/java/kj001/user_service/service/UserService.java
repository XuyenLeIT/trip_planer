package kj001.user_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
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


import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    //Phương thức lấy 1 user
    public UserResponseDTO getOneUser(Long userId) {
        Optional<User> existingUser = userRepository.findById(userId);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            //Chuyển đổi User sang UserResponseDTO
            UserResponseDTO userResponseDTO = objectMapper.convertValue(user, UserResponseDTO.class);

            //Lấy thông tin từ profile
            Profile profile = user.getProfile();
            if (profile != null) {
                ProfileDTO profileDTO = new ProfileDTO();
                profileDTO.setId(profile.getId());
                profileDTO.setHobbies(profile.getHobbies());
                profileDTO.setAddress(profile.getAddress());
                profileDTO.setAge(profile.getAge());
                profileDTO.setAvatar(profile.getAvatar());
                profileDTO.setDescription(profile.getDescription());
                profileDTO.setMaritalStatus(profile.getMaritalStatus());

                //Thêm ProfileDTO và UserResponseDTO
                userResponseDTO.setProfileDTO(profileDTO);
            }
            return userResponseDTO;
        }
        return null;
    }

    //Phương thức Register User
    @Transactional
    public UserResponseDTO createUser(CreateUserDTO createUserDTO) throws MessagingException, IOException {
        //Kiểm tra nếu có bất kỳ user nào với email đang acive
        List<User> existingActiveUsers = userRepository.findByEmailAndIsActiveTrue(createUserDTO.getEmail());
        if (!existingActiveUsers.isEmpty()) {
            throw new RuntimeException("UserActiveExists");
        }
        //Lấy thời gian hiện tại
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endDay = now.toLocalDate().atTime(23, 59, 59);

        //Tìm người dùng có cùng email và đã đăng ký trong ngày
        List<User> existingUserToday = userRepository
                .findByEmailAndRegistrationDateBetween(createUserDTO.getEmail(), startDay, endDay);
        //Kiểm tra với email đó trong ngày đã đăng ký mấy lần
        if (existingUserToday.size() >= 3) {
            throw new RuntimeException("EmailAlready3TimeOfDay");
        }
        //Xóa những bản ghi cũ cho email đã đăng ký ngày trước hôm nay mà chưa active
        userRepository.deleteByEmailAndRegistrationDateBeforeAndIsActiveFalse(createUserDTO.getEmail(), startDay);

        //Kiểm tra newPass và ConfirmPass có trùng nhau hay không
        if (!createUserDTO.getPassword().equals(createUserDTO.getConfirmPassword())) {
            throw new RuntimeException("ConfirmEqualNewPassword");
        }
        User user = objectMapper.convertValue(createUserDTO, User.class);  //Chuyển đổi đối tượng createUserDTO<dữ liệu người dùng nhập vào> sang User để lưu vào dữ liệu . Điều này có nghĩa là dữ liệu createUserDTO sẽ được sao chép vào User và lưu vào database vì DTO sẽ không trực tiếp lưu vào DB
        user.setPassword(passwordEncoder.encode(createUserDTO.getPassword()));  //Mã hóa password trước khi lưu vào DB

        String otpCode = generateOtp();   //Tạo mã OTP
        user.setOtpCode(otpCode);
        user.setCreateAt(LocalDateTime.now());    //Thiết lập time tạo user
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES);   //Set Thời gian hết hạn
        user.setExpiryTime(expiryTime);
        userRepository.save(user);
        UserResponseDTO userResponseDTO = objectMapper.convertValue(user, UserResponseDTO.class);

        //Logic gửi mail khi user đăng ký tài khoản và xác thực bằng OTP bằng template
        String templatePath = "templates/otp_email_template.html";
        String emailTemplate = new String(Files.readAllBytes(Paths.get(templatePath)));
        String emailContent = emailTemplate.replace("[EMAIL]", createUserDTO.getEmail())
                                           .replace("[OTP_CODE]", otpCode);
        MailEntity mailEntity = new MailEntity();
        mailEntity.setEmail(createUserDTO.getEmail());
        mailEntity.setSubject("Verify Account");
        mailEntity.setContent(emailContent);
        mailResetPass.sendMailOTP(mailEntity);
        return userResponseDTO;
    }

    //VerifyAccount
    public boolean verifyAccount(String email, String code) {
        Optional<User> user = userRepository.findByEmailAndOtpCode(email, code);
        if (user.isPresent()) {
            //Nếu đã verify mà user tiếp tục verify thì báo lỗi
            if (user.get().isActive()) {
                throw new RuntimeException("OTPVERIFIED");
            }
            //Kiểm tra OTP đã hết hạn chưa
            if (user.get().getExpiryTime().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("OTPHasExpired");
            }
            user.get().setActive(true);    //KHi user đã xác thực thì đặt trạng thái isActive = true
            userRepository.save(user.get());

            // Xóa các bản ghi khác có cùng email nhưng không được kích hoạt
            userRepository.deleteInactiveUsersByEmail(email, user.get().getId());
            return true;
        }
        return false;

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

    //Phương thức delete User
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

    //Phương thức Change Password
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

    //Phương thức gửi OTP
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
        otp.setSendAttemps(existingOTP.map(otp1 -> otp1.getSendAttemps() + 1).orElse(1));    //Số lần gửi OTP sẽ được tăng lên
        otp.setExpiryTime(expiryTime);
        otp.setLastSend(LocalDateTime.now());
        otpRepository.save(otp);

        //Login send mail mã OTP
        MailEntity mailEntity = new MailEntity();
        mailEntity.setEmail(sendOtpRequestDTO.getEmail());
        mailEntity.setSubject(otp.getSubject());
        mailEntity.setContent(otp.getContent());
        mailResetPass.sendMailOTP(mailEntity);
    }

    //phương thức tạo mã OTP ngẫu nhiên
    private String generateOtp() {
        Random random = new Random();
        //Tạo ngẫu nhiên 6 chữ số
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    //Logic ResetPassword
    public boolean resetPassword(VerifyOTPRequestDTO resetPasswordRequestDTO) throws MessagingException, UnsupportedEncodingException {
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
        mailResetPass.sendMailOTP(mailEntity);
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
