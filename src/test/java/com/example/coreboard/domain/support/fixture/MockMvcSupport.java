package com.example.coreboard.domain.support.fixture;

import com.example.coreboard.domain.common.exception.GlobalExceptionHandler;
import com.example.coreboard.domain.common.interceptor.AuthInterceptor;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;

public class MockMvcSupport {
    private MockMvcSupport() {
    }

    public static MockMvc create(Object controller) {
        return MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    public static MockMvc createWithInterceptor(Object controller) {
        return MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .addInterceptors(new AuthInterceptor())
                .build();
    }
}
