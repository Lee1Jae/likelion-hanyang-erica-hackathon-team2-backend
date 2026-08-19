package com.bloom.backend.mileage.repository;

import com.bloom.backend.mileage.domain.MileageWallet;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MileageWalletRepository extends JpaRepository<MileageWallet, Long> {
    Optional<MileageWallet> findByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select wallet from MileageWallet wallet where wallet.user.id = :userId")
    Optional<MileageWallet> findByUserIdForUpdate(@Param("userId") Long userId);

    void deleteByUserId(Long userId);
}
