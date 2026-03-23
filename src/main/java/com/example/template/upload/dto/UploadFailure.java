package com.example.template.upload.dto;

public record UploadFailure(int lineNumber, String rawLine, String reason) {
}
