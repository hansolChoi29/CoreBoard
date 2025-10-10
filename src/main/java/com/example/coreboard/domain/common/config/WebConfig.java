package com.example.coreboard.domain.common.config;

import com.example.coreboard.domain.common.interceptor.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 인터셉터 만들기 : 체인에 끼워 넣어야 모든 요청이 들어오기 전에 가로챌 수 있음
        registry.addInterceptor(new AuthInterceptor())
                // 기본적으로 api경로의 모든 요청 검사 - REST API 진입점은 api.
                .addPathPatterns("/api/**")
                // auth과 에러는 예외 - 아직 토큰이 없거나 새로 발급 받아야 하는 구간이기 때문
                .excludePathPatterns(
                        "/api/auth/**",
                        "/error"
                );
    }
}
