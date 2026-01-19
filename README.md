# CoreBoard

CoreBoard는 **기본기 검증용 미니 게시판**입니다. </br>
**Spring Security/Lombok 없이 TDD로 구현**하며 스프링 동작 원리와 책임 분리를 훈련했습니다.</br>
비밀번호(BCrypt)와 개인정보(AES/GCM)를 분리했고, DTO는 **request/command/response**로 나눴습니다.</br>
**검증 로직이 섞여 테스트가 깨지던 문제**는 **Validator 분리로 해결**해 변경 영향 범위와 회귀 리스크를 줄였습니다.</br>



## Quick Start
1. JDK : 17.0.15 (Amazon Corretto) 
2. MySQL : 8.0.34
3. Gradle : 8.14.3
4. Spring Boot : 3.5.6


### MySQL 컨테이너 실행

`application-local.yml`의 기본값은 **3306 포트**를 사용합니다.

---

### 3306 사용(권장: 로컬 MySQL이 없을 때)

**Windows (PowerShell)**
```powershell
docker run -d --name coreboard-mysql `
  -e MYSQL_ROOT_PASSWORD=password `
  -e MYSQL_DATABASE=CoreBoard `
  -p 3306:3306 `
  --restart=always mysql:8.0.34
```

### macOS / Linux

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



## 목차

- [기능](#기능)
- [기술 스택](#기술-스택)
- [프로젝트 구조](#프로젝트-구조)
- [Quick Start](#quick-start)
- [인증 사용 방법](#인증-사용-방법)
- [API 예시](#api-예시)
- [공통 응답 포맷](#공통-응답-포맷)


---

## 기능

- 인증
  - 회원가입: `/auth/users`
  - 로그인: `/auth/token` (Access Token 발급)
  - Refresh Token은 **HttpOnly Cookie**로 내려줌(현재 Refresh 재발급 API는 미구현)
- 게시글
  - 생성/수정/삭제: JWT 필요
  - 조회(단건/목록): GET 요청은 인증 없이 허용
  - 목록 조회: page/size/sort(asc|desc) 지원
- 공통
  - `ApiResponse` 기반 공통 응답 포맷
  - `GlobalExceptionHandler` 기반 공통 예외 응답
  - Interceptor 기반 인증 처리(`Authorization: Bearer <token>`)
  - 응답 JSON String XSS escape 처리(WebConfig)

---

## 기술 스택

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

## 프로젝트 구조

> 도메인 단위로 패키징하고, DTO는 목적별로 분리합니다.

- `domain/auth`
  - controller / service / dto(command, request, response)
- `domain/board`
  - controller / service / repository / entity / dto(command, request, response)
- `domain/common`
  - config (WebConfig, JwtConfig, 암호화 유틸)
  - interceptor (JWT 인증)
  - exception (에러코드/예외/핸들러)
  - response (ApiResponse, PageResponse)

## 인증 사용 방법
회원가입 </br>
```http://localhost:8080/auth/users```
```json
{
    "username":"dssaa",
    "email":"user012@naver.com",
    "phoneNumber":"user01",
    "password":"user01",
    "confirmPassword":"user01"    
}
```

로그인 </br>
```json
{
    "username":"dssaa",
    "password":"user01",
}
```

## API 예시

CoreBoard 프로젝트의 API 문서는 Swagger UI를 통해 확인 가능합니다.  
> ⚠️ 서버를 먼저 실행해야 합니다.

- Swagger UI: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- OpenAPI JSON 스펙: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)


## 공통 응답 포맷
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
