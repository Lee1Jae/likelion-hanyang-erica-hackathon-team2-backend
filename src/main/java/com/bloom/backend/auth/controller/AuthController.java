package com.bloom.backend.auth.controller;

import com.bloom.backend.auth.dto.LoginRequest;
import com.bloom.backend.auth.dto.RefreshTokenRequest;
import com.bloom.backend.auth.dto.SignupRequest;
import com.bloom.backend.auth.dto.SignupResponse;
import com.bloom.backend.auth.dto.SessionAction;
import com.bloom.backend.auth.dto.SessionRequest;
import com.bloom.backend.auth.dto.TokenResponse;
import com.bloom.backend.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        SignupResponse response = authService.signup(request);
        return ResponseEntity.created(URI.create("/api/v1/users/" + response.userId())).body(response);
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @Operation(summary = "토큰 재발급")
    @PostMapping("/reissue")
    public TokenResponse reissue(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.reissue(request.refreshToken());
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            Authentication authentication,
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        authService.logout(Long.valueOf(authentication.getName()), request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "세션 관리", description = "REISSUE는 새 토큰을 반환하고 LOGOUT은 204를 반환합니다.")
    @PostMapping("/session")
    public ResponseEntity<?> manageSession(
            Authentication authentication,
            @Valid @RequestBody SessionRequest request
    ) {
        if (request.action() == SessionAction.REISSUE) {
            return ResponseEntity.ok(authService.reissue(request.refreshToken()));
        }
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }
        authService.logout(Long.valueOf(authentication.getName()), request.refreshToken());
        return ResponseEntity.noContent().build();
    }
}
