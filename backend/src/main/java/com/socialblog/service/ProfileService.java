package com.socialblog.service;

import com.socialblog.model.entity.User;
import com.socialblog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {

    private final UserRepository userRepository;

    private final String UPLOAD_FOLDER = "src/main/resources/static/uploads_avatar/";

    // =================== LƯU FILE ===================
    private String saveFile(MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return null;
            }

            File dir = new File(UPLOAD_FOLDER);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String original = file.getOriginalFilename();
            String fileName = System.currentTimeMillis() + "_" + original;

            Path path = Paths.get(UPLOAD_FOLDER + fileName);
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            return fileName;

        } catch (Exception e) {
            log.error("Lỗi lưu file: ", e);
            throw new RuntimeException("Không thể lưu file");
        }
    }

    // =================== UPDATE INFO ===================
    public void updateUserInfo(User user, String fullName, String bio, String address,
            LocalDate dob, String gender) {

        user.setFullName(fullName);
        user.setBio(bio);
        user.setAddress(address);
        user.setDateOfBirth(dob);

        if (gender != null && !gender.isEmpty()) {
            user.setGender(Enum.valueOf(com.socialblog.model.enums.Gender.class, gender));
        }

        userRepository.save(user);
    }

    // =================== UPDATE AVATAR ===================
    public void updateAvatar(User user, MultipartFile avatarFile) {

        String url = saveFile(avatarFile);

        if (url != null) {
            user.setAvatarUrl(url);
            userRepository.save(user);
        }
    }

    // =================== UPDATE COVER ===================
    public void updateCover(User user, MultipartFile coverFile) {

        String url = saveFile(coverFile);

        if (url != null) {
            user.setCoverUrl(url);
            userRepository.save(user);
        }
    }
}
