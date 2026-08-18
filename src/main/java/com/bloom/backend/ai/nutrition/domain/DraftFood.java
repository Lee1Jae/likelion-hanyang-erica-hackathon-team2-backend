package com.bloom.backend.ai.nutrition.domain;

import com.bloom.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "nutrition_draft_foods")
public class DraftFood extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "analysis_id", nullable = false)
    private NutritionAnalysis analysis;
    @Column(name = "food_name", nullable = false, length = 100)
    private String foodName;
    @Column(precision = 8, scale = 1)
    private BigDecimal amount;
    @Column(name = "amount_unit", length = 20)
    private String amountUnit;
    private Integer kcal;
    private Integer carbs;
    private Integer protein;
    private Integer fat;
    @Column(precision = 4, scale = 3)
    private BigDecimal confidence;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private NutritionSource source;

    protected DraftFood() {}
    public DraftFood(NutritionAnalysis analysis, String foodName, BigDecimal amount, String amountUnit,
                     Integer kcal, Integer carbs, Integer protein, Integer fat,
                     BigDecimal confidence, NutritionSource source) {
        this.analysis = analysis; this.confidence = confidence; this.source = source;
        update(foodName, amount, amountUnit, kcal, carbs, protein, fat);
    }
    public void update(String foodName, BigDecimal amount, String amountUnit,
                       Integer kcal, Integer carbs, Integer protein, Integer fat) {
        if (foodName != null) this.foodName = foodName;
        if (amount != null) this.amount = amount;
        if (amountUnit != null) this.amountUnit = amountUnit;
        if (kcal != null) this.kcal = kcal;
        if (carbs != null) this.carbs = carbs;
        if (protein != null) this.protein = protein;
        if (fat != null) this.fat = fat;
    }
    public void patchFoodName(String value) { this.foodName = value; }
    public void patchAmount(BigDecimal value) { this.amount = value; }
    public void patchAmountUnit(String value) { this.amountUnit = value; }
    public void patchKcal(Integer value) { this.kcal = value; }
    public void patchCarbs(Integer value) { this.carbs = value; }
    public void patchProtein(Integer value) { this.protein = value; }
    public void patchFat(Integer value) { this.fat = value; }
    public Long getId() { return id; }
    public String getFoodName() { return foodName; }
    public BigDecimal getAmount() { return amount; }
    public String getAmountUnit() { return amountUnit; }
    public Integer getKcal() { return kcal; }
    public Integer getCarbs() { return carbs; }
    public Integer getProtein() { return protein; }
    public Integer getFat() { return fat; }
    public BigDecimal getConfidence() { return confidence; }
    public NutritionSource getSource() { return source; }
}
