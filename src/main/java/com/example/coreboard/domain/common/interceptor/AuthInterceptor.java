package com.example.coreboard.domain.common.interceptor;

import com.example.coreboard.domain.common.exception.auth.AuthErrorCode;
import com.example.coreboard.domain.common.exception.auth.AuthErrorException;
import com.example.coreboard.domain.common.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authorization = request.getHeader("Authorization");

        if (authorization == null) {
            if ("GET".equals(request.getMethod())) {
                return true;
            }
            throw new AuthErrorException(AuthErrorCode.UNAUTHORIZED);
        }

        String accessToken = authorization
                .replaceFirst("(?i)^Bearer\\s+", "")
                .replaceAll("\\s+", "");

        
        if (!JwtUtil.validationToken(accessToken)) {
            throw new AuthErrorException(AuthErrorCode.UNAUTHORIZED);
        }
//
        String username = JwtUtil.getUsername(accessToken);
        request.setAttribute("username", username);
        return true;
    }
}
