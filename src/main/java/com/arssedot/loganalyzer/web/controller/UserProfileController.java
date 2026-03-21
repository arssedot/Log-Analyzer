package com.arssedot.loganalyzer.web.controller;

import com.arssedot.loganalyzer.domain.User;
import com.arssedot.loganalyzer.service.UserProfileService;
import com.arssedot.loganalyzer.service.UserProfileService.ProfileDto;
import com.arssedot.loganalyzer.service.UserProfileService.UpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private static final long MAX_AVATAR_BYTES = 2 * 1024 * 1024; // 2 MB

    private final UserProfileService profileService;

    @GetMapping
    public ProfileDto get(@AuthenticationPrincipal User user) {
        return profileService.getProfile(user);
    }

    @PutMapping
    public ProfileDto update(@AuthenticationPrincipal User user,
                             @RequestBody UpdateRequest request) {
        return profileService.updateProfile(user, request);
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProfileDto uploadAvatar(@AuthenticationPrincipal User user,
                                   @RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) throw new IllegalArgumentException("No file provided");
        if (file.getSize() > MAX_AVATAR_BYTES) throw new IllegalArgumentException("Avatar must be under 2 MB");
        return profileService.uploadAvatar(user, file);
    }

    @DeleteMapping("/avatar")
    public ProfileDto removeAvatar(@AuthenticationPrincipal User user) {
        return profileService.removeAvatar(user);
    }
}
