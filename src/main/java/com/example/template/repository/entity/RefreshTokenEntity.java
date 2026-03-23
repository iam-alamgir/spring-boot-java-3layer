package com.example.template.repository.entity;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("refresh_token")
public record RefreshTokenEntity(
        @Id UUID id,
        @Column("user_id") UUID userId,
        @Column("token_hash") String tokenHash,
        @Column("expires_at") OffsetDateTime expiresAt,
        @Column("revoked") boolean revoked,
        @Column("created_at") OffsetDateTime createdAt
) {
}
