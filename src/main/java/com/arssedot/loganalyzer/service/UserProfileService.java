package com.arssedot.loganalyzer.service;

import com.arssedot.loganalyzer.domain.User;
import com.arssedot.loganalyzer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;

    public record ProfileDto(String email, String displayName, String language, String avatarData) {}
    public record UpdateRequest(String displayName, String language) {}

    @Transactional(readOnly = true)
    public ProfileDto getProfile(User user) {
        User u = userRepository.findById(user.getId()).orElse(user);
        return toDto(u);
    }

    @Transactional
    public ProfileDto updateProfile(User user, UpdateRequest req) {
        User u = userRepository.findById(user.getId()).orElseThrow();
        if (req.displayName() != null) {
            u.setDisplayName(req.displayName().isBlank() ? null : req.displayName().strip());
        }
        if (req.language() != null && List.of("en", "ru").contains(req.language())) {
            u.setLanguage(req.language());
        }
        return toDto(userRepository.save(u));
    }

    @Transactional
    public ProfileDto uploadAvatar(User user, MultipartFile file) throws IOException {
        User u = userRepository.findById(user.getId()).orElseThrow();
        String mime = Objects.requireNonNullElse(file.getContentType(), "image/jpeg");
        String dataUri = "data:" + mime + ";base64," + Base64.getEncoder().encodeToString(file.getBytes());
        u.setAvatarData(dataUri);
        return toDto(userRepository.save(u));
    }

    @Transactional
    public ProfileDto removeAvatar(User user) {
        User u = userRepository.findById(user.getId()).orElseThrow();
        u.setAvatarData(null);
        return toDto(userRepository.save(u));
    }

    private static ProfileDto toDto(User u) {
        return new ProfileDto(u.getEmail(), u.getDisplayName(), u.getLanguage(), u.getAvatarData());
    }
}
