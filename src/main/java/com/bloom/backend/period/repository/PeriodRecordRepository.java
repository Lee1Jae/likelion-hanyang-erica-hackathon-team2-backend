package com.bloom.backend.period.repository;

import com.bloom.backend.period.domain.PeriodRecord;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PeriodRecordRepository extends JpaRepository<PeriodRecord, Long> {
    List<PeriodRecord> findAllByUserIdOrderByStartDateDescIdDesc(Long userId);
    Optional<PeriodRecord> findByIdAndUserId(Long id, Long userId);
    void deleteAllByUserId(Long userId);
}
