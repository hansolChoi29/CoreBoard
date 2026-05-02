# CoreBoard
## 프로젝트 개요

CoreBoard는 기존 학습용 CRUD 게시판을 기반으로, **운영 중 게시판 메뉴를 확장할 수 있는 개발 공유 게시판**으로 개선 중인 프로젝트입니다.


초기 버전은 Spring 동작 원리와 책임 분리를 검증하기 위한 미니 게시판이었지만,
피드백을 반영하면서 단순 게시글 CRUD를 넘어 **게시판 메뉴 관리, 게시글 구조 분리, 권한 정책, 조회 전략**까지 고려하는 방향으로 재설계했습니다.

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
#### Admin API

| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| POST | `/admin/setup` | 최초 관리자 계정 생성 | 불필요 |
| GET | `/admin/users` | 관리자 목록 조회 | ADMIN |
| PATCH | `/admin/users/{userId}/role` | 사용자 권한 변경 | ADMIN |


#### Auth API

| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| POST | `/auth/users` | 회원가입 | 불필요 |
| POST | `/auth/token` | 로그인 (AccessToken 발급, RefreshToken 쿠키 저장) | 불필요 |
| POST | `/auth/refresh` | AccessToken 재발급 (쿠키의 RefreshToken 사용) | 불필요 |
| DELETE | `/auth/refresh` | 로그아웃 (RefreshToken 쿠키 삭제) | 불필요 |

#### Board API

| Method | URL | 설명           | 인증 |
|--------|-----|--------------|------|
| POST | `/admin/boards` | 게시판 메뉴 생성    | ADMIN |
| GET | `/boards` | 활성 게시판 목록 조회 | 불필요 |
| PATCH | `/admin/boards/{boardId}` | 게시판 메뉴 수정    | ADMIN |
| PATCH | `DELETE /admin/boards/{id}` | 게시판 삭제 요청(soft delete)  | ADMIN |

#### Post API

| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| POST | `/boards/{boardId}/posts` | 게시글 생성 | USER |
| GET | `/boards/{boardId}/posts` | 특정 게시판 게시글 목록 조회 | 불필요 |
| GET | `/posts/{postId}` | 게시글 단건 조회 | 불필요 |
| PATCH | `/posts/{postId}` | 게시글 수정 | 작성자 |
| DELETE | `/posts/{postId}` | 게시글 삭제 | 작성자 또는 ADMIN |

---

## 데이터 모델 설명

#### Users

사용자 정보와 자격증명을 관리하는 엔티티입니다.

| 필드 | 타입 | 설명 |
|------|------|------|
| userId | Long | 사용자 ID (PK, AUTO_INCREMENT) |
| username | String | 로그인 아이디 (UNIQUE, NOT NULL) |
| nickname | String | 사용자 표시 이름 |
| role | UserRole | USER / ADMIN 역할 |
| password | String | BCrypt 해시값 (cost=12) |
| email | String | AES/GCM 암호화 저장 |
| phoneNumber | String | AES/GCM 암호화 저장 |

#### Board

게시판 메뉴와 게시판별 운영 정책을 관리하는 엔티티입니다.

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | 게시판 ID |
| name | String | 화면에 표시되는 게시판 이름 |
| slug | String | URL/API에서 사용하는 게시판 식별자 |
| commentEnabled | boolean | 댓글 허용 여부 |
| answerAcceptedEnabled | boolean | Q&A 답변 채택 허용 여부 |
| requireAttachment | boolean | 첨부파일 필수 여부 |
| maxAttachmentCount | int | 첨부파일 최대 개수 |
| maxContentLength | int | 본문 최대 길이 |
| requiredWriteRole | UserRole | 게시글 작성 가능 권한 |
| active | boolean | 게시판 활성화 여부 |

#### Post

특정 게시판에 작성되는 게시글 엔티티입니다.

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | 게시글 ID |
| boardId | Long | 소속 게시판 ID |
| userId | Long | 작성자 ID |
| title | String | 제목 |
| content | String | 본문 |
| contentFormat | String | 본문 저장 형식 |
| status | String | 게시글 상태 |
| viewCount | Long | 조회수 |
| createdAt | LocalDateTime | 생성일 |
| updatedAt | LocalDateTime | 수정일 |

