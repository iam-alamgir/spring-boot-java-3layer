package com.example.template.repository.entity;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("customer")
public record CustomerEntity(
        @Id UUID id,
        @Column("external_ref") String externalRef,
        @Column("full_name") String fullName,
        @Column("email") String email,
        @Column("created_at") OffsetDateTime createdAt,
        @Column("updated_at") OffsetDateTime updatedAt
) {
}
