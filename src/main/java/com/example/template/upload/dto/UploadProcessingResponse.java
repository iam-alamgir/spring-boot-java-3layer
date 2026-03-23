package com.example.template.upload.dto;

import java.util.List;

public record UploadProcessingResponse(int totalLines, int successCount, int failureCount,
                                       List<UploadSuccess> processed, List<UploadFailure> failed) {
}
