package com.example.template.repository.entity;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("app_user")
public record UserEntity(
        @Id UUID id,
        @Column("username") String username,
        @Column("password_hash") String passwordHash,
        @Column("display_name") String displayName,
        @Column("enabled") boolean enabled,
        @Column("created_at") OffsetDateTime createdAt,
        @Column("updated_at") OffsetDateTime updatedAt
) {
}
