# CoreBoard

## 프로젝트 개요

서비스 URL : https://coreboard-web.vercel.app/

Devlog : https://winwin0219.tistory.com/category/CoreBoard

CoreBoard는 Spring Boot 기반의 게시판 프로젝트입니다.
처음에는 기본 게시글 CRUD를 학습하기 위한 미니 게시판으로 시작했지만, 현재는 **게시판 운영 정책, 권한, 첨부파일, 댓글, 검색, 응답 포맷, 테스트, 배포와 관측 가능성**까지 함께 다루는 방향으로 확장하고 있습니다.

이 프로젝트에서 중점적으로 다룬 내용은 다음과 같습니다.

- Spring Security 없이 `HandlerInterceptor`로 JWT 인증 흐름 직접 구현
- 비밀번호는 BCrypt, 이메일과 휴대폰번호는 AES/GCM 방식으로 암호화
- `request → command → result → response` 흐름으로 DTO 역할 분리
- 입력값 검증은 Controller 진입 전용 Validator에서 처리하고, Service는 비즈니스 흐름과 권한 검증에 집중
- 게시판별 정책으로 댓글 허용 여부, 첨부파일 필수 여부, 첨부파일 최대 개수, 작성 가능 권한 관리
- 게시글 목록은 게시판 단위로 조회하고, 검색어가 있으면 제목 또는 본문 기준으로 검색
- 게시글 목록은 Page 기반 오프셋 페이지네이션, 댓글 목록은 Slice 기반 페이지네이션으로 분리
- 첨부파일은 게시글 작성 전 임시 업로드 후, 게시글 저장 시 확정 처리
- 오래된 임시 첨부파일과 삭제 대기 첨부파일은 스케줄러로 정리

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

#### Storage

