package com.example.coreboard.domain.common.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import static org.junit.jupiter.api.Assertions.*;

@ContextConfiguration(classes = WebConfig.class)
@ExtendWith(SpringExtension.class)
class WebConfigTest {

    @Test
    void addInterceptors() {
        WebConfig web = new WebConfig();
        web.addInterceptors(new InterceptorRegistry());
    }
}