package com.example.template.logging;

import com.example.template.config.ApplicationProperties;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class LogSanitizer {

    private final ApplicationProperties properties;

    public LogSanitizer(ApplicationProperties properties) {
        this.properties = properties;
    }

    public String sanitize(String payload) {
        if (payload == null || payload.isBlank()) {
            return payload;
        }
        var masked = payload;
        for (String field : properties.logging().sensitiveFields()) {
            masked = Pattern.compile("(\\\"" + field + "\\\"\\s*:\\s*\\\")(.*?)(\\\")", Pattern.CASE_INSENSITIVE)
                    .matcher(masked)
                    .replaceAll("$1***$3");
        }
        return masked;
    }
}
