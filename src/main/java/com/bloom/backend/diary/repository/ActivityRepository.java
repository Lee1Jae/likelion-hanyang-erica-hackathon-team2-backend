package com.bloom.backend.diary.repository;

import com.bloom.backend.diary.domain.Activity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findAllByDiaryIdOrderByIdAsc(Long diaryId);
    List<Activity> findAllByDiaryIdInOrderByDiaryIdAscIdAsc(List<Long> diaryIds);
    Optional<Activity> findByIdAndDiaryUserId(Long id, Long userId);
    List<Activity> findAllByDiaryUserIdAndDiaryDate(Long userId, LocalDate date);
    void deleteAllByDiaryUserId(Long userId);

    @Query("select distinct a.diary.date from Activity a where a.diary.user.id = :userId " +
            "and (a.exerciseMinutes > 0 or a.burnedKcal > 0) and a.diary.date <= :today order by a.diary.date desc")
    List<LocalDate> findExerciseDates(@Param("userId") Long userId, @Param("today") LocalDate today);
}
