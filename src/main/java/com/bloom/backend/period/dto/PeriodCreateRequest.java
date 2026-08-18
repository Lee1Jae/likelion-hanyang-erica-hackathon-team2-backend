package com.bloom.backend.period.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record PeriodCreateRequest(@NotNull LocalDate startDate, @NotNull LocalDate endDate) {}
