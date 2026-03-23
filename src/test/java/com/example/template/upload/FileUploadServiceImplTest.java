package com.example.template.upload;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.template.repository.ProcessedUploadRecordRepository;
import com.example.template.repository.entity.ProcessedUploadRecordEntity;
import com.example.template.upload.dto.UploadMetadataRequest;
import com.example.template.upload.service.impl.FileUploadServiceImpl;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class FileUploadServiceImplTest {

    @Mock
    private ProcessedUploadRecordRepository processedUploadRecordRepository;

    @Test
    void processShouldPersistValidRowsAndReturnFailures() {
        var filePart = new FilePart() {
            @Override public String filename() { return "sample.csv"; }
            @Override public String name() { return "file"; }
            @Override public reactor.core.publisher.Mono<Void> transferTo(java.nio.file.Path dest) { return Mono.empty(); }
            @Override public org.springframework.http.HttpHeaders headers() { return new org.springframework.http.HttpHeaders(); }
            @Override public Flux<org.springframework.core.io.buffer.DataBuffer> content() {
                return Flux.just(new DefaultDataBufferFactory().wrap("Jane,jane@example.com\nBadLine\nJohn,john@example.com".getBytes(StandardCharsets.UTF_8)));
            }
        };

        when(processedUploadRecordRepository.save(any(ProcessedUploadRecordEntity.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        var service = new FileUploadServiceImpl(processedUploadRecordRepository);

        StepVerifier.create(service.process(filePart, new UploadMetadataRequest("tester", "crm")))
                .expectNextMatches(response -> response.successCount() == 2 && response.failureCount() == 1)
                .verifyComplete();
    }
}
