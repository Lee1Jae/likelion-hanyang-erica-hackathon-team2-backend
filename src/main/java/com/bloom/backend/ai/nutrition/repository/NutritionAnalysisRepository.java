package com.bloom.backend.ai.nutrition.repository;
import com.bloom.backend.ai.nutrition.domain.NutritionAnalysis;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
public interface NutritionAnalysisRepository extends JpaRepository<NutritionAnalysis, Long> {
    Optional<NutritionAnalysis> findByIdAndUserId(Long id, Long userId);
    void deleteAllByUserId(Long userId);
}
