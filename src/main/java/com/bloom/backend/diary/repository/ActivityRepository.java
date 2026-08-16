package com.bloom.backend.diary.repository;

import com.bloom.backend.diary.domain.Activity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findAllByDiaryIdOrderByIdAsc(Long diaryId);
    List<Activity> findAllByDiaryIdInOrderByDiaryIdAscIdAsc(List<Long> diaryIds);
    Optional<Activity> findByIdAndDiaryUserId(Long id, Long userId);
    List<Activity> findAllByDiaryUserIdAndDiaryDate(Long userId, LocalDate date);
    void deleteAllByDiaryUserId(Long userId);
}
