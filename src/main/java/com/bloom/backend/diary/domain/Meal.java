package com.bloom.backend.diary.domain;

import com.bloom.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "meals")
public class Meal extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "diary_id", nullable = false)
    private Diary diary;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false, length = 20)
    private MealType mealType;

    @Column(name = "food_name", nullable = false, length = 100)
    private String foodName;

    @Column
    private Integer calories;

    @Column
    private Integer carbs;

    @Column
    private Integer protein;

    @Column
    private Integer fat;

    protected Meal() {}

    public Meal(Diary diary, MealType mealType, String foodName, Integer calories, Integer carbs, Integer protein, Integer fat) {
        this.diary = diary;
        update(mealType, foodName, calories, carbs, protein, fat);
    }

    public void update(MealType mealType, String foodName, Integer calories, Integer carbs, Integer protein, Integer fat) {
        this.mealType = mealType;
        this.foodName = foodName;
        this.calories = calories;
        this.carbs = carbs;
        this.protein = protein;
        this.fat = fat;
    }

    public Long getId() { return id; }
    public Diary getDiary() { return diary; }
    public MealType getMealType() { return mealType; }
    public String getFoodName() { return foodName; }
    public Integer getCalories() { return calories; }
    public Integer getCarbs() { return carbs; }
    public Integer getProtein() { return protein; }
    public Integer getFat() { return fat; }
}