![MinIO](https://img.shields.io/badge/MinIO-C72E49?style=for-the-badge&logo=minio&logoColor=white)
![S3 Compatible](https://img.shields.io/badge/S3%20Compatible%20Storage-569A31?style=for-the-badge&logo=amazons3&logoColor=white)

#### Monitoring

![Prometheus](https://img.shields.io/badge/Prometheus-E6522C?style=for-the-badge&logo=prometheus&logoColor=white)
![Grafana](https://img.shields.io/badge/Grafana-F46800?style=for-the-badge&logo=grafana&logoColor=white)
![Actuator](https://img.shields.io/badge/Spring%20Boot%20Actuator-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)

#### API Documentation

![Swagger](https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)

#### Build Tool

![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white)

#### Test

![JUnit5](https://img.shields.io/badge/JUnit5-25A162?style=for-the-badge&logo=junit5&logoColor=white)
![Mockito](https://img.shields.io/badge/Mockito-78A641?style=for-the-badge)
![Testcontainers](https://img.shields.io/badge/Testcontainers-MySQL-blue?style=for-the-badge)

> Spring Security와 Lombok은 사용하지 않았습니다.

---

## 실행 방법

#### 실행 환경

- Java 17
- Gradle 8.14.3
- Spring Boot 3.5.6
- Docker
- MySQL 8.0.34
- MinIO

#### MySQL 컨테이너 실행

```bash
docker run -d --name coreboard-mysql \
  -e MYSQL_ROOT_PASSWORD=password \
  -e MYSQL_DATABASE=CoreBoard \
  -p 3306:3306 \
  --restart=always mysql:8.0.34
```

<details>
<summary>Windows PowerShell</summary>

```powershell
docker run -d --name coreboard-mysql `
  -e MYSQL_ROOT_PASSWORD=password `
  -e MYSQL_DATABASE=CoreBoard `
  -p 3306:3306 `
  --restart=always mysql:8.0.34
```

</details>

#### MinIO 컨테이너 실행

```bash
docker run -d --name coreboard-minio \
  -p 9000:9000 \
  -p 9001:9001 \
  -e MINIO_ROOT_USER=minioadmin \
  -e MINIO_ROOT_PASSWORD=minioadmin \
  minio/minio server /data --console-address ":9001"
```

MinIO 관리자 화면은 아래 주소로 접속합니다.

```text
http://localhost:9001
```

#### 애플리케이션 실행

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

#### API 명세 확인

```text
http://localhost:8080/swagger-ui/index.html
```

#### 모니터링 실행

```bash
cd src/main/monitoring
docker compose -f docker-compose.monitoring.yml up -d
```

Prometheus는 `/actuator/prometheus`를 15초마다 수집하도록 구성되어 있습니다.

```text
Prometheus: http://localhost:9090
Grafana: http://localhost:3000
```

---

## API 목록

로컬 실행 후 Swagger UI에서 전체 요청/응답 예시를 확인할 수 있습니다.

```text
http://localhost:8080/swagger-ui/index.html
```

#### Admin API

| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| POST | `/admin/setup` | 최초 관리자 계정 생성 | 불필요 |
| GET | `/admin/users` | 사용자 목록 조회 | ADMIN |
| PATCH | `/admin/users/{id}/role` | 사용자 권한 변경 | ADMIN |

#### Auth API

| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| POST | `/auth/users` | 회원가입 | 불필요 |
| POST | `/auth/token` | 로그인, AccessToken 발급, RefreshToken 쿠키 저장 | 불필요 |
| POST | `/auth/refresh` | RefreshToken 쿠키로 AccessToken 재발급 | 불필요 |
| DELETE | `/auth/refresh` | RefreshToken 쿠키 삭제 | 불필요 |

#### Board API

| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| POST | `/admin/boards` | 게시판 생성 | ADMIN |
| GET | `/boards` | 게시판 목록 조회 | 불필요 |
| GET | `/boards/{id}` | 게시판 단건 조회와 해당 게시판의 게시글 목록 조회 | 불필요 |
| PATCH | `/admin/boards/{id}` | 게시판 수정 | ADMIN |
| DELETE | `/admin/boards/{id}` | 게시판 삭제 요청 | ADMIN |

#### Post API

| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| POST | `/boards/{boardId}/posts` | 특정 게시판에 게시글 생성 | USER 또는 ADMIN |
| GET | `/boards/{boardId}/posts` | 특정 게시판 게시글 목록 조회 | 불필요 |
| GET | `/boards/{boardId}/posts?keyword={keyword}` | 특정 게시판 안에서 검색어로 게시글 목록 조회 | 불필요 |
| GET | `/posts/{id}` | 게시글 단건 조회 | 불필요 |
| PUT | `/posts/{id}` | 게시글 수정 | 작성자 또는 ADMIN |
| DELETE | `/posts/{id}` | 게시글 삭제 | 작성자 |

#### Comment API

| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| POST | `/posts/{postId}/comments` | 댓글 생성 | USER 또는 ADMIN |
| GET | `/posts/{postId}/comments?page={page}&size={size}` | 댓글 목록 조회 | 불필요 |
| PATCH | `/posts/{postId}/comments/{id}` | 댓글 수정 | 작성자 |
| DELETE | `/posts/{postId}/comments/{id}` | 댓글 삭제 | 작성자 |

#### Attachment API

| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| POST | `/attachments` | 게시글 저장 전 첨부파일 임시 업로드 | USER 또는 ADMIN |

---

## 주요 설계 정리

#### 1. 인증과 역할 기반 권한

- JWT 기반 인증을 사용합니다.
- AccessToken은 응답 본문으로 반환하고, RefreshToken은 HttpOnly 쿠키에 저장합니다.
- `GET` 요청은 비로그인 사용자도 접근할 수 있도록 했습니다.
- `/admin`으로 시작하는 요청은 Interceptor에서 ADMIN 권한을 검사합니다.
- 최초 ADMIN 계정은 관리자 초기 설정 API를 통해 1번만 생성할 수 있습니다.
- ADMIN 계정이 1명 이하일 때는 마지막 ADMIN을 일반 USER로 변경할 수 없도록 막았습니다.
- 게시판 작성 권한은 게시판별 설정값에 따라 USER 또는 ADMIN으로 제한할 수 있습니다.

#### 2. 게시판 운영 정책

- 게시판은 코드에 고정하지 않고 DB 데이터로 관리합니다.
- 게시판마다 댓글 허용 여부, 답변 채택 허용 여부, 첨부파일 필수 여부, 첨부파일 최대 개수, 작성 가능 권한을 가질 수 있습니다.
- 게시판 삭제는 실제 삭제가 아니라 `deletedAt`을 기록하는 방식으로 처리합니다.
- 게시글이 존재하는 게시판은 삭제할 수 없도록 막았습니다.

#### 3. 게시글과 댓글

- 게시글은 게시판에 속하며, 상태는 `PUBLISHED`, `DELETED`로 관리합니다.
- 게시글 목록은 게시판 단위로 조회하고, 검색어가 있으면 제목 또는 본문 기준으로 목록을 좁힙니다.
- 검색 범위는 현재 게시판과 `PUBLISHED` 상태 게시글로 제한합니다.
- 게시글 수정은 제목, 본문, 본문 형식, 첨부파일 유지/추가 목록을 함께 다룹니다.
- 댓글은 게시글에 속하며, 댓글 목록은 Slice 기반으로 조회합니다.
- 삭제된 게시글이나 댓글이 비활성화된 게시판에는 댓글을 작성할 수 없습니다.

#### 4. 첨부파일

- 첨부파일은 게시글 저장 전에 먼저 임시 업로드합니다.
- 임시 업로드 상태는 `TEMP`, 게시글 연결 후에는 `CONFIRMED` 상태로 관리합니다.
- 게시글 수정 중 유지하지 않은 기존 첨부파일은 `DELETED` 상태로 변경합니다.
- 24시간이 지난 `TEMP` 첨부파일과 7일이 지난 `DELETED` 첨부파일은 스케줄러로 정리합니다.
- 파일 본문은 DB에 저장하지 않고 S3 호환 스토리지에 저장합니다.

#### 5. 응답과 검증

- 성공 응답은 `ApiResponse` 형식으로 통일했습니다.
- 목록 응답은 `OffsetPageResponse`, `SliceResponse`로 구분했습니다.
- 요청값 검증은 Controller 계층의 Validator에서 먼저 수행합니다.
- Service는 사용자 조회, 권한 확인, 도메인 상태 확인, 저장 흐름을 담당합니다.

---

## 데이터 모델 설명

#### Users

사용자 정보와 인증 정보를 관리합니다.

| 필드 | 타입 | 설명 |
|------|------|------|
| userId | Long | 사용자 ID |
| username | String | 로그인 아이디 |
| nickname | String | 사용자 표시 이름 |
| password | String | 암호화된 비밀번호 |
| email | String | 암호화된 이메일 |
| phoneNumber | String | 암호화된 휴대폰번호 |
| role | UserRole | USER 또는 ADMIN |

#### Board

게시판 메뉴와 게시판별 정책을 관리합니다.

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | 게시판 ID |
| name | String | 화면에 표시되는 게시판 이름 |
| slug | String | 시스템에서 사용하는 게시판 식별자 |
| commentEnabled | boolean | 댓글 허용 여부 |
| answerAcceptedEnabled | boolean | 답변 채택 허용 여부 |
| requireAttachment | boolean | 첨부파일 필수 여부 |
| maxAttachmentCount | int | 첨부파일 최대 개수 |
| allowedWriteRoles | UserRole | 게시글 작성 가능 권한 |
| deletedAt | LocalDateTime | 게시판 삭제 시각 |

#### Post

게시판에 작성되는 게시글입니다.

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | 게시글 ID |
| board | Board | 소속 게시판 |
| user | Users | 작성자 |
| title | String | 제목 |
| content | String | 본문 |
| contentFormat | ContentFormat | 본문 형식 |
| status | PostStatus | PUBLISHED 또는 DELETED |
| viewCount | Long | 조회수 저장용 필드 |
| createdAt | LocalDateTime | 생성 시각 |
| updatedAt | LocalDateTime | 수정 시각 |

#### Comment

게시글에 작성되는 댓글입니다.

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | 댓글 ID |
| post | Post | 댓글이 속한 게시글 |
| user | Users | 댓글 작성자 |
| content | String | 댓글 내용 |
| status | CommentStatus | ACTIVE 또는 DELETED |
| createdDate | LocalDateTime | 생성 시각 |
| lastModifiedDate | LocalDateTime | 수정 시각 |

#### Attachment

게시글 첨부파일의 메타데이터를 관리합니다.

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | 첨부파일 ID |
| user | Users | 업로드한 사용자 |
| post | Post | 연결된 게시글 |
| originalFileName | String | 원본 파일명 |
| objectKey | String | 스토리지 객체 키 |
| storeUrl | String | 스토리지 접근 URL |
| contentType | String | 파일 형식 |
| fileSize | Long | 파일 크기 |
| status | AttachmentStatus | TEMP, CONFIRMED, DELETED |
| createdAt | LocalDateTime | 생성 시각 |
| deletedAt | LocalDateTime | 삭제 처리 시각 |

#### 관계

- Board : Post = 1 : N
- Users : Post = 1 : N
- Users : Comment = 1 : N
- Post : Comment = 1 : N
- Post : Attachment = 1 : N
- Users : Attachment = 1 : N

---

## Architecture Decision Records

프로젝트의 주요 설계 결정은 ADR로 기록합니다.

| ADR | 내용 |
|-----|------|
| [ADR-0001](docs/adr/0001-manage-boards-as-data.md) | 운영 중 변경 가능한 게시판 메뉴를 DB 데이터로 관리한 이유 |
| [ADR-0002](docs/adr/0002-pagination-strategy-by-use-case.md) | 조회 목적별 페이지네이션 전략을 분리한 이유 |
| [ADR-0003](docs/adr/0003-post-view-count-strategy.md) | 게시글 조회수 저장 방식과 중복 조회 처리 기준 |
| [ADR-0004](docs/adr/0004-name-slug-separation.md) | 게시판 표시 이름과 시스템 식별자를 분리한 이유 |
| [ADR-0005](docs/adr/0005-admin-policy-for-board-management.md) | 관리자 초기 생성 및 권한 전환 정책 |
| [ADR-0006](docs/adr/0006-auth-with-handler-interceptor.md) | Spring Security 대신 HandlerInterceptor로 인증 흐름을 직접 구현한 이유 |
| [ADR-0007](docs/adr/0007-request-command-response-dto.md) | DTO를 요청, 명령, 결과, 응답으로 분리한 이유 |
| [ADR-0008](docs/adr/0008-password-and-personal-data-encryption.md) | 비밀번호와 개인정보 암호화 전략을 분리한 이유 |
| [ADR-0009](docs/adr/0009-refresh-token-http-only-cookie.md) | RefreshToken을 HttpOnly 쿠키에 저장한 이유 |
| [ADR-0010](docs/adr/0010-use-testcontainers-instead-of-h2.md) | H2 대신 Testcontainers를 선택한 이유 |

---

## 테스트 실행 방법

```bash
./gradlew test
```

테스트는 컨트롤러 테스트, 서비스 단위 테스트, 유틸/설정 테스트, Testcontainers 기반 통합 테스트로 구성되어 있습니다.

| 종류 | 설명 |
|------|------|
| 컨트롤러 테스트 | MockMvc 기반 요청, 응답, 검증 실패 흐름 확인 |
| 서비스 테스트 | Mockito 기반 비즈니스 흐름, 권한, 예외, 저장 대상 검증 |
| 유틸/설정 테스트 | JWT, 암호화, 설정 객체 검증 |
| 통합 테스트 | Testcontainers MySQL 기반 주요 흐름 검증 |

커버리지 리포트는 아래 명령어로 확인할 수 있습니다.

```bash
./gradlew test jacocoTestReport
```
