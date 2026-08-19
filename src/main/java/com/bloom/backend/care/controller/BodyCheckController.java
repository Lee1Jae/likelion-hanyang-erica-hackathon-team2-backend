package com.bloom.backend.care.controller;

import com.bloom.backend.care.dto.BodyCheckCreateRequest;
import com.bloom.backend.care.dto.BodyCheckPatchRequest;
import com.bloom.backend.care.dto.BodyCheckResponse;
import com.bloom.backend.care.service.BodyCheckAnalysisService;
import com.bloom.backend.care.service.BodyCheckService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/care/body-checks")
public class BodyCheckController {
    private final BodyCheckService bodyCheckService;
    private final BodyCheckAnalysisService bodyCheckAnalysisService;

    public BodyCheckController(BodyCheckService bodyCheckService,
                               BodyCheckAnalysisService bodyCheckAnalysisService) {
        this.bodyCheckService = bodyCheckService;
        this.bodyCheckAnalysisService = bodyCheckAnalysisService;
    }

    @PostMapping
    public ResponseEntity<BodyCheckResponse> create(Authentication auth,
            @Valid @RequestBody BodyCheckCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bodyCheckService.create(userId(auth), request));
    }

    @GetMapping
    public List<BodyCheckResponse> getAll(Authentication auth) {
        return bodyCheckService.getAll(userId(auth));
    }

    @GetMapping("/{bodyCheckId}")
    public BodyCheckResponse get(Authentication auth, @PathVariable Long bodyCheckId) {
        return bodyCheckService.get(userId(auth), bodyCheckId);
    }

    @PatchMapping("/{bodyCheckId}")
    public BodyCheckResponse patch(Authentication auth, @PathVariable Long bodyCheckId,
            @Valid @RequestBody BodyCheckPatchRequest request) {
        return bodyCheckService.patch(userId(auth), bodyCheckId, request);
    }

    @DeleteMapping("/{bodyCheckId}")
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable Long bodyCheckId) {
        bodyCheckService.delete(userId(auth), bodyCheckId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{bodyCheckId}/analysis")
    public BodyCheckResponse analyze(Authentication auth, @PathVariable Long bodyCheckId) {
        return bodyCheckAnalysisService.analyze(userId(auth), bodyCheckId);
    }

    private Long userId(Authentication authentication) {
        return Long.valueOf(authentication.getName());
    }
}
