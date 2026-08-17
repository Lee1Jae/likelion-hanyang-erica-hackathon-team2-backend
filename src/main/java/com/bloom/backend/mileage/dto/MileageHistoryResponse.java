package com.bloom.backend.mileage.dto;

import com.bloom.backend.mileage.domain.*;
import java.time.Instant;

public record MileageHistoryResponse(Long mileageHistoryId, MileageType type, MileageReason reason,
        int amount, int balanceAfter, Instant createdAt) {
    public static MileageHistoryResponse from(MileageHistory history) {
        return new MileageHistoryResponse(history.getId(), history.getType(), history.getReason(),
                history.getAmount(), history.getBalanceAfter(), history.getCreatedAt());
    }
}
