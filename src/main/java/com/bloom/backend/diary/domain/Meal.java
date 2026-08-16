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

    @Column(nullable = false)
    private int calories;

    @Column(nullable = false)
    private int carbs;

    @Column(nullable = false)
    private int protein;

    @Column(nullable = false)
    private int fat;

    protected Meal() {}

    public Meal(Diary diary, MealType mealType, String foodName, int calories, int carbs, int protein, int fat) {
        this.diary = diary;
        update(mealType, foodName, calories, carbs, protein, fat);
    }

    public void update(MealType mealType, String foodName, int calories, int carbs, int protein, int fat) {
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
    public int getCalories() { return calories; }
    public int getCarbs() { return carbs; }
    public int getProtein() { return protein; }
    public int getFat() { return fat; }
}
