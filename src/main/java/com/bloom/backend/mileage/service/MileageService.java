package com.bloom.backend.mileage.service;

import com.bloom.backend.diary.repository.ActivityRepository;
import com.bloom.backend.global.error.*;
import com.bloom.backend.mileage.domain.*;
import com.bloom.backend.mileage.dto.*;
import com.bloom.backend.mileage.repository.*;
import com.bloom.backend.user.domain.User;
import com.bloom.backend.user.repository.UserRepository;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MileageService {
    private static final int ATTENDANCE_REWARD = 100;
    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");
    private static final Map<Integer, Integer> STREAK_REWARDS = Map.of(3, 100, 7, 300, 14, 500);
    private final MileageWalletRepository walletRepository;
    private final MileageHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;

    public MileageService(MileageWalletRepository walletRepository, MileageHistoryRepository historyRepository,
                          UserRepository userRepository, ActivityRepository activityRepository) {
        this.walletRepository = walletRepository; this.historyRepository = historyRepository;
        this.userRepository = userRepository; this.activityRepository = activityRepository;
    }

    public MileageBalanceResponse balance(Long userId) {
        ensureUser(userId);
        return new MileageBalanceResponse(walletRepository.findByUserId(userId)
                .map(MileageWallet::getBalance).orElse(0));
    }

    public List<MileageHistoryResponse> history(Long userId) {
        ensureUser(userId);
        return historyRepository.findAllByUserIdOrderByIdDesc(userId).stream()
                .map(MileageHistoryResponse::from).toList();
    }

    @Transactional
    public MileageRewardResponse attendance(Long userId) {
        LocalDate today = LocalDate.now(SERVICE_ZONE);
        String reference = "ATTENDANCE:" + today;
        MileageWallet wallet = walletForUpdate(userId);
        if (historyRepository.existsByUserIdAndReferenceId(userId, reference)) {
            return new MileageRewardResponse(false, 0, wallet.getBalance(), "ALREADY_REWARDED", null);
        }
        earn(userId, wallet, MileageReason.ATTENDANCE, ATTENDANCE_REWARD, reference);
        return new MileageRewardResponse(true, ATTENDANCE_REWARD, wallet.getBalance(), "ATTENDANCE", null);
    }

    @Transactional
    public MileageRewardResponse checkStreak(Long userId) {
        LocalDate today = LocalDate.now(SERVICE_ZONE);
        int streak = calculateStreak(activityRepository.findExerciseDates(userId, today), today);
        MileageWallet wallet = walletForUpdate(userId);
        Optional<Integer> milestone = STREAK_REWARDS.keySet().stream().filter(value -> streak >= value)
                .sorted(Comparator.reverseOrder())
                .filter(value -> !historyRepository.existsByUserIdAndReferenceId(userId, "ROUTINE_STREAK:" + value))
                .findFirst();
        if (milestone.isEmpty()) {
            return new MileageRewardResponse(false, 0, wallet.getBalance(), "NO_NEW_REWARD", streak);
        }
        int days = milestone.get();
        int amount = STREAK_REWARDS.get(days);
        MileageReason reason = MileageReason.valueOf("ROUTINE_STREAK_" + days);
        earn(userId, wallet, reason, amount, "ROUTINE_STREAK:" + days);
        return new MileageRewardResponse(true, amount, wallet.getBalance(), reason.name(), streak);
    }

    private int calculateStreak(List<LocalDate> dates, LocalDate today) {
        Set<LocalDate> exercised = new HashSet<>(dates);
        int streak = 0;
        for (LocalDate date = today; exercised.contains(date); date = date.minusDays(1)) streak++;
        return streak;
    }

    private void earn(Long userId, MileageWallet wallet, MileageReason reason, int amount, String reference) {
        wallet.credit(amount);
        User user = ensureUser(userId);
        historyRepository.save(new MileageHistory(user, MileageType.EARN, reason, amount,
                wallet.getBalance(), reference));
    }

    private MileageWallet walletForUpdate(Long userId) {
        return walletRepository.findByUserIdForUpdate(userId).orElseGet(() ->
                walletRepository.save(new MileageWallet(ensureUser(userId))));
    }

    private User ensureUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
