package com.bloom.backend.mileage.repository;

import com.bloom.backend.mileage.domain.MileageHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MileageHistoryRepository extends JpaRepository<MileageHistory, Long> {
    List<MileageHistory> findAllByUserIdOrderByIdDesc(Long userId);
    boolean existsByUserIdAndReferenceId(Long userId, String referenceId);
    void deleteAllByUserId(Long userId);
}
