package com.bloom.backend.diary.repository;

import com.bloom.backend.diary.domain.Diary;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiaryRepository extends JpaRepository<Diary, Long> {
    Optional<Diary> findByUserIdAndDate(Long userId, LocalDate date);
}
