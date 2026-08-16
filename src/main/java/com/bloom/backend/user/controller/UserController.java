package com.bloom.backend.user.controller;

import com.bloom.backend.user.dto.DeleteUserRequest;
import com.bloom.backend.user.dto.OnboardingRequest;
import com.bloom.backend.user.dto.ProfilePatchRequest;
import com.bloom.backend.user.dto.ProfileResponse;
import com.bloom.backend.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/onboarding")
    public ProfileResponse onboarding(Authentication auth, @Valid @RequestBody OnboardingRequest request) {
        return userService.onboarding(userId(auth), request);
    }

    @GetMapping("/users/me/profile")
    public ProfileResponse profile(Authentication auth) {
        return userService.getProfile(userId(auth));
    }

    @PatchMapping("/users/me/profile")
    public ProfileResponse patchProfile(Authentication auth, @Valid @RequestBody ProfilePatchRequest request) {
        return userService.patchProfile(userId(auth), request);
    }

    @DeleteMapping("/users/me")
    public ResponseEntity<Void> delete(Authentication auth, @Valid @RequestBody(required = false) DeleteUserRequest request) {
        userService.deleteUser(userId(auth));
        return ResponseEntity.noContent().build();
    }

    private Long userId(Authentication authentication) {
        return Long.valueOf(authentication.getName());
    }
}
