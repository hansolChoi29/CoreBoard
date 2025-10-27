package com.example.coreboard.domain.integration;

import com.example.coreboard.domain.board.repository.BoardRepository;
import com.example.coreboard.domain.common.interceptor.AuthInterceptor;
import com.example.coreboard.domain.users.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest        // 스프링 부트 애플리케이션 전체를 테스트용으로 띄워서 실제처럼 컨테이너를 다 로드해주는 어노테이션
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false) // HTTP 요청 시뮬레이션 (가짜 서블릿 환경)
@Testcontainers       // Testcontainers 활성화
@Transactional        // 각 테스트가 끝나면 롤백해서 DB 깨끗하게 초기화
public class BoardTest {

    @Container // Testcontainers가 이 필드를 테스트 시작 시 자동으로 Docker 컨테이너로 실행하게 함
    static MySQLContainer mysql = new MySQLContainer("mysql:8.0.36") // 도커 환경으로 DB 띄우겠다
            // 컨테이너 내부 DB 이름을 testdb로 설정
            .withDatabaseName("testdb")
            // DB 접속 계정 정보 설정
            .withUsername("test") // 실제 MySQL 계정 넣으면 안 된다
            .withPassword("test");
    @MockitoBean
    AuthInterceptor authInterceptor;
    // Testcontainers 설정 영역
    // MySQL 대신 도커 MySQL 컨테이너를 띄워서 사용
    // 운영 DB랑 최대한 비슷한 환경을 만들기 위한 목적
    // 스프링 빈 주입 영역 (@Autowired)
    @Autowired
    BoardRepository boardRepository;
    @Autowired
    UsersRepository usersRepository;
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

    @TestConfiguration
    static class NoAuthForIntegrationTest implements WebMvcConfigurer {
        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            // 비워둔다. => 이 테스트 컨텍스트에선 어떤 인터셉터도 등록하지 않는다.
            // 즉, new AuthInterceptor() 자체가 체인에 안 끼게 된다.
        }
    }
    // 테스트 메서드 (컨트롤러 -> 서비스 -> DB 흐름)
    // POST /board

    // 학습테스트 먼저 한 이후 아래 요청도 테스트 예정

    // GET /board/{id}
    // GET /board
    // PUT /board/{id}
    // Delete /board/{id}

}
