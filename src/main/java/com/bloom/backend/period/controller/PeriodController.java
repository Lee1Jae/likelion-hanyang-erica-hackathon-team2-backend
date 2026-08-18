package com.bloom.backend.period.controller;

import com.bloom.backend.period.dto.*;
import com.bloom.backend.period.service.PeriodService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/periods")
public class PeriodController {
    private final PeriodService periodService;
    public PeriodController(PeriodService periodService) { this.periodService = periodService; }

    @PostMapping
    public ResponseEntity<PeriodResponse> create(Authentication auth, @Valid @RequestBody PeriodCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(periodService.create(userId(auth), request));
    }

    @GetMapping
    public List<PeriodResponse> getAll(Authentication auth) { return periodService.getAll(userId(auth)); }

    @PatchMapping("/{periodId}")
    public PeriodResponse patch(Authentication auth, @PathVariable Long periodId,
                                @Valid @RequestBody PeriodPatchRequest request) {
        return periodService.patch(userId(auth), periodId, request);
    }

    @DeleteMapping("/{periodId}")
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable Long periodId) {
        periodService.delete(userId(auth), periodId);
        return ResponseEntity.noContent().build();
    }

    private Long userId(Authentication authentication) { return Long.valueOf(authentication.getName()); }
}
