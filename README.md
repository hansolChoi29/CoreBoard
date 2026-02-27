# CoreBoard

**CoreBoard Tistory**: https://winwin0219.tistory.com/category/CoreBoard

CoreBoard는 **기본기 검증용 미니 게시판**입니다. </br>
**Spring Security/Lombok 없이 TDD로 구현**하며 스프링 동작 원리와 책임 분리를 훈련했습니다.</br>
비밀번호(BCrypt)와 개인정보(AES/GCM)를 분리했고, DTO는 **request/command/response**로 나눴습니다.</br>
**검증 로직이 섞여 테스트가 깨지던 문제**는 **Validator 분리로 해결**해 변경 영향 범위와 회귀 리스크를 줄였습니다.</br>

## 프로젝트 진행 상태

| 항목                   | 상태       |
|----------------------|----------|
| EC2 배포 + CI/CD       | ✅ 완료     |
| 모니터링 + 부하테스트         | ✅ 완료     |
| Refresh / Logout API | ✅ 완료     |
| CSP 헤더 적용            | 🔧 개선 예정 |


## 기능
### 인증

- 회원가입: `POST /auth/users`
- 로그인(Access Token 발급): `POST /auth/token`
- 토큰 재발급: `POST /auth/refresh`
- 로그아웃: `DELETE /auth/refresh`

### 게시글

- 생성/수정/삭제: JWT 필요
- 조회(단건/목록): GET 요청은 인증 없이 허용
- 목록 조회: `cursorTitle / cursorId` 기반 Keyset Pagination

### 공통

- `ApiResponse` 기반 공통 응답 포맷
- `GlobalExceptionHandler` 기반 공통 예외 응답
- Interceptor 기반 인증(`Authorization: Bearer <token>`)
- 응답 JSON String XSS escape 처리(WebConfig)



## Quick Start

**Requirements**

- JDK: 17.0.15 (Amazon Corretto)
- MySQL: 8.0.34
- Gradle: 8.14.3
- Spring Boot: 3.5.6

### MySQL 실행 (Docker)


```bash
docker run -d --name coreboard-mysql \
  -e MYSQL_ROOT_PASSWORD=password \
  -e MYSQL_DATABASE=CoreBoard \
  -p 3306:3306 \
  --restart=always mysql:8.0.34
```

> application-local.yml 기본값은 3306 포트를 사용합니다.

<details>
<summary>Windows(PowerShell) 명령어 보기</summary>

```powershell
docker run -d --name coreboard-mysql `
  -e MYSQL_ROOT_PASSWORD=password `
  -e MYSQL_DATABASE=CoreBoard `
  -p 3306:3306 `
  --restart=always mysql:8.0.34
```

</details>

### 애플리케이션 실행
```bash
./gradlew bootRun
```
### 테스트 실행
```bash
./gradlew test
```


## API 문서
- (배포) Swagger UI: http://3.38.144.47:8080/swagger-ui/index.html
- (배포) OpenAPI JSON: http://3.38.144.47:8080/v3/api-docs
- (로컬) Swagger UI: http://localhost:8080/swagger-ui/index.html


## 공통 응답 포맷
```json
{
  "success": false,
  "message": "제목은 필수입니다.",
  "data": {
    "code": "400",
    "errors": {
      "field": "title",
      "reason": "제목은 필수입니다."
    }
  }
}
```

## 성능 개선

| 단계  | 방식              | p95 응답시간 | 개선 내용           |
|-----|-----------------|---------:|-----------------|
| 1단계 | OFFSET + 인덱스 없음 |  1,660ms | -               |
| 2단계 | OFFSET + 인덱스 추가 |    369ms | title 컬럼 인덱스 추가 |
| 3단계 | Keyset 페이지네이션   |  12.56ms | 구조적 한계 해소       |

- 테스트 환경: 10만 건, 동시 접속 기준 k6 부하테스트

## 프로젝트 구조

> 도메인 단위로 패키징하고, DTO는 목적별로 분리합니다. </br>
(request: 입력 검증/요청, command: 서비스 입력 모델, response: 응답 전용)

**1) domain/auth**

- controller / service / dto(command, request, response)

**2) domain/board**

- controller / service / repository / entity / dto(command, request, response)

**3) domain/common**

- config: WebConfig, JwtConfig, SwaggerConfig, 암호화 유틸
- interceptor: JWT 인증(AuthInterceptor)
- exception: 에러코드/예외/핸들러(GlobalExceptionHandler)
- response: ApiResponse, CursorResponse
- validation: AuthValidation, BoardValidation

```
CoreBoard/
└─ src/
   ├─ main/
   │  ├─ java/com/example/coreboard/
   │  │  ├─ CoreBoardApplication.java
   │  │  └─ domain/
   │  │     ├─ auth/   (controller/service/dto)
   │  │     ├─ board/  (controller/service/repository/entity/dto)
   │  │     ├─ users/  (entity/repository)
   │  │     └─ common/ (config/interceptor/exception/response/validation/util)
   │  ├─ resources/   (profile yml)
   │  └─ monitoring/  (prometheus, docker-compose)
   └─ test/
      ├─ java/...      (unit/integration)
      └─ resources/
```
