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

    public Long getId() { return id; }
    public LocalDate getDate() { return date; }
    public String getMemo() { return memo; }
    public BigDecimal getConditionScore() { return conditionScore; }
    public BigDecimal getWeightKg() { return weightKg; }
    public Integer getWaterMl() { return waterMl; }
    public String getSkinCondition() { return skinCondition; }
    public Boolean getMenstrualStatus() { return menstrualStatus; }
}
