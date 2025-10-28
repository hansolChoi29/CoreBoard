package com.example.coreboard.domain.integration;

import com.example.coreboard.domain.board.entity.Board;
import com.example.coreboard.domain.board.repository.BoardRepository;
import com.example.coreboard.domain.common.interceptor.AuthInterceptor;
import com.example.coreboard.domain.users.entity.Users;
import com.example.coreboard.domain.users.repository.UsersRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.http.MediaType;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest        // 스프링 부트 애플리케이션 전체를 테스트용으로 띄워서 실제처럼 컨테이너를 다 로드해주는 어노테이션
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = true)
// HTTP 요청 시뮬레이션 (가짜 서블릿 환경)
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

    private String accessToken;
    private Long savedUserId;
    private String savedUsername;

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
    void setup() {
        Users user = usersRepository.save(new Users("username", "password", "email@naver.com", "01012341234"));

        String secret = "test-jwt-secret-key-for-coreboard";
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        // 뽑아 쓸 거임
        this.savedUserId = user.getUserId();
        this.savedUsername = user.getUsername();

        // 이 클래스에 있는 accessToken 변수에 값을 넣어라
        // accessToken = Jwts.builder() : 메서드 안의 변수인지 클래스의 변수인지 모름
        this.accessToken = Jwts.builder()
                .setSubject("username")
                .claim("userId", user.getUserId())
                .claim("type", "access")
                .setExpiration(new Date(System.currentTimeMillis() + 60 * 60 * 1000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    @Test
    @DisplayName("POST/board")
    void boardCreate() throws Exception {
        String json = """
                {
                    "title" : "title",
                    "content" : "content"
                }
                """;
        mockMvc.perform(
                        post("/board")
                                .header("Authorization", "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("title"))
                .andExpect(jsonPath("$.data.content").value("content"));
        assertThat(boardRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("GET/board/id")
    void getOn() throws Exception {
        Board board = new Board(
                null, // 가짜가 아니라 실제 DB에 접근하기 때문에 1L 지정해주면 에러 남
                10L,
                "title",
                "content",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        Board saved = boardRepository.save(board);
        Long realId = saved.getId();
        mockMvc.perform(
                        get("/board/{id}", realId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(realId))
                .andExpect(jsonPath("$.data.title").value("title"))
                .andExpect(jsonPath("$.data.content").value("content"));
    }

    @Test
    @DisplayName("GET/board")
    void getAll() throws Exception {
        Board board = new Board(
                null, // 가짜가 아니라 실제 DB에 접근하기 때문에 1L 지정해주면 에러 남
                10L,
                "title",
                "content",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        boardRepository.save(board);
        mockMvc.perform(
                        get("/board")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("page", "0")
                                .param("size", "10")
                                .param("sort", "asc")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page").value("0"))
                .andExpect(jsonPath("$.data.size").value("10"));
    }


    // NoAuthForIntegrationTest : 통합 테스트에서 인증만 잠깐 꺼두는 설정 클래스
    // WebMvcConfigurer : 스프링 MVC 설정을 커스터마이징 할 수 있는 인터페이스

    // 예시) 실제 인터셉터 안 거치게 하고 싶다, 이메일 찐 발송 대신 가짜 발송 대체하고 싶다.
    // 테스트용 Bean 하나만 등록하고 싶다 등
    @TestConfiguration // 테스트할 때만 임시로 적용되는 스프링 설정
    static class NoAuthForIntegrationTest implements WebMvcConfigurer { // 바깥 클래스의 객체가 없어도 이 클래스를 사용할 수 있게 하려는 것
        // (static이어야 Spring이 정상적으로 읽는다)
        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            // 테스트 전용 설정 클래스 하나 만들어서
            // 스프링 MVC의 인터셉터 등록을 빈칸으로 덮어쓰겠다
        }
    }
}
