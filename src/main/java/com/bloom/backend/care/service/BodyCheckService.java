package com.bloom.backend.care.service;

import com.bloom.backend.care.domain.BodyCheck;
import com.bloom.backend.care.dto.BodyCheckCreateRequest;
import com.bloom.backend.care.dto.BodyCheckPatchRequest;
import com.bloom.backend.care.dto.BodyCheckResponse;
import com.bloom.backend.care.repository.BodyCheckRepository;
import com.bloom.backend.global.error.BusinessException;
import com.bloom.backend.global.error.ErrorCode;
import com.bloom.backend.user.domain.User;
import com.bloom.backend.user.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BodyCheckService {
    private final BodyCheckRepository bodyCheckRepository;
    private final UserRepository userRepository;

    public BodyCheckService(BodyCheckRepository bodyCheckRepository, UserRepository userRepository) {
        this.bodyCheckRepository = bodyCheckRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public BodyCheckResponse create(Long userId, BodyCheckCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return BodyCheckResponse.from(bodyCheckRepository.save(
                new BodyCheck(user, request.recordedDate(), request.originalImageUrl())));
    }

    public List<BodyCheckResponse> getAll(Long userId) {
        return bodyCheckRepository.findAllByUserIdOrderByRecordedDateDescIdDesc(userId).stream()
                .map(BodyCheckResponse::from).toList();
    }

    public BodyCheckResponse get(Long userId, Long bodyCheckId) {
        return BodyCheckResponse.from(find(userId, bodyCheckId));
    }

    @Transactional
    public BodyCheckResponse patch(Long userId, Long bodyCheckId, BodyCheckPatchRequest request) {
        if (!request.hasChanges()) {
            throw new BusinessException(ErrorCode.BODY_CHECK_PATCH_EMPTY);
        }
        BodyCheck bodyCheck = find(userId, bodyCheckId);
        bodyCheck.update(request.recordedDate(), request.originalImageUrl());
        return BodyCheckResponse.from(bodyCheck);
    }

    @Transactional
    public void delete(Long userId, Long bodyCheckId) {
        bodyCheckRepository.delete(find(userId, bodyCheckId));
    }

    private BodyCheck find(Long userId, Long bodyCheckId) {
        return bodyCheckRepository.findByIdAndUserId(bodyCheckId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BODY_CHECK_NOT_FOUND));
    }
}
