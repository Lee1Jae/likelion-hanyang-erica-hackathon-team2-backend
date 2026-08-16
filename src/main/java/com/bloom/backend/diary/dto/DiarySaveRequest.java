package com.bloom.backend.diary.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record DiarySaveRequest(
        @Size(max = 1000) String memo,
        @DecimalMin("0.0") @DecimalMax("5.0") @Digits(integer = 1, fraction = 1) BigDecimal condition,
        @DecimalMin("20.0") @DecimalMax("300.0") @Digits(integer = 3, fraction = 1) BigDecimal weight,
        @Min(0) @Max(10000) Integer waterIntake,
        @Size(max = 30) String skinCondition,
        Boolean menstrualStatus
) {}
