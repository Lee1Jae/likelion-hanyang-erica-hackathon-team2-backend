package com.bloom.backend.ai.nutrition.domain;

import com.bloom.backend.diary.domain.MealType;
import com.bloom.backend.global.entity.BaseTimeEntity;
import com.bloom.backend.user.domain.User;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "nutrition_analyses")
public class NutritionAnalysis extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(name = "record_date", nullable = false)
    private LocalDate date;
    @Enumerated(EnumType.STRING) @Column(name = "meal_type", nullable = false, length = 20)
    private MealType mealType;
    @Enumerated(EnumType.STRING) @Column(name = "input_type", nullable = false, length = 20)
    private NutritionInputType inputType;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private NutritionAnalysisStatus status;
    @Column(name = "image_url", length = 1000)
    private String imageUrl;
    @Column(name = "input_text", columnDefinition = "TEXT")
    private String inputText;
    @Column(name = "model_version", length = 100)
    private String modelVersion;

    protected NutritionAnalysis() {}
    public NutritionAnalysis(User user, LocalDate date, MealType mealType, NutritionInputType inputType,
                             String imageUrl, String inputText, String modelVersion) {
        this.user = user; this.date = date; this.mealType = mealType; this.inputType = inputType;
        this.imageUrl = imageUrl; this.inputText = inputText; this.modelVersion = modelVersion;
        this.status = NutritionAnalysisStatus.DRAFT;
    }
    public void fail() { this.status = NutritionAnalysisStatus.FAILED; }
    public void draft() { this.status = NutritionAnalysisStatus.DRAFT; }
    public void record() { this.status = NutritionAnalysisStatus.RECORDED; }
    public Long getId() { return id; }
    public LocalDate getDate() { return date; }
    public MealType getMealType() { return mealType; }
    public NutritionAnalysisStatus getStatus() { return status; }
    public String getImageUrl() { return imageUrl; }
    public String getModelVersion() { return modelVersion; }
}
