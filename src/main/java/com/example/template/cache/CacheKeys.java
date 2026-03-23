package com.example.template.cache;

import java.util.UUID;

public final class CacheKeys {
    private CacheKeys() {
    }

    public static String accessToken(String tokenHash) {
        return "token:access:" + tokenHash;
    }

    public static String refreshToken(String tokenHash) {
        return "token:refresh:" + tokenHash;
    }

    public static String userTokens(UUID userId) {
        return "token:user:" + userId;
    }

    public static String aclPermissions(UUID userId) {
        return "acl:user:" + userId;
    }
}
