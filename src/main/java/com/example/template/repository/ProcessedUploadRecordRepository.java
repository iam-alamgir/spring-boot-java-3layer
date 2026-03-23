package com.example.template.repository;

import com.example.template.repository.entity.ProcessedUploadRecordEntity;
import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ProcessedUploadRecordRepository extends ReactiveCrudRepository<ProcessedUploadRecordEntity, UUID> {
}