#### Comment

게시글에 작성되는 댓글 엔티티입니다.  
Q&A 답변 채택 기능 확장을 고려해 별도 도메인으로 분리했습니다.

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | 댓글 ID |
| post | Post | 댓글이 속한 게시글 |
| user | Users | 댓글 작성자 |
| content | String | 댓글 내용 |
| createdAt | LocalDateTime | 생성일 |
| updatedAt | LocalDateTime | 수정일 |


#### 관계

- Board : Post = 1 : N
- Users : Post = 1 : N
- Users : Comment = 1 : N
- Post : Comment = 1 : N

---

## Architecture Decision Records

프로젝트의 주요 설계 결정은 ADR로 기록합니다.

| ADR                                                                | 내용 |
|--------------------------------------------------------------------|------|
| [ADR-0001](docs/adr/0001-manage-boards-as-data.md)                 | 운영 중 변경 가능한 게시판 메뉴를 DB 데이터로 관리한 이유 |
| [ADR-0002](docs/adr/0002-pagination-strategy-by-use-case.md)       | 게시글 목록 조회에서 조회 목적별 페이지네이션 전략을 분리한 이유 |
| [ADR-0003](docs/adr/0003-post-view-count-strategy.md)              | 게시글 조회수 저장 방식과 중복 조회 처리 기준 |
| [ADR-0004](docs/adr/0004-board-name-slug-separation.md)            | 게시판 표시 이름과 URL 식별자를 분리한 이유 |
| [ADR-0005](docs/adr/0005-admin-policy-for-board-management.md)     |Admin 초기 생성 및 권한 전환 정책|
| [ADR-0006](docs/adr/0006-auth-with-handler-interceptor.md)         | Spring Security 대신 HandlerInterceptor로 인증 흐름을 직접 구현한 이유 |
| [ADR-0007](docs/adr/0007-request-command-response-dto.md)          | DTO를 request-command-response로 분리한 이유 |
| [ADR-0008](docs/adr/0008-password-and-personal-data-encryption.md) | 비밀번호와 개인정보 암호화 전략을 분리한 이유 |
| [ADR-009](docs/adr/0009-refresh-token-http-only-cookie.md)         |  RefreshToken 노출 범위를 줄이기 위해 HttpOnly 쿠키를 선택한 이유 |
| [ADR-0010](docs/adr/0010-use-testcontainers-instead-of-h2.md)      | H2 대신 Testcontainers를 선택한 이유 |

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


**3. 역할 기반 접근 제어**

- 일반 사용자는 게시글과 댓글을 작성할 수 있습니다.
- ADMIN은 게시판 생성, 수정, 비활성화 같은 운영 기능에 접근할 수 있습니다.
- JWT에 role claim을 포함하고, `HandlerInterceptor`에서 `/admin/**` 요청에 대해 ADMIN 권한을 검증합니다.
- 게시글 수정/삭제는 작성자 본인 또는 ADMIN만 허용하는 방향으로 확장 중입니다.

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
## 제약사항

**1. 관리자 계정 생성 제약**

- 최초 관리자 계정은 `/admin/setup`을 통해 생성합니다.
- ADMIN 계정이 이미 1명 이상 존재하면 `/admin/setup`으로 추가 ADMIN 계정을 생성할 수 없습니다.
- 추가 관리자는 기존 ADMIN이 사용자 권한 변경 API를 통해 USER를 ADMIN으로 전환하는 방식으로 관리합니다.

**2. 관리자 권한 회수 제약**

- ADMIN 계정이 1명뿐인 경우 해당 계정을 USER로 전환할 수 없습니다.
- 마지막 ADMIN 권한이 회수되면 게시판 생성, 수정, 비활성화 같은 운영 기능을 수행할 계정이 사라지기 때문입니다.

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

