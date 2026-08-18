package com.bloom.backend.period.service;

import com.bloom.backend.global.error.BusinessException;
import com.bloom.backend.global.error.ErrorCode;
import com.bloom.backend.period.domain.PeriodRecord;
import com.bloom.backend.period.dto.*;
import com.bloom.backend.period.repository.PeriodRecordRepository;
import com.bloom.backend.user.domain.User;
import com.bloom.backend.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PeriodService {
    private final PeriodRecordRepository periodRepository;
    private final UserRepository userRepository;

    public PeriodService(PeriodRecordRepository periodRepository, UserRepository userRepository) {
        this.periodRepository = periodRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public PeriodResponse create(Long userId, PeriodCreateRequest request) {
        validateRange(request.startDate(), request.endDate());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return PeriodResponse.from(periodRepository.save(new PeriodRecord(user, request.startDate(), request.endDate())));
    }

    public List<PeriodResponse> getAll(Long userId) {
        return periodRepository.findAllByUserIdOrderByStartDateDescIdDesc(userId).stream()
                .map(PeriodResponse::from).toList();
    }

    @Transactional
    public PeriodResponse patch(Long userId, Long periodId, PeriodPatchRequest request) {
        if (!request.hasChanges()) throw new BusinessException(ErrorCode.PERIOD_PATCH_EMPTY);
        PeriodRecord record = find(userId, periodId);
        LocalDate startDate = request.startDate() == null ? record.getStartDate() : request.startDate();
        LocalDate endDate = request.endDate() == null ? record.getEndDate() : request.endDate();
        validateRange(startDate, endDate);
        record.patch(request.startDate(), request.endDate());
        return PeriodResponse.from(record);
    }

    @Transactional
    public void delete(Long userId, Long periodId) { periodRepository.delete(find(userId, periodId)); }

    private PeriodRecord find(Long userId, Long periodId) {
        return periodRepository.findByIdAndUserId(periodId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PERIOD_NOT_FOUND));
    }

    private void validateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) throw new BusinessException(ErrorCode.DATE_RANGE_INVALID);
    }
}
