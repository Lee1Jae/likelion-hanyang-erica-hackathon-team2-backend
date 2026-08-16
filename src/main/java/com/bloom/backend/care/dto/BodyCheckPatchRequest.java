package com.bloom.backend.care.dto;

import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record BodyCheckPatchRequest(
        LocalDate recordedDate,
        @Size(min = 1, max = 1000) String originalImageUrl
) {}
