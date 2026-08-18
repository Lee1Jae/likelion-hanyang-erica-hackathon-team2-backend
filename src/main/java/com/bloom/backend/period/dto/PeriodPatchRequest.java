package com.bloom.backend.period.dto;

import java.time.LocalDate;

public record PeriodPatchRequest(LocalDate startDate, LocalDate endDate) {
    public boolean hasChanges() { return startDate != null || endDate != null; }
}
