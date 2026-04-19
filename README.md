# CoreBoard
## 프로젝트 개요

CoreBoard는 **Spring 동작 원리와 책임 분리를 훈련하기 위한 기본기 검증용 미니 게시판**입니다.

취업 준비 과정에서 프레임워크 추상화에 의존하지 않고 직접 구현 능력을 검증하고 싶었습니다.
그래서 **Spring Security와 Lombok을 의도적으로 배제하고, TDD 방식으로 처음부터 구현**했습니다.

인증은 `HandlerInterceptor`로 직접 구현해 요청 처리 파이프라인 흐름을 이해했고,
비밀번호(BCrypt)와 개인정보(AES/GCM)의 암호화 전략을 분리했습니다.
DTO는 `request / command / response` 3단계로 역할을 명확히 나눴고,
검증 로직이 서비스와 섞여 테스트가 불안정해지던 문제는 `Validator` 클래스 분리로 해결했습니다.

목록 조회 성능도 개선했습니다.
OFFSET 방식에서 Keyset 방식으로 전환해 10만 건 기준 p95 응답시간을 **1,660ms → 12.56ms (99.2% 개선)** 달성했습니다.

---

## 기술 스택

#### Language

![Java](https://img.shields.io/badge/Java-17-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)

#### Backend

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Web](https://img.shields.io/badge/Spring%20Web-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/Spring%20Data%20JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Hibernate](https://img.shields.io/badge/Hibernate-59666C?style=for-the-badge&logo=hibernate&logoColor=white)

#### Database

![MySQL](https://img.shields.io/badge/MySQL-4479A1.svg?style=for-the-badge&logo=mysql&logoColor=white)

#### API Documentation

![Swagger](https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)

#### Build Tool

![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white)

#### Test

![JUnit5](https://img.shields.io/badge/JUnit5-25A162?style=for-the-badge&logo=junit5&logoColor=white)
![Testcontainers](https://img.shields.io/badge/Testcontainers-MySQL-blue?style=for-the-badge)

> Spring Security, Lombok 미사용 (의도적 배제)

---

## 실행 방법

#### 실행 환경

- Java 17 (Amazon Corretto)
- Gradle 8.14.3
- Spring Boot 3.5.6
- Docker
- MySQL 8.0.34

#### MySQL 컨테이너 실행

```bash
docker run -d --name coreboard-mysql \
  -e MYSQL_ROOT_PASSWORD=password \
  -e MYSQL_DATABASE=CoreBoard \
  -p 3306:3306 \
  --restart=always mysql:8.0.34
```

<details>
<summary>Windows (PowerShell)</summary>

```powershell
docker run -d --name coreboard-mysql `
  -e MYSQL_ROOT_PASSWORD=password `
  -e MYSQL_DATABASE=CoreBoard `
  -p 3306:3306 `
  --restart=always mysql:8.0.34
```

</details>

#### 애플리케이션 실행

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

#### API 명세 확인

실행 후 아래 URL 접속

```
http://localhost:8080/swagger-ui/index.html
```

---

## API 목록 및 예시

로컬 실행 후 Swagger UI에서 전체 API 명세와 샘플 요청/응답을 확인할 수 있습니다.

```
http://localhost:8080/swagger-ui/index.html
```

#### Auth API

| Method | URL | 설명 |
|--------|-----|------|
| POST | `/auth/users` | 회원가입 |
| POST | `/auth/token` | 로그인 (AccessToken 발급, RefreshToken 쿠키 저장) |
| POST | `/auth/refresh` | AccessToken 재발급 (쿠키의 RefreshToken 사용) |
| DELETE | `/auth/refresh` | 로그아웃 (RefreshToken 쿠키 삭제) |

#### Board API

| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| POST | `/board` | 게시글 생성 | Bearer Token |
| GET | `/board/{id}` | 게시글 단건 조회 | 불필요 |
| GET | `/board?cursorTitle=&cursorId=&size=10&sort=asc` | 게시글 목록 조회 (Keyset Pagination) | 불필요 |
| PUT | `/board/{id}` | 게시글 수정 (본인만 가능) | Bearer Token |
| DELETE | `/board/{id}` | 게시글 삭제 (본인만 가능) | Bearer Token |

---

## 데이터 모델 설명

#### Users

사용자 정보와 자격증명을 관리하는 엔티티입니다.

| 필드 | 타입 | 설명 |
|------|------|------|
| userId | Long | 사용자 ID (PK, AUTO_INCREMENT) |
| username | String | 로그인 아이디 (UNIQUE, NOT NULL) |
| password | String | BCrypt 해시값 (cost=12) |
| email | String | AES/GCM 암호화 저장 |
| phoneNumber | String | AES/GCM 암호화 저장 |

#### Board

게시글 본문과 작성 이력을 관리하는 엔티티입니다.

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | 게시글 ID (PK, AUTO_INCREMENT) |
| title | String | 제목 (NOT NULL, 최대 255자, 인덱스 적용) |
| content | String | 본문 (NOT NULL, 최대 1000자) |
| userId | Long | 작성자 FK (users.userId 참조) |
| createdDate | LocalDateTime | JPA Auditing 자동 관리 |
| lastModifiedDate | LocalDateTime | JPA Auditing 자동 관리 |

#### 관계

- Users : Board = 1 : N
- 하나의 사용자가 여러 게시글을 작성할 수 있습니다.
- Board는 반드시 하나의 Users에 속합니다.

---

## 요구사항

CoreBoard는 외부 요구사항 명세 없이 스스로 설계한 프로젝트입니다.
아래는 설계 착수 시점에 직접 세운 가정들입니다.

**1. 인증 방식**

- Spring Security 없이 `HandlerInterceptor`로 직접 구현했습니다.
- 요청 처리 파이프라인(Filter → Interceptor → Controller) 흐름을 실제로 이해하는 것이 목적이었습니다.

**2. 조회는 인증 면제**

- 게시판 특성상 비로그인 사용자도 게시글을 읽을 수 있어야 한다고 판단했습니다.
- `GET` 요청은 `Authorization` 헤더가 없어도 통과시켰습니다.

**3. 수정/삭제 권한**

- 본인 게시글만 허용합니다.
- 별도 역할(role) 체계는 미니 프로젝트 범위를 초과한다고 판단해 제외했습니다.

**4. 개인정보 암호화 전략**

- 이메일과 전화번호는 서비스 내부에서 원문 조회가 필요하므로 양방향 암호화(AES/GCM)를 선택했습니다.
- 비밀번호는 원문 복원이 불필요하고 단방향이 안전하므로 BCrypt를 선택했습니다.

**5. 목록 조회 페이지네이션**

- 대용량 데이터에서 OFFSET의 성능 한계가 명확하므로 처음부터 Keyset 방식으로 설계했습니다.
- `(title, id)` 복합 커서를 사용해 동일 제목이 여러 개일 때 발생하는 중복/누락 문제를 방지했습니다.

**6. 공통 응답 포맷**

- 성공/실패 모두 `ApiResponse`로 통일했습니다.
- 클라이언트가 응답 구조를 예측할 수 있도록 일관성을 우선했습니다.

---

## 설계 결정과 이유

**1. Spring Security를 쓰지 않고 HandlerInterceptor를 직접 구현한 이유**

Spring Security를 사용하면 필터 체인 내부 동작을 모른 채 설정값만 다루게 됩니다.
`HandlerInterceptor.preHandle()`에서 `Authorization` 헤더를 파싱하고, JWT를 검증한 뒤 `request.setAttribute("username", ...)`으로 컨트롤러에 사용자 정보를 전달하는 흐름을 직접 만들었습니다.
이를 통해 요청 처리 파이프라인(Filter → Interceptor → ArgumentResolver → Controller)이 어떻게 동작하는지 직접 체감할 수 있었습니다.

**2. DTO를 3단계(request → command → response)로 분리한 이유**

| 계층 | 역할 |
|------|------|
| `request` | 외부 입력 수신. 검증 전 원본 데이터 |
| `command` | 서비스 입력 모델. 검증을 통과한 의미 있는 값만 전달 |
| `response` | 외부 출력 전용. 내부 엔티티 구조를 노출하지 않음 |

컨트롤러와 서비스 사이의 계약을 명확히 하고, 한 계층의 변경이 다른 계층에 파급되지 않도록 하기 위해 이 구조를 선택했습니다.

**3. Validator 클래스를 별도로 분리한 이유**

초기에는 검증 로직이 컨트롤러와 서비스에 혼재해 있었습니다.
이로 인해 서비스 레이어 테스트에서 검증 로직까지 함께 실행돼 테스트가 불필요하게 깨지거나, 검증이 중복 실행되는 문제가 발생했습니다.
`AuthValidation`, `BoardValidation`으로 분리한 뒤 단위 테스트에서 검증 로직을 독립적으로 제어할 수 있게 됐고, 회귀 리스크도 줄었습니다.

**4. 비밀번호와 개인정보 암호화 전략을 분리한 이유**

| 대상 | 방식 | 이유 |
|------|------|------|
| 비밀번호 | BCrypt (단방향, cost=12) | 원문 복원 불필요. 해킹 시 역추적 불가 |
| 이메일 / 전화번호 | AES/GCM (양방향) | 서비스 내부에서 원문 조회가 필요한 데이터 |

같은 "암호화"라도 복호화 필요 여부에 따라 전략이 달라진다는 것을 설계에 반영했습니다.
AES/GCM은 IV(12byte)를 매번 새로 생성해 암호문 앞에 붙이므로, 같은 평문이라도 매번 다른 암호문이 생성됩니다.

**5. Keyset Pagination에서 `(title, id)` 복합 커서를 선택한 이유**

| 단계 | 방식 | p95 응답시간 | 비고 |
|------|------|----------:|------|
| 1단계 | OFFSET + 인덱스 없음 | 1,660ms | - |
| 2단계 | OFFSET + title 인덱스 | 369ms | title 컬럼 인덱스 추가 |
| 3단계 | Keyset (cursorTitle, cursorId) | 12.56ms | 구조적 한계 해소 |

OFFSET은 페이지가 뒤로 갈수록 앞의 모든 행을 스캔해야 하는 구조적 한계가 있습니다.
Keyset은 이전 페이지의 마지막 커서를 WHERE 조건으로 필터링하기 때문에 인덱스를 타고 해당 지점부터 바로 조회합니다.
`title`만으로 커서를 잡으면 동일 제목이 여러 개일 때 중복/누락이 발생하므로, `id`를 보조 커서로 추가해 유일성을 보장했습니다.

**6. RefreshToken을 HttpOnly 쿠키에 분리해 저장한 이유**

AccessToken은 응답 body에, RefreshToken은 `HttpOnly; Secure; SameSite=Strict; Path=/auth/refresh` 쿠키에 담았습니다.

- `HttpOnly` 쿠키는 JavaScript로 접근이 불가하므로 XSS 공격으로 RefreshToken이 탈취되는 위험을 낮춥니다.
- `Path=/auth/refresh` 제한으로 RefreshToken이 다른 API 요청에 자동 첨부되지 않습니다.

**7. Testcontainers를 선택한 이유**

| 항목 | Testcontainers | H2 인메모리 |
|------|----------------|-------------|
| 실제 DB 동작 재현 | 높음 | 낮음 (방언 차이) |
| 테스트 환경 격리 | 높음 | 높음 |
| 속도 | 컨테이너 기동 필요 | 빠름 |
| 선택 | ✓ | — |

H2는 MySQL과 SQL 방언이 달라 실제 환경에서만 나타나는 버그를 잡기 어렵습니다.
Testcontainers로 MySQL 컨테이너를 띄워 실제 DB 환경과 동일한 조건에서 통합 테스트를 실행했습니다.
`withReuse(true)` 설정으로 컨테이너를 재사용하고, BCrypt cost를 4로 낮춰 테스트 실행 시간을 약 43% 단축했습니다.

---

## 테스트 실행 방법

```bash
./gradlew test
```

테스트는 서비스 계층 단위 테스트와 Testcontainers 기반 통합 테스트로 구성되어 있습니다.
Docker가 실행 중이면 별도 MySQL 설정 없이 동작합니다.

| 종류 | 설명 |
|------|------|
| 단위 테스트 | Service, Validator, JwtUtil, 암호화 유틸 독립 검증 (Mockito) |
| 통합 테스트 | Testcontainers MySQL 기반 인증/게시글 전체 흐름 검증 |
| 컨트롤러 테스트 | MockMvc 기반 요청/응답 포맷 검증 |

커버리지 리포트는 아래 명령어로 확인할 수 있습니다.

```bash
./gradlew test jacocoTestReport
```

> 도메인 레이어 테스트 커버리지 100% 달성

---
