package com.bloom.backend.ai.nutrition.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class DraftFoodPatchRequest {
    @Size(min = 1, max = 100) private String foodName;
    @PositiveOrZero private BigDecimal amount;
    @Size(max = 20) private String amountUnit;
    @PositiveOrZero private Integer kcal;
    @PositiveOrZero private Integer carbs;
    @PositiveOrZero private Integer protein;
    @PositiveOrZero private Integer fat;
    private boolean foodNamePresent;
    private boolean amountPresent;
    private boolean amountUnitPresent;
    private boolean kcalPresent;
    private boolean carbsPresent;
    private boolean proteinPresent;
    private boolean fatPresent;

    @JsonSetter("foodName") public void setFoodName(String value) { foodNamePresent = true; foodName = value; }
    @JsonSetter("amount") public void setAmount(BigDecimal value) { amountPresent = true; amount = value; }
    @JsonSetter("amountUnit") public void setAmountUnit(String value) { amountUnitPresent = true; amountUnit = value; }
    @JsonSetter("kcal") public void setKcal(Integer value) { kcalPresent = true; kcal = value; }
    @JsonSetter("carbs") public void setCarbs(Integer value) { carbsPresent = true; carbs = value; }
    @JsonSetter("protein") public void setProtein(Integer value) { proteinPresent = true; protein = value; }
    @JsonSetter("fat") public void setFat(Integer value) { fatPresent = true; fat = value; }

    public String foodName() { return foodName; }
    public BigDecimal amount() { return amount; }
    public String amountUnit() { return amountUnit; }
    public Integer kcal() { return kcal; }
    public Integer carbs() { return carbs; }
    public Integer protein() { return protein; }
    public Integer fat() { return fat; }
    public boolean foodNamePresent() { return foodNamePresent; }
    public boolean amountPresent() { return amountPresent; }
    public boolean amountUnitPresent() { return amountUnitPresent; }
    public boolean kcalPresent() { return kcalPresent; }
    public boolean carbsPresent() { return carbsPresent; }
    public boolean proteinPresent() { return proteinPresent; }
    public boolean fatPresent() { return fatPresent; }
    public boolean hasChanges() {
        return foodNamePresent || amountPresent || amountUnitPresent || kcalPresent || carbsPresent
                || proteinPresent || fatPresent;
    }
}
