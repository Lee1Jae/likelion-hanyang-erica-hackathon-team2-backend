package com.bloom.backend.diary.domain;

import com.bloom.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "activities")
public class Activity extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "diary_id", nullable = false)
    private Diary diary;

    @Column(nullable = false)
    private int steps;

    @Column(name = "exercise_minutes", nullable = false)
    private int exerciseMinutes;

    @Column(name = "burned_kcal", nullable = false)
    private int burnedKcal;

    @Column(length = 200)
    private String memo;

    protected Activity() {}

    public Activity(Diary diary, int steps, int exerciseMinutes, int burnedKcal, String memo) {
        this.diary = diary;
        update(steps, exerciseMinutes, burnedKcal, memo);
    }

    public void update(int steps, int exerciseMinutes, int burnedKcal, String memo) {
        this.steps = steps;
        this.exerciseMinutes = exerciseMinutes;
        this.burnedKcal = burnedKcal;
        this.memo = memo;
    }

    public Long getId() { return id; }
    public Diary getDiary() { return diary; }
    public int getSteps() { return steps; }
    public int getExerciseMinutes() { return exerciseMinutes; }
    public int getBurnedKcal() { return burnedKcal; }
    public String getMemo() { return memo; }
}
