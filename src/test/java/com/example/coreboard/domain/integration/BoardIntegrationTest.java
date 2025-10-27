package com.example.coreboard.domain.integration;


import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest        // 스프링 부트 애플리케이션 전체를 테스트용으로 띄워서 실제처럼 컨테이너를 다 로드해주는 어노테이션
@ActiveProfiles("test")
@AutoConfigureMockMvc // HTTP 요청 시뮬레이션 (가짜 서블릿 환경)
@Testcontainers       // Testcontainers 활성화
@Transactional        // 각 테스트가 끝나면 롤백해서 DB 깨끗하게 초기화
public class BoardIntegrationTest {
    // Testcontainers 설정 영역

    // 스프링 빈 주입 영역 (@Autowired)

    // 테스트 메서드 (컨트롤러 -> 서비스 -> DB 흐름)
    // POST /board
    // given(JSON요청 보문)
    // when: /board 엔드포인트 호출(Controller->Service->Repository)
    // then: DB에 데이터 실제로 들어갔는지 확인

    // 학습테스트 먼저 한 이후 아래 요청도 테스트 예정

    // GET /board/{id}
    // GET /board
    // PUT /board/{id}
    // Delete /board/{id}

    // POST /auth/users
    // POST /auth/token

}
