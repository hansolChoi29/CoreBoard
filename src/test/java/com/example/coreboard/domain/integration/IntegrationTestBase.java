package com.example.coreboard.domain.integration;


import com.example.coreboard.domain.common.interceptor.AuthInterceptor;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional; 

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public abstract class IntegrationTestBase extends AbstractIntegrationTest{
    @MockitoBean
    AuthInterceptor authInterceptor;
}
