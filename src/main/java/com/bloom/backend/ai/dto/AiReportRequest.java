package com.bloom.backend.ai.dto;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
public record AiReportRequest(@NotNull LocalDate from, @NotNull LocalDate to) {}
