package com.bloom.backend.period.dto;

import com.bloom.backend.period.domain.PeriodRecord;
import java.time.LocalDate;

public record PeriodResponse(Long periodId, LocalDate startDate, LocalDate endDate) {
    public static PeriodResponse from(PeriodRecord record) {
        return new PeriodResponse(record.getId(), record.getStartDate(), record.getEndDate());
    }
}
