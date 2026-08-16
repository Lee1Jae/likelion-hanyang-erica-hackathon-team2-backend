package com.bloom.backend.care.repository;

import com.bloom.backend.care.domain.BodyCheck;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BodyCheckRepository extends JpaRepository<BodyCheck, Long> {
    List<BodyCheck> findAllByUserIdOrderByRecordedDateDescIdDesc(Long userId);
    Optional<BodyCheck> findByIdAndUserId(Long id, Long userId);
    void deleteAllByUserId(Long userId);
}
