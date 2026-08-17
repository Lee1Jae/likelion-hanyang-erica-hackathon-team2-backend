package com.bloom.backend.mileage.controller;

import com.bloom.backend.mileage.dto.*;
import com.bloom.backend.mileage.service.MileageService;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/mileage")
public class MileageController {
    private final MileageService mileageService;
    public MileageController(MileageService mileageService) { this.mileageService = mileageService; }
    @GetMapping public MileageBalanceResponse balance(Authentication auth) { return mileageService.balance(userId(auth)); }
    @GetMapping("/history") public List<MileageHistoryResponse> history(Authentication auth) { return mileageService.history(userId(auth)); }
    @PostMapping("/attendance") public MileageRewardResponse attendance(Authentication auth) { return mileageService.attendance(userId(auth)); }
    @PostMapping("/routine-streak/check") public MileageRewardResponse streak(Authentication auth) { return mileageService.checkStreak(userId(auth)); }
    private Long userId(Authentication authentication) { return Long.valueOf(authentication.getName()); }
}
