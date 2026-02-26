# CoreBoard
<b>CoreBoard Link</b> :
<a href="https://winwin0219.tistory.com/category/CoreBoard">
CoreBoard Tistory
</a>

CoreBoard는 **기본기 검증용 미니 게시판**입니다. </br>
**Spring Security/Lombok 없이 TDD로 구현**하며 스프링 동작 원리와 책임 분리를 훈련했습니다.</br>
비밀번호(BCrypt)와 개인정보(AES/GCM)를 분리했고, DTO는 **request/command/response**로 나눴습니다.</br>
**검증 로직이 섞여 테스트가 깨지던 문제**는 **Validator 분리로 해결**해 변경 영향 범위와 회귀 리스크를 줄였습니다.</br>

## 프로젝트 진행 상태

| 항목 | 상태 |
|------|------|
| EC2 배포 + CI/CD | ✅ 완료 |
| 모니터링 + 부하테스트 | ✅ 완료 |
| Refresh / Logout API | ✅ 완료 |
| CSP 헤더 적용 | 🔧 개선 예정 |


## Quick Start
1. JDK : 17.0.15 (Amazon Corretto)
2. MySQL : 8.0.34
3. Gradle : 8.14.3
4. Spring Boot : 3.5.6


### 1. MySQL 컨테이너 실행

`application-local.yml`의 기본값은 **3306 포트**를 사용합니다.


**Windows (PowerShell)**
```powershell
docker run -d --name coreboard-mysql `
  -e MYSQL_ROOT_PASSWORD=password `
  -e MYSQL_DATABASE=CoreBoard `
  -p 3306:3306 `
  --restart=always mysql:8.0.34
```

**macOS / Linux**

```
docker run -d \
  --name coreboard-mysql \
  -e MYSQL_ROOT_PASSWORD=password \
  -e MYSQL_DATABASE=CoreBoard \
  -p 3306:3306 \
  -v "${PWD}/docker/mysql/data:/var/lib/mysql" \
  -v "${PWD}/docker/mysql/init:/docker-entrypoint-initdb.d" \
  --restart always \
  mysql:8.0.34
```
---
### 2. Testcontainers Reuse
- 통합 테스트 실행 시 MySQL Testcontainers가 매번 새로 생성되면 초기 기동 시간이 **20~30초가 소요된 것으로 확인**되었습니다.
- 로컬 개발 환경에서는 컨테이너 재사용(reuse)을 활성화하여 테스트 실행 속도를 단축할 수 있습니다.
- 사용자 **홈 디렉토리에 `.testcontainers.properties` 파일을 생성**해야 합니다.

**Windows (PowerShell)**
```
@"
docker.client.strategy=org.testcontainers.dockerclient.NpipeSocketClientProviderStrategy
testcontainers.reuse.enable=true
"@ | Out-File -FilePath "$env:USERPROFILE\.testcontainers.properties" -Encoding ascii
```
**macOS / Linux**
```
echo "testcontainers.reuse.enable=true" >> ~/.testcontainers.properties
```
**확인 방법**
- 테스트 실행 후 MySQL 컨테이너가 종료되지 않고 유지되면 정상 적용
- 두 번째 실행부터는 컨테이너 기동 시간이 사라짐
> ⚠️  해당 설정은 로컬 개발 환경 전용입니다
> CI 환경에서는 컨테이너 재사용을 권장하지 않습니다

---

## 목차

