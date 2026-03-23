package com.example.template.acl;

import com.example.template.repository.AclPermissionRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AclService {

    private final AclPermissionRepository aclPermissionRepository;

    public AclService(AclPermissionRepository aclPermissionRepository) {
        this.aclPermissionRepository = aclPermissionRepository;
    }

    public Mono<Boolean> hasPermission(UUID userId, PermissionTarget target) {
        return aclPermissionRepository.findByUserIdAndGrantedTrue(userId)
                .any(permission -> permission.resourceType().equals(target.resourceType())
                        && permission.resourceKey().equals(target.resourceKey())
                        && permission.action().equals(target.action()));
    }
}
