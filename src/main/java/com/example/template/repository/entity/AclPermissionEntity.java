package com.example.template.repository.entity;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("acl_permission")
public record AclPermissionEntity(
        @Id UUID id,
        @Column("user_id") UUID userId,
        @Column("resource_type") String resourceType,
        @Column("resource_key") String resourceKey,
        @Column("action") String action,
        @Column("granted") boolean granted,
        @Column("created_at") OffsetDateTime createdAt
) {
}
