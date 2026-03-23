package com.example.template.upload.dto;

import jakarta.validation.constraints.NotBlank;

public record UploadMetadataRequest(@NotBlank String uploadedBy, @NotBlank String sourceSystem) {
}
