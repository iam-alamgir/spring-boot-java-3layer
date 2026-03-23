package com.example.template.repository.entity;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("processed_upload_record")
public record ProcessedUploadRecordEntity(
        @Id UUID id,
        @Column("uploaded_by") String uploadedBy,
        @Column("source_system") String sourceSystem,
        @Column("line_number") int lineNumber,
        @Column("raw_line") String rawLine,
        @Column("processed_name") String processedName,
        @Column("processed_email") String processedEmail,
        @Column("created_at") OffsetDateTime createdAt
) {
}
