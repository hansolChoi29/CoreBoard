package com.example.coreboard.domain.post.support;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class ViewerKeyGenerator {
    private static final String IP_PREFIX = "ip:";
    private static final String X_FORWARDED_FOR = "X-Forwarded-For";

    public String generate(HttpServletRequest request) {
        String forwardedFor = request.getHeader(X_FORWARDED_FOR);

        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return IP_PREFIX + forwardedFor.split(",")[0].trim();
        }

        return IP_PREFIX + request.getRemoteAddr();
    }
}
