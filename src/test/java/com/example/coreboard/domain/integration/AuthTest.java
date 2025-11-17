package com.example.coreboard.domain.integration;

import com.example.coreboard.domain.common.config.EmailPhoneNumberManager;
import com.example.coreboard.domain.common.config.PasswordManager;
import com.example.coreboard.domain.common.interceptor.AuthInterceptor;
import com.example.coreboard.domain.users.entity.Users;
import com.example.coreboard.domain.users.repository.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@Testcontainers
@Transactional
public class AuthTest {

    @Container
    static MySQLContainer mysql = new MySQLContainer("mysql:8.0.36")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @MockitoBean
    AuthInterceptor authInterceptor;

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    PasswordManager passwordEncode;

    @Autowired
    EmailPhoneNumberManager emailPhoneNumberEncode;

    @Autowired
    MockMvc mockMvc;

    @DynamicPropertySource
    static void overrideDataSourceProps(
            DynamicPropertyRegistry registry
    ) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @BeforeEach
    void setUser() {
        Users user = new Users(
                "username",
                passwordEncode.encrypt("password"),
                emailPhoneNumberEncode.encrypt("email@naver.com"),
                emailPhoneNumberEncode.encrypt("01012341234")
        );
        usersRepository.save(user);
    }

    @Test
    @DisplayName("POST/auth/users")
    void authUsers() throws Exception {
        String json = """
                {
                    "username" : "username1",
                    "password" : "password",
                    "confirmPassword" : "password",
                    "email" : "email@naver.com",
                    "phoneNumber" : "01012341234"
                }
                """;
        mockMvc.perform(
                        post("/auth/users")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("username1"));
        assertThat(usersRepository.count()).isEqualTo(2);
        Users created = usersRepository.findByUsername("username1").get();
        assertThat(created.getUsername()).isEqualTo("username1");
    }

    @Test
    @DisplayName("POST/auth/token")
    void authToken() throws Exception {
        String json = """
                {
                    "username" : "username",
                    "password" : "password"
                }
                """;

        mockMvc.perform(
                        post("/auth/token")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").exists());
        assertThat(usersRepository.count()).isEqualTo(1);
        Users token = usersRepository.findByUsername("username").get();
        assertThat(token.getUsername()).isEqualTo("username");
    }
}
