package com.bloom.backend.user.domain;

import com.bloom.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "user_profiles")
public class UserProfile extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

    @Column(name = "height_cm", precision = 5, scale = 1)
    private BigDecimal heightCm;

    @Column(name = "weight_kg", precision = 5, scale = 1)
    private BigDecimal weightKg;

    @Column(name = "last_period_date")
    private LocalDate lastPeriodDate;

    @Column(name = "cycle_length")
    private Integer cycleLength;

    @Column(name = "beauty_goals", columnDefinition = "TEXT")
    private String beautyGoals;

    @Column(name = "health_issues", columnDefinition = "TEXT")
    private String healthIssues;

    @Column(name = "onboarding_completed", nullable = false)
    private boolean onboardingCompleted;

    protected UserProfile() {
    }

    public UserProfile(User user) {
        this.user = user;
        this.onboardingCompleted = false;
    }

    public void completeOnboarding(LocalDate birthDate, LocalDate deliveryDate, BigDecimal heightCm,
                                   BigDecimal weightKg, String beautyGoals, String healthIssues,
                                   LocalDate lastPeriodDate, Integer cycleLength) {
        this.birthDate = birthDate;
        this.deliveryDate = deliveryDate;
        this.heightCm = heightCm;
        this.weightKg = weightKg;
        this.beautyGoals = beautyGoals;
        this.healthIssues = healthIssues;
        this.lastPeriodDate = lastPeriodDate;
        this.cycleLength = cycleLength;
        this.onboardingCompleted = true;
    }

    public void update(BigDecimal heightCm, BigDecimal weightKg, String beautyGoals, String healthIssues,
                       LocalDate lastPeriodDate, Integer cycleLength) {
        if (heightCm != null) this.heightCm = heightCm;
        if (weightKg != null) this.weightKg = weightKg;
        if (beautyGoals != null) this.beautyGoals = beautyGoals;
        if (healthIssues != null) this.healthIssues = healthIssues;
        if (lastPeriodDate != null) this.lastPeriodDate = lastPeriodDate;
        if (cycleLength != null) this.cycleLength = cycleLength;
    }

    public LocalDate getBirthDate() { return birthDate; }
    public LocalDate getDeliveryDate() { return deliveryDate; }
    public BigDecimal getHeightCm() { return heightCm; }
    public BigDecimal getWeightKg() { return weightKg; }
    public LocalDate getLastPeriodDate() { return lastPeriodDate; }
    public Integer getCycleLength() { return cycleLength; }
    public String getBeautyGoals() { return beautyGoals; }
    public String getHealthIssues() { return healthIssues; }
    public boolean isOnboardingCompleted() { return onboardingCompleted; }
}