[1. 기능](#1-기능) </br>
[2. 기술 스택](#2-기술-스택) </br>
[3. 프로젝트 구조](#3-프로젝트-구조)</br>
[4. 인증 사용 방법](#4-인증-사용-방법)</br>
[5. API 예시](#5-api-예시)</br>
[6. 공통 응답 포맷](#6-공통-응답-포맷)</br>
[7. 성능 개선](#7-성능-개선)</br>

---

## 1 기능

**1) 인증**
- 회원가입: `/auth/users`
- 로그인: `/auth/token` (Access Token 발급)
- 토큰 재발급: `/auth/refresh` 
- 로그아웃: `DELETE /auth/token`

**2) 게시글**
- 생성/수정/삭제: JWT 필요
- 조회(단건/목록): GET 요청은 인증 없이 허용
- 목록 조회: cursorTitle / cursorId 기반 Keyset

**3) 공통**
- `ApiResponse` 기반 공통 응답 포맷
- `GlobalExceptionHandler` 기반 공통 예외 응답
- Interceptor 기반 인증 처리(`Authorization: Bearer <token>`)
- 응답 JSON String XSS escape 처리(WebConfig)

---

## 2 기술 스택

![Java](https://img.shields.io/badge/Java-17-007396?style=flat-square&logo=java&logoColor=white)
![Spring](https://img.shields.io/badge/Spring-6DB33F?style=flat-square&logo=spring&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A?style=flat-square&logo=gradle&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=flat-square&logo=mysql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white)

![JUnit5](https://img.shields.io/badge/JUnit5-25A162?style=flat-square&logo=junit5&logoColor=white)
![Mockito](https://img.shields.io/badge/Mockito-78A641?style=flat-square)
![AssertJ](https://img.shields.io/badge/AssertJ-3F51B5?style=flat-square)
![MockMvc](https://img.shields.io/badge/MockMvc-6DB33F?style=flat-square&logo=spring&logoColor=white)
![Testcontainers](https://img.shields.io/badge/Testcontainers-2D2D2D?style=flat-square&logo=testcontainers&logoColor=white)
![JaCoCo](https://img.shields.io/badge/JaCoCo-Coverage-BF360C?style=flat-square)

![OpenAPI](https://img.shields.io/badge/OpenAPI-6BA539?style=flat-square&logo=openapiinitiative&logoColor=white)
![Swagger](https://img.shields.io/badge/Swagger-85EA2D?style=flat-square&logo=swagger&logoColor=black)



---

## 3 프로젝트 구조

> 도메인 단위로 패키징하고, DTO는 목적별로 분리합니다.

**1) `domain/auth`**
- controller / service / dto(command, request, response)

**2) `domain/board`**
- controller / service / repository / entity / dto(command, request, response)

**3)  `domain/common`**
- config (WebConfig, JwtConfig, 암호화 유틸)
- interceptor (JWT 인증)
- exception (에러코드/예외/핸들러)
- response (ApiResponse, CursorResponse)

## 4 인증 사용 방법
### 1) 회원가입 (Public)
#### macOS / Linux / Git Bash
```bash
curl -X POST "http://localhost:8080/auth/users" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "dssaa",
    "email": "user012@naver.com",
    "phoneNumber": "01012345678",
    "password": "user01",
    "confirmPassword": "user01"
  }'
```

#### Windows PowerShell
```bash
irm http://localhost:8080/auth/users -Method Post -ContentType 'application/json' -Body '{
"username":"qwerqwer2"
,"email":"qwerqwer2@naver.com"
,"phoneNumber":"01012345678"
,"password":"qwerqwer2"
,"confirmPassword":"qwerqwer2"
}'
```


### 2) 로그인 (Public)
#### macOS / Linux / Git Bash
```bash
curl -i -X POST "http://localhost:8080/auth/token" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "qwerqwer2",
    "password": "qwerqwer2"
  }'
```

#### Windows PowerShell (로그인 + 토큰 저장)
```bash
$token = (irm http://localhost:8080/auth/token -Method Post -ContentType 'application/json' -Body (@{
  username='qwerqwer2'
  password='qwerqwer2'
} | ConvertTo-Json)).data.accessToken

```
### 3) 게시글 생성
#### Windows PowerShell
```bash
irm http://localhost:8080/board -Method Post -ContentType 'application/json' `
  -Headers @{ Authorization = "Bearer $token" } `
  -Body (@{ title='title1'; content='content1' } | ConvertTo-Json)
```


## 5 API 예시

CoreBoard 프로젝트의 API 문서는 Swagger UI를 통해 확인 가능합니다.
> ⚠️ 서버를 먼저 실행해야 합니다.

- Swagger UI: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- OpenAPI JSON 스펙: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)


## 6 공통 응답 포맷
```json
{
  "success": false,
  "message": "제목은 필수입니다.",
  "data": {
    "code": "400",
    "errors": { "field": "title", "reason": "제목은 필수입니다." }
  }
}
```
## 7. 성능 개선

| 단계 | 방식 | p95 응답시간 | 개선 내용 |
|------|------|-------------|----------|
| 1단계 | OFFSET + 인덱스 없음 | 1,660ms | - |
| 2단계 | OFFSET + 인덱스 추가 | 369ms | title 컬럼 인덱스 추가 |
| 3단계 | Keyset 페이지네이션 | 12.56ms | 구조적 한계 해소 |

- 테스트 환경: 10만 건, 동시 접속 기준 k6 부하테스트