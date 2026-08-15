package com.bloom.backend.auth.service;

import com.bloom.backend.auth.dto.LoginRequest;
import com.bloom.backend.auth.dto.SignupRequest;
import com.bloom.backend.auth.dto.SignupResponse;
import com.bloom.backend.auth.dto.TokenResponse;
import com.bloom.backend.auth.security.JwtTokenProvider;
import com.bloom.backend.global.error.BusinessException;
import com.bloom.backend.global.error.ErrorCode;
import com.bloom.backend.user.domain.User;
import com.bloom.backend.user.domain.UserProfile;
import com.bloom.backend.user.repository.UserProfileRepository;
import com.bloom.backend.user.repository.UserRepository;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenManager refreshTokenManager;

    public AuthService(
            UserRepository userRepository,
            UserProfileRepository profileRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            RefreshTokenManager refreshTokenManager
    ) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenManager = refreshTokenManager;
    }

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        User user = userRepository.save(new User(email, passwordEncoder.encode(request.password()), request.nickname()));
        profileRepository.save(new UserProfile(user));
        return new SignupResponse(user.getId(), user.getEmail(), user.getNickname());
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(normalizeEmail(request.email()))
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }
        return tokens(user);
    }

    @Transactional
    public TokenResponse reissue(String refreshToken) {
        User user = refreshTokenManager.consume(refreshToken);
        return tokens(user);
    }

    @Transactional
    public void logout(Long authenticatedUserId, String refreshToken) {
        refreshTokenManager.revoke(refreshToken, authenticatedUserId);
    }

    private TokenResponse tokens(User user) {
        return new TokenResponse(
                jwtTokenProvider.createAccessToken(user),
                refreshTokenManager.issue(user),
                jwtTokenProvider.accessTtlSeconds()
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
