# Bottleneck Analysis

## 1. 분석 목적

Scenario C 부하 테스트에서 `POST /boards/{boardId}/posts` 게시글 작성 API의 응답 시간이 크게 증가한 원인을 좁힌다.

전체 오류율은 0.00%였지만, 게시글 작성 API의 평균 응답 시간과 p95가 다른 API에 비해 크게 높게 측정되었다. 따라서 네트워크, JVM, DB connection, 조회 API 병목이 아니라 게시글 생성
API 내부 처리 흐름을 중심으로 원인을 분석한다.

## 2. 분석 대상 시나리오

| 항목        | 내용                                    |
|-----------|---------------------------------------|
| 시나리오명     | Scenario C - 로그인 사용자의 글 작성 및 댓글 작성    |
| 분석 기준 테스트 | 5 users / Ramp-up 60초 / Loop Count 10 |
| 분석 일자     | 2026-05-20                            |
| 참고 문서     | load-test-scenario-c.md               |

## 3. 병목 의심 구간 요약

| 구간                           | 관찰된 현상                                            | 병목 의심 이유                          |
|------------------------------|---------------------------------------------------|-----------------------------------|
| POST /boards/{boardId}/posts | 평균 5270ms, p95 13824ms, 최대 18400ms                | 전체 시나리오 중 유일하게 응답 시간이 크게 증가       |
| 다른 API                       | 로그인, 게시판 목록 조회, 상세 조회, 댓글 작성/조회는 낮은 응답 시간 유지      | 병목이 전체 서버 문제가 아니라 게시글 작성 API에 집중됨 |
| DB Connection                | Active connection은 최대 5개 수준, pending connection 0 | connection pool 고갈 가능성 낮음         |
| CPU                          | 낮은 수준 유지                                          | CPU 연산 병목 가능성 낮음                  |
| JVM Memory                   | Heap 증가 후 감소 패턴                                   | 즉시 GC/OOM 병목으로 보기는 어려움            |

## 4. 응답 시간 관점 분석

| API                           | 평균 응답 시간 변화 |  p95 변화 | 해석               |
|-------------------------------|------------:|--------:|------------------|
| POST /auth/token              |       295ms |   313ms | 로그인 요청은 안정적      |
| GET /boards                   |         4ms |     5ms | 게시판 목록 조회는 병목 아님 |
| POST /boards/{boardId}/posts  |      5270ms | 13824ms | 핵심 병목 구간         |
| GET /posts/{postId}           |        13ms |    18ms | 작성 후 상세 조회는 정상   |
| POST /posts/{postId}/comments |        27ms |    30ms | 댓글 작성은 정상        |
| GET /posts/{postId}/comments  |         4ms |     6ms | 댓글 조회는 정상        |

## 5. 자원 사용률 관점 분석

| 항목            | 관찰 결과                              | 해석                              |
|---------------|------------------------------------|---------------------------------|
| CPU           | 낮은 수준 유지                           | CPU 자체 병목 가능성 낮음                |
| JVM Memory    | Heap 사용량 증가 후 감소                   | GC/OOM 문제로 단정하기 어려움             |
| DB Connection | active connection 최대 5개, pending 0 | connection pool 부족 가능성 낮음       |
| Swap          | 급증 확인 없음                           | 메모리 부족에 따른 swap 병목 가능성 낮음       |
| 기타            | 게시글 작성 API만 지연                     | 공통 인프라보다 API 내부 로직/쿼리 병목 가능성 높음 |

## 6. 병목 후보

