package com.bloom.backend.mileage.repository;

import com.bloom.backend.mileage.domain.MileageWallet;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface MileageWalletRepository extends JpaRepository<MileageWallet, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<MileageWallet> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}
