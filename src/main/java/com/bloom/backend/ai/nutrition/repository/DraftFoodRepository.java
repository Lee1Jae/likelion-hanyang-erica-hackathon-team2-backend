package com.bloom.backend.ai.nutrition.repository;
import com.bloom.backend.ai.nutrition.domain.DraftFood;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface DraftFoodRepository extends JpaRepository<DraftFood, Long> {
    List<DraftFood> findAllByAnalysisIdOrderByIdAsc(Long analysisId);
    Optional<DraftFood> findByIdAndAnalysisId(Long id, Long analysisId);
    void deleteAllByAnalysisId(Long analysisId);
    void deleteAllByAnalysisIdIn(List<Long> analysisIds);
}
