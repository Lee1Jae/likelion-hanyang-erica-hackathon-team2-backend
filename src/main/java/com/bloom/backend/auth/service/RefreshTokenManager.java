package com.bloom.backend.auth.service;

import com.bloom.backend.auth.domain.RefreshToken;
import com.bloom.backend.auth.repository.RefreshTokenRepository;
import com.bloom.backend.auth.security.JwtTokenProvider;
import com.bloom.backend.global.error.BusinessException;
import com.bloom.backend.global.error.ErrorCode;
import com.bloom.backend.user.domain.User;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenManager {

    private static final SecureRandom RANDOM = new SecureRandom();
    private final RefreshTokenRepository repository;
    private final JwtTokenProvider jwtTokenProvider;

    public RefreshTokenManager(RefreshTokenRepository repository, JwtTokenProvider jwtTokenProvider) {
        this.repository = repository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public String issue(User user) {
        byte[] bytes = new byte[48];
        RANDOM.nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        repository.save(new RefreshToken(
                user,
                hash(rawToken),
                Instant.now().plusSeconds(jwtTokenProvider.refreshTtlSeconds())
        ));
        return rawToken;
    }

    public User consume(String rawToken) {
        RefreshToken token = repository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_REFRESH_TOKEN_INVALID));
        Instant now = Instant.now();
        if (!token.isUsable(now)) {
            throw new BusinessException(ErrorCode.AUTH_REFRESH_TOKEN_INVALID);
        }
        token.revoke(now);
        return token.getUser();
    }

    public void revoke(String rawToken, Long authenticatedUserId) {
        RefreshToken token = repository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_REFRESH_TOKEN_INVALID));
        if (!token.getUser().getId().equals(authenticatedUserId)) {
            throw new BusinessException(ErrorCode.RESOURCE_FORBIDDEN);
        }
        token.revoke(Instant.now());
    }

    private String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
