package com.bloom.backend.upload.dto;

public record ImageUploadResponse(String imageUrl, String contentType, long size) {}
