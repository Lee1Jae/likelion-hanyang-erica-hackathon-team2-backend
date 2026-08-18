package com.bloom.backend.ai.repository;
import com.bloom.backend.ai.domain.AiReport;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AiReportRepository extends JpaRepository<AiReport, Long> {
    Optional<AiReport> findByIdAndUserId(Long id, Long userId);
    Optional<AiReport> findFirstByUserIdOrderByCreatedAtDesc(Long userId);
    void deleteAllByUserId(Long userId);
}
