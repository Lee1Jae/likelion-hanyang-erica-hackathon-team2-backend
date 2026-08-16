package com.bloom.backend.user.dto;

import jakarta.validation.constraints.Size;

public record DeleteUserRequest(@Size(max = 500) String reason) {}
