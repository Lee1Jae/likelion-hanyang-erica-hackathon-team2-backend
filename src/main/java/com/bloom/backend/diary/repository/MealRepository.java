package com.bloom.backend.diary.repository;

import com.bloom.backend.diary.domain.Meal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealRepository extends JpaRepository<Meal, Long> {
    List<Meal> findAllByDiaryIdOrderByIdAsc(Long diaryId);
    List<Meal> findAllByDiaryIdInOrderByDiaryIdAscIdAsc(List<Long> diaryIds);
    Optional<Meal> findByIdAndDiaryUserId(Long id, Long userId);
    List<Meal> findAllByDiaryUserIdAndDiaryDate(Long userId, LocalDate date);
    void deleteAllByDiaryUserId(Long userId);
}