| 병목 후보                 | 근거                                                                        | 추가 확인 방법                                              | 우선순위 |
|-----------------------|---------------------------------------------------------------------------|-------------------------------------------------------|------|
| Count Query           | POST 생성 API에는 Page/count query가 직접 관여하지 않음                                | Hibernate SQL 로그에서 count 쿼리 발생 여부 확인                  | 낮음   |
| 정렬 비용                 | POST 생성 API라 ORDER BY 정렬 비용과 직접 관련 낮음                                     | SQL 로그에서 ORDER BY 발생 여부 확인                            | 낮음   |
| N+1 또는 연관 조회          | 생성 과정에서 user, board 조회는 있으나 목록 응답처럼 반복 조회 구조는 아님                          | Hibernate SQL 로그로 한 요청당 쿼리 개수 확인                      | 중간   |
| title 중복 검사           | `existsByTitle(command.title())`가 생성 전마다 실행됨                              | `EXPLAIN SELECT ... WHERE title = ?` 확인, title 인덱스 확인 | 높음   |
| board 조회 / 권한 검사      | 생성 시마다 board를 조회하고 `board.canWrite(user.getRole())` 수행                    | board 조회 SQL 실행 시간 확인                                 | 중간   |
| attachment confirm 처리 | 생성 후 `attachmentService.confirm(command.attachmentIds(), saved, user)` 실행 | attachmentIds가 빈 값일 때도 쿼리가 발생하는지 확인                   | 높음   |
| DB Connection 부족      | pending connection이 0으로 유지됨                                               | HikariCP active/pending 지표 확인                         | 낮음   |
| JVM Memory / GC       | Heap은 회수 패턴을 보임                                                           | GC 로그 또는 Grafana JVM GC 지표 확인                         | 낮음   |
| 네트워크 / Nginx          | local 테스트이며 다른 API는 빠름                                                    | 동일 서버 내 curl 비교                                       | 낮음   |

## 7. 추가 확인 내용

### 7.1 쿼리 확인

게시글 작성 API 1회 호출 시 실제 실행되는 SQL을 확인한다.

확인 대상은 다음과 같다.

```text
1. users 조회 쿼리
2. post title 중복 검사 쿼리
3. board 조회 쿼리
4. post insert 쿼리
5. attachment confirm 관련 쿼리
```

특히 title 중복 검사 쿼리는 게시글 수가 많아질수록 비용이 커질 수 있으므로 실행 계획을 확인한다.

```sql
EXPLAIN
SELECT 1
FROM post
WHERE title = '테스트 제목' LIMIT 1;
```

또는 Hibernate가 실제로 생성한 SQL 기준으로 EXPLAIN을 수행한다.

### 7.2 인덱스 확인

title 중복 검사를 계속 유지한다면 `post.title`에 인덱스가 있는지 확인한다.

```sql
SHOW
INDEX FROM post;
```

확인 포인트는 다음과 같다.

```text
1. title 컬럼 인덱스 존재 여부
2. board_id + title 복합 인덱스 필요 여부
3. status 조건이 함께 들어가는지 여부
```

현재 정책이 “전체 게시글 title 전역 중복 금지”라면 `title` 단일 인덱스가 필요할 수 있다.

하지만 게시판별로 같은 제목을 허용할 수 있다면 `board_id + title` 기준으로 정책을 바꾸는 것도 검토한다.

### 7.3 애플리케이션 로그 확인

게시글 작성 메서드 내부 구간별 시간을 로그로 확인한다.

확인 구간은 다음과 같다.

```text
1. usersRepository.findByUsername
2. postRepository.existsByTitle
3. boardRepository.findById
4. PostAttachmentPolicyValidator.validate
5. postRepository.save
6. attachmentService.confirm
```

### 7.4 Grafana 지표 확인

Grafana에서는 다음 지표를 계속 확인한다.

```text
1. CPU 사용률
2. JVM Heap 사용량
3. HikariCP active connection
4. HikariCP pending connection
5. HTTP 요청 시간
```

현재 5 users 테스트에서는 CPU, JVM, DB connection 고갈 징후보다 게시글 작성 API 내부 처리 지연 가능성이 더 높다.

## 8. 개선 방향

