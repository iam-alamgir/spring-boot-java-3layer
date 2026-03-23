package com.example.template.upload.service.impl;

import com.example.template.repository.ProcessedUploadRecordRepository;
import com.example.template.repository.entity.ProcessedUploadRecordEntity;
import com.example.template.upload.dto.UploadFailure;
import com.example.template.upload.dto.UploadMetadataRequest;
import com.example.template.upload.dto.UploadProcessingResponse;
import com.example.template.upload.dto.UploadSuccess;
import com.example.template.upload.service.FileUploadService;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class FileUploadServiceImpl implements FileUploadService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private final ProcessedUploadRecordRepository processedUploadRecordRepository;

    public FileUploadServiceImpl(ProcessedUploadRecordRepository processedUploadRecordRepository) {
        this.processedUploadRecordRepository = processedUploadRecordRepository;
    }

    @Override
    public Mono<UploadProcessingResponse> process(FilePart filePart, UploadMetadataRequest metadata) {
        return DataBufferUtils.join(filePart.content())
                .map(buffer -> {
                    var bytes = new byte[buffer.readableByteCount()];
                    buffer.read(bytes);
                    DataBufferUtils.release(buffer);
                    return new String(bytes, StandardCharsets.UTF_8);
                })
                .flatMap(content -> {
                    var lines = List.of(content.split("\\R"));
                    var successes = new ArrayList<UploadSuccess>();
                    var failures = new ArrayList<UploadFailure>();
                    var entities = new ArrayList<ProcessedUploadRecordEntity>();

                    for (int index = 0; index < lines.size(); index++) {
                        var lineNumber = index + 1;
                        var rawLine = lines.get(index).trim();
                        if (rawLine.isBlank()) {
                            failures.add(new UploadFailure(lineNumber, rawLine, "Line is blank"));
                            continue;
                        }
                        var columns = rawLine.split(",");
                        if (columns.length < 2) {
                            failures.add(new UploadFailure(lineNumber, rawLine, "Expected name,email columns"));
                            continue;
                        }
                        var name = columns[0].trim();
                        var email = columns[1].trim();
                        if (name.isBlank()) {
                            failures.add(new UploadFailure(lineNumber, rawLine, "Name is blank"));
                        } else if (!EMAIL_PATTERN.matcher(email).matches()) {
                            failures.add(new UploadFailure(lineNumber, rawLine, "Email is invalid"));
                        } else {
                            successes.add(new UploadSuccess(lineNumber, name, email));
                            entities.add(new ProcessedUploadRecordEntity(
                                    UUID.randomUUID(), metadata.uploadedBy(), metadata.sourceSystem(), lineNumber,
                                    rawLine, name, email, OffsetDateTime.now(ZoneOffset.UTC)
                            ));
                        }
                    }

                    return Flux.fromIterable(entities)
                            .flatMap(processedUploadRecordRepository::save)
                            .then(Mono.just(new UploadProcessingResponse(lines.size(), successes.size(), failures.size(),
                                    List.copyOf(successes), List.copyOf(failures))));
                });
    }
}
