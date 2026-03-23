package com.example.template.security;

import java.security.Principal;
import java.util.UUID;

public record AuthenticatedUser(UUID userId, String username) implements Principal {
    @Override
    public String getName() {
        return username;
    }
}