| 개선 대상              | 개선 방향                                             | 기대 효과                                   | 우선순위 |
|--------------------|---------------------------------------------------|-----------------------------------------|------|
| title 중복 검사        | `post.title`에 `uk_post_title` UNIQUE INDEX 추가     | 중복 검사 쿼리의 풀스캔 제거 및 title 중복 정책 DB 레벨 보장 | 높음   |
| 게시글 생성 요청 데이터      | Scenario C에서는 첨부파일을 제외하고 `attachmentIds: []`로 요청  | 첨부파일 재사용/연결 오류 제거                       | 높음   |
| 게시글 생성 로직          | 구간별 실행 시간 로그로 title 중복 검사, board 조회, insert 구간 확인 | 병목 위치를 정확히 분리                           | 높음   |
| attachment confirm | Scenario D에서 별도 검증                                | 첨부파일 조회/확정 비용을 Scenario C와 분리           | 중간   |

## 9. 적용 결과

| 개선 전               |  개선 후 | 변화              |
|--------------------|------:|-----------------|
| 게시글 작성 평균 5270ms   |  18ms | 약 99.7% 감소      |
| 게시글 작성 p95 13824ms |  31ms | 약 99.8% 감소      |
| 게시글 작성 최대 18400ms  |  34ms | 최대 지연 크게 감소     |
| 전체 평균 935ms        |  53ms | 전체 응답 시간 개선     |
| 전체 p95 3798ms      | 284ms | 중단 기준 3초 이하로 회복 |
| 오류율 0.00%          | 0.00% | 오류 없이 유지        |

`post.title`에 `uk_post_title` UNIQUE INDEX를 추가한 뒤 동일 조건으로 Scenario C 5 users / Ramp-up 60초 / Loop Count 10 테스트를 재실행했다.

개선 전에는 `existsByTitle()` 쿼리가 title 조건으로 post 테이블 약 140만 건을 풀스캔했고, 게시글 작성 API의 p95가 13824ms까지 증가했다.

개선 후에는 title 조건 조회가 `uk_post_title` 인덱스 기반으로 처리되었고, 게시글 작성 API의 평균 응답 시간은 18ms, p95는 31ms로 감소했다.

또한 Scenario C에서는 첨부파일 성능을 분리하기 위해 `attachmentIds: []`로 요청했다. 첨부파일이 있는 게시글 조회와 attachment confirm 비용은 Scenario D에서 별도로
측정한다.

따라서 Scenario C의 병목 원인은 게시글 INSERT 자체가 아니라, 게시글 생성 전 수행되는 title 중복 검사 쿼리의 인덱스 부재로 판단한다.

## 10. 결론

Scenario C의 게시글 작성 지연 원인은 `postRepository.existsByTitle()`에서 실행되는 title 중복 검사 쿼리였다.

초기 EXPLAIN 결과, `WHERE title = ?` 조건에 사용할 수 있는 BTREE 인덱스가 없어 MySQL이 post 테이블 약 140만 건을 풀스캔했다. 이로 인해 게시글 작성 API의 평균 응답 시간은
5270ms, p95는 13824ms까지 증가했다.

이후 로컬 테스트 DB를 초기화하고 게시글 1,420,115건을 재생성했다. 게시글 생성 전 `post.title`에 `uk_post_title` UNIQUE INDEX를 먼저 생성하여 title 중복 불가 정책을
DB 레벨에서도 보장했다.

개선 후 동일한 JMeter 조건으로 재측정한 결과, 게시글 작성 API의 평균 응답 시간은 18ms, p95는 31ms로 개선되었다. 전체 p95도 3798ms에서 284ms로 감소하여 중단 기준인 3초 이하로
회복되었다.

따라서 이번 병목은 CPU, JVM memory, DB connection, N+1, 첨부파일 처리 문제가 아니라 title 중복 검사 쿼리의 인덱스 부재 문제로 결론낸다.

현재 인덱스는 로컬 DB에서 SQL로 직접 적용한 상태이므로, 운영 환경까지 반영하려면 Flyway 등 DB migration으로 `uk_post_title` 인덱스 생성 이력을 관리해야 한다.