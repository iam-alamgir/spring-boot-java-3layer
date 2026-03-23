package com.example.template.upload.service;

import com.example.template.upload.dto.UploadMetadataRequest;
import com.example.template.upload.dto.UploadProcessingResponse;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

public interface FileUploadService {
    Mono<UploadProcessingResponse> process(FilePart filePart, UploadMetadataRequest metadata);
}
