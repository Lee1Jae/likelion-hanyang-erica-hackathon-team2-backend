package com.bloom.backend.diary.domain;

import com.bloom.backend.global.entity.BaseTimeEntity;
import com.bloom.backend.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "diaries", uniqueConstraints = @UniqueConstraint(name = "uk_diaries_user_date", columnNames = {"user_id", "record_date"}))
public class Diary extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "record_date", nullable = false)
    private LocalDate date;

    @Column(length = 1000)
    private String memo;

    @Column(name = "condition_score", precision = 2, scale = 1)
    private BigDecimal conditionScore;

    @Column(name = "weight_kg", precision = 5, scale = 1)
    private BigDecimal weightKg;

    @Column(name = "water_ml")
    private Integer waterMl;

    @Column(name = "skin_condition", length = 30)
    private String skinCondition;

    @Column(name = "menstrual_status")
    private Boolean menstrualStatus;

    @Column(length = 30)
    private String mood;

    private Integer stress;

    private Integer fatigue;

    @Column(name = "skin_conditions", length = 500)
    private String skinConditions;

    @Column(name = "period_start")
    private LocalDate periodStart;

    @Column(name = "period_end")
    private LocalDate periodEnd;

    protected Diary() {}

    public Diary(User user, LocalDate date) {
        this.user = user;
        this.date = date;
    }

    public void update(String memo, BigDecimal conditionScore, BigDecimal weightKg, Integer waterMl,
                       String skinCondition, Boolean menstrualStatus) {
        this.memo = memo;
        this.conditionScore = conditionScore;
        this.weightKg = weightKg;
        this.waterMl = waterMl;
        this.skinCondition = skinCondition;
        this.menstrualStatus = menstrualStatus;
    }

    public void patchDaily(BigDecimal weightKg, String mood, Integer stress, Integer fatigue,
                           Integer waterMl, String skinConditions, LocalDate periodStart,
                           LocalDate periodEnd, String note) {
        if (weightKg != null) this.weightKg = weightKg;
        if (mood != null) this.mood = mood;
        if (stress != null) this.stress = stress;
        if (fatigue != null) this.fatigue = fatigue;
        if (waterMl != null) this.waterMl = waterMl;
        if (skinConditions != null) this.skinConditions = skinConditions;
        if (periodStart != null) this.periodStart = periodStart;
        if (periodEnd != null) this.periodEnd = periodEnd;
        if (note != null) this.memo = note;
    }

    public Long getId() { return id; }
    public LocalDate getDate() { return date; }
    public String getMemo() { return memo; }
    public BigDecimal getConditionScore() { return conditionScore; }
    public BigDecimal getWeightKg() { return weightKg; }
    public Integer getWaterMl() { return waterMl; }
    public String getSkinCondition() { return skinCondition; }
    public Boolean getMenstrualStatus() { return menstrualStatus; }
    public String getMood() { return mood; }
    public Integer getStress() { return stress; }
    public Integer getFatigue() { return fatigue; }
    public String getSkinConditions() { return skinConditions; }
    public LocalDate getPeriodStart() { return periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
}
