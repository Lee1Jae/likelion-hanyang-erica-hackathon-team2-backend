package com.bloom.backend.upload.controller;

import com.bloom.backend.upload.domain.ImagePurpose;
import com.bloom.backend.upload.domain.UploadedImage;
import com.bloom.backend.upload.dto.ImageUploadResponse;
import com.bloom.backend.upload.service.ImageUploadService;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/uploads/images")
public class ImageUploadController {
    private final ImageUploadService imageUploadService;

    public ImageUploadController(ImageUploadService imageUploadService) {
        this.imageUploadService = imageUploadService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageUploadResponse> upload(Authentication auth,
            @RequestPart("file") MultipartFile file, @RequestParam ImagePurpose purpose) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(imageUploadService.upload(userId(auth), file, purpose));
    }

    @GetMapping("/{imageId}")
    public ResponseEntity<byte[]> get(Authentication auth, @PathVariable Long imageId) {
        UploadedImage image = imageUploadService.get(userId(auth), imageId);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(image.getContentType()))
                .cacheControl(CacheControl.noStore()).body(image.getData());
    }

    private Long userId(Authentication authentication) { return Long.valueOf(authentication.getName()); }
}
