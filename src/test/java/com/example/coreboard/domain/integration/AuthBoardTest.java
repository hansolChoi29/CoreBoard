package com.example.coreboard.domain.integration;

import com.example.coreboard.domain.common.config.EmailPhoneNumberEncode;
import com.example.coreboard.domain.common.config.PasswordEncode;
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

@SpringBootTest        // 스프링 부트 애플리케이션 전체를 테스트용으로 띄워서 실제처럼 컨테이너를 다 로드해주는 어노테이션
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false) // HTTP 요청 시뮬레이션 (가짜 서블릿 환경)
@Testcontainers       // Testcontainers 활성화
@Transactional        // 각 테스트가 끝나면 롤백해서 DB 깨끗하게 초기화
public class AuthBoardTest {

    @Container // Testcontainers가 이 필드를 테스트 시작 시 자동으로 Docker 컨테이너로 실행하게 함
    static MySQLContainer mysql = new MySQLContainer("mysql:8.0.36") // 도커 환경으로 DB 띄우겠다
            // 컨테이너 내부 DB 이름을 testdb로 설정
            .withDatabaseName("testdb")
            // DB 접속 계정 정보 설정
            .withUsername("test") // 실제 MySQL 계정 넣으면 안 된다
            .withPassword("test");

    @MockitoBean
    AuthInterceptor authInterceptor;
    @Autowired
    UsersRepository usersRepository;
    @Autowired
    PasswordEncode passwordEncode;
    @Autowired
    EmailPhoneNumberEncode emailPhoneNumberEncode;
    @Autowired
    MockMvc mockMvc;

    // url, username, password를 여기서 주입한다
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
                // exists : 이 필드가 JSON 응답 안에 존재하기만 하면 통과해라
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists());
        assertThat(usersRepository.count()).isEqualTo(1);
        Users token = usersRepository.findByUsername("username").get();
        assertThat(token.getUsername()).isEqualTo("username");
    }
}
