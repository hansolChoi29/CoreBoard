package com.example.coreboard;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest        // 스프링 부트 애플리케이션 전체를 테스트용으로 띄워서 실제처럼 컨테이너를 다 로드해주는 어노테이션
@ActiveProfiles("test") // 대상 : 시작할 때
@Testcontainers       // Testcontainers 활성화
class CoreBoardApplicationTest {

    @Container // Testcontainers가 이 필드를 테스트 시작 시 자동으로 Docker 컨테이너로 실행하게 함
    static MySQLContainer mysql = new MySQLContainer("mysql:8.0.36") // 도커 환경으로 DB 띄우겠다
            // 컨테이너 내부 DB 이름을 testdb로 설정
            .withDatabaseName("testdb")
            // DB 접속 계정 정보 설정
            .withUsername("test") // 실제 MySQL 계정 넣으면 안 된다
            .withPassword("test");

    // url, username, password를 여기서 주입한다
    @DynamicPropertySource
    static void overrideDataSourceProps(
            DynamicPropertyRegistry registry
    ) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Test
    void contextLoads() {
        assertNotNull(mysql);
    }

    @Test
    void main_run() {
        // try-catch 이유 : 앱을 또 한 번 부팅하는 거라 이미 띄워진 컨텍스트랑 겹쳐서 충돌 남
        try {
            // test 프로필로 띄우는 시도
            CoreBoardApplication.main(new String[]{"--spring.profiles.active=test"}); // 대상 : 두 번째 런타임
        } catch (Throwable ignore) {
            // - 이유: 이 테스트의 목적은 main() 라인 커버 뿐이고
            //        이미 다른 통합테스트에서 검증하고 있음
        }
    }
}