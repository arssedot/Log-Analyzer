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
    public ProfileDto getProfile(User currentUser) {
        User managedUser = userRepository.findById(currentUser.getId()).orElse(currentUser);
        return toDto(managedUser);
    }

    @Transactional
    public ProfileDto updateProfile(User currentUser, UpdateRequest updateRequest) {
        User managedUser = userRepository.findById(currentUser.getId()).orElseThrow();
        if (updateRequest.displayName() != null) {
            managedUser.setDisplayName(updateRequest.displayName().isBlank() ? null : updateRequest.displayName().strip());
        }
        if (updateRequest.language() != null && List.of("en", "ru").contains(updateRequest.language())) {
            managedUser.setLanguage(updateRequest.language());
        }
        return toDto(userRepository.save(managedUser));
    }

    @Transactional
    public ProfileDto uploadAvatar(User currentUser, MultipartFile file) throws IOException {
        User managedUser = userRepository.findById(currentUser.getId()).orElseThrow();
        String mimeType = Objects.requireNonNullElse(file.getContentType(), "image/jpeg");
        String dataUri = "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(file.getBytes());
        managedUser.setAvatarData(dataUri);
        return toDto(userRepository.save(managedUser));
    }

    @Transactional
    public ProfileDto removeAvatar(User currentUser) {
        User managedUser = userRepository.findById(currentUser.getId()).orElseThrow();
        managedUser.setAvatarData(null);
        return toDto(userRepository.save(managedUser));
    }

    private static ProfileDto toDto(User user) {
        return new ProfileDto(user.getEmail(), user.getDisplayName(), user.getLanguage(), user.getAvatarData());
    }
}
