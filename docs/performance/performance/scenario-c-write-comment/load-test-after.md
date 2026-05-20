# Load Test

## 1. 테스트 목적

Scenario C에서 로그인 후 게시글 작성과 댓글 작성이 포함된 흐름을 대상으로 API 응답 시간과 자원 사용률을 확인한다.

이전 테스트에서 `POST /boards/{boardId}/posts` 게시글 작성 API가 크게 지연되었기 때문에, `post.title` UNIQUE INDEX 적용 후 동일 조건에서 응답 시간이 개선되었는지 확인한다.

## 2. 테스트 대상 시나리오

| 항목 | 내용 |
|---|---|
| 시나리오명 | Scenario C - 로그인 사용자의 글 작성 및 댓글 작성 |
| 테스트 환경 | local |
| 실행 일자 | 2026-05-20 |
| 실행 도구 | JMeter |
| 모니터링 도구 | Prometheus / Grafana |

## 3. 부하 조건

| 단계 | Number of Threads | Ramp-up Period | Duration / Loop Count | 예상 요청 수 |
|---:|---:|---:|---:|---:|
| 1 | 5 | 60초 | Loop Count 10 | 300 |
| 2 | 10 | 60초 | Loop Count 10 | 600 |
| 3 | 20 | 60초 | Loop Count 10 | 1200 |
| 4 | 30 | 60초 | Loop Count 10 | 1800 |

## 4. 실행 API

| 순서 | API | 설명 |
|---:|---|---|
| 1 | POST /auth/token | 로그인 및 accessToken 발급 |
| 2 | GET /boards | 게시판 목록 조회 |
| 3 | POST /boards/1/posts | 첨부파일 없는 게시글 작성 |
| 4 | GET /posts/{postId} | 작성한 게시글 상세 조회 |
| 5 | POST /posts/{postId}/comments | 작성한 게시글에 댓글 작성 |
| 6 | GET /posts/{postId}/comments | 댓글 목록 조회 |

## 5. 단계별 전체 결과

| 단계 | 총 요청 수 | 평균 응답 시간 | p95 | 최소 | 최대 | 오류율 | 처리량 |
|---:|---:|---:|---:|---:|---:|---:|---:|
| 5 users | 300 | 53ms | 284ms | 2ms | 311ms | 0.00% | 5.9/sec |
| 10 users | 600 | 50ms | 274ms | 2ms | 336ms | 0.00% | 10.5/sec |
| 20 users | 1200 | 48ms | 258ms | 2ms | 290ms | 0.00% | 20.0/sec |
| 30 users | 1800 | 52ms | 290ms | 1ms | 319ms | 0.00% | 29.5/sec |

## 6. API별 결과

### 6.1 5 users

| API | 평균 응답 시간 | p95 | 최소 | 최대 | 오류율 | 처리량 | 비고 |
|---|---:|---:|---:|---:|---:|---:|---|
| POST /auth/token | 271ms | 297ms | 252ms | 311ms | 0.00% | 58.9/min | 로그인 요청 안정적 |
| GET /boards | 3ms | 4ms | 2ms | 8ms | 0.00% | 59.2/min | 게시판 목록 조회 정상 |
| POST /boards/1/posts | 18ms | 31ms | 12ms | 34ms | 0.00% | 59.2/min | title UNIQUE INDEX 적용 후 정상 |
| GET /posts/{postId} | 5ms | 8ms | 4ms | 17ms | 0.00% | 59.2/min | 상세 조회 정상 |
| POST /posts/{postId}/comments | 16ms | 29ms | 11ms | 35ms | 0.00% | 59.2/min | 댓글 작성 정상 |
| GET /posts/{postId}/comments | 3ms | 5ms | 2ms | 7ms | 0.00% | 59.2/min | 댓글 조회 정상 |

### 6.2 10 users

| API | 평균 응답 시간 | p95 | 최소 | 최대 | 오류율 | 처리량 | 비고 |
|---|---:|---:|---:|---:|---:|---:|---|
| POST /auth/token | 267ms | 287ms | 252ms | 336ms | 0.00% | 1.8/sec | 로그인 요청 안정적 |
| GET /boards | 2ms | 3ms | 2ms | 4ms | 0.00% | 1.8/sec | 게시판 목록 조회 정상 |
| POST /boards/1/posts | 14ms | 17ms | 11ms | 36ms | 0.00% | 1.8/sec | title UNIQUE INDEX 적용 후 정상 |
| GET /posts/{postId} | 4ms | 5ms | 3ms | 6ms | 0.00% | 1.8/sec | 상세 조회 정상 |
| POST /posts/{postId}/comments | 12ms | 14ms | 10ms | 24ms | 0.00% | 1.8/sec | 댓글 작성 정상 |
| GET /posts/{postId}/comments | 3ms | 4ms | 2ms | 8ms | 0.00% | 1.8/sec | 댓글 조회 정상 |

### 6.3 20 users

| API | 평균 응답 시간 | p95 | 최소 | 최대 | 오류율 | 처리량 | 비고 |
|---|---:|---:|---:|---:|---:|---:|---|
| POST /auth/token | 258ms | 268ms | 250ms | 290ms | 0.00% | 3.3/sec | 로그인 요청 안정적 |
| GET /boards | 2ms | 3ms | 2ms | 4ms | 0.00% | 3.4/sec | 게시판 목록 조회 정상 |
| POST /boards/1/posts | 13ms | 16ms | 10ms | 19ms | 0.00% | 3.4/sec | title UNIQUE INDEX 적용 후 정상 |
| GET /posts/{postId} | 3ms | 5ms | 3ms | 8ms | 0.00% | 3.4/sec | 상세 조회 정상 |
| POST /posts/{postId}/comments | 11ms | 15ms | 9ms | 23ms | 0.00% | 3.4/sec | 댓글 작성 정상 |
| GET /posts/{postId}/comments | 3ms | 4ms | 2ms | 5ms | 0.00% | 3.4/sec | 댓글 조회 정상 |

### 6.4 30 users

| API | 평균 응답 시간 | p95 | 최소 | 최대 | 오류율 | 처리량 | 비고 |
|---|---:|---:|---:|---:|---:|---:|---|
| POST /auth/token | 279ms | 301ms | 249ms | 319ms | 0.00% | 4.9/sec | 로그인 요청 안정적 |
| GET /boards | 2ms | 3ms | 1ms | 4ms | 0.00% | 4.9/sec | 게시판 목록 조회 정상 |
| POST /boards/1/posts | 12ms | 16ms | 10ms | 20ms | 0.00% | 4.9/sec | title UNIQUE INDEX 적용 후 정상 |
| GET /posts/{postId} | 4ms | 5ms | 3ms | 12ms | 0.00% | 4.9/sec | 상세 조회 정상 |
| POST /posts/{postId}/comments | 11ms | 14ms | 9ms | 26ms | 0.00% | 4.9/sec | 댓글 작성 정상 |
| GET /posts/{postId}/comments | 3ms | 4ms | 2ms | 6ms | 0.00% | 4.9/sec | 댓글 조회 정상 |

## 7. 모니터링 결과

### 7.1 CPU

| 단계 | 관찰 결과 | 특이사항 |
|---|---|---|
| 5 users | 낮은 수준 유지 | 순간적인 작은 spike는 있었으나 병목으로 보기 어려움 |
| 10 users | 낮은 수준 유지 | 순간적인 spike는 있었으나 지속되지 않아 CPU 병목으로 보기 어려움 |
| 20 users | 낮은 수준 유지 | 순간적인 spike는 있었으나 10% 미만 수준으로 CPU 병목 징후 없음 |
| 30 users | 낮은 수준 유지 | 순간적으로 약 15% 수준까지 상승했으나 지속되지 않아 CPU 병목으로 보기 어려움 |

### 7.2 JVM Memory

| 단계 | 관찰 결과 | 특이사항 |
|---|---|---|
| 5 users | Heap 사용량 증가 후 GC로 감소 | OOM 또는 GC 병목 징후 없음 |
| 10 users | Heap 사용량 증가 후 GC로 감소 | OOM 또는 GC 병목 징후 없음 |
| 20 users | Heap 사용량 증가 후 GC로 감소 | OOM 또는 GC 병목 징후 없음 |
| 30 users | Heap 사용량 증가 후 GC로 감소 | OOM 또는 GC 병목 징후 없음 |

### 7.3 DB Connection

| 단계 | 관찰 결과 | 특이사항 |
|---|---|---|
| 5 users | Active connection은 순간적으로 1 수준 | Pending connection 0으로 connection pool 고갈 없음 |
| 10 users | Active connection은 순간적으로 1 수준 | Pending connection 0으로 connection pool 고갈 없음 |
| 20 users | Active connection은 순간적으로 1 수준 | Pending connection 0으로 connection pool 고갈 없음 |
| 30 users | Active connection은 순간적으로 2 수준 | Pending connection 0으로 connection pool 고갈 없음 |

### 7.4 Swap

| 단계 | 관찰 결과 | 특이사항 |
|---|---|---|
| 5 users | 급증 확인 없음 | 메모리 부족 징후 없음 |
| 10 users | 급증 확인 없음 | 메모리 부족 징후 없음 |
| 20 users | 급증 확인 없음 | 메모리 부족 징후 없음 |
| 30 users | 급증 확인 없음 | 메모리 부족 징후 없음 |

## 8. 중단 기준 발생 여부

| 항목 | 기준 | 발생 여부 | 비고 |
|---|---|---|---|
| p95 응답 시간 | 3초 초과 지속 | 미발생 | 30 users 기준 전체 p95 290ms |
| 5xx 오류 | 0% 초과 | 미발생 | 전체 오류율 0.00% |
| CPU | 80% 이상 지속 | 미발생 | 낮은 수준 유지 |
| Memory | available 급감 | 미발생 | JVM Heap 회수 패턴 확인 |
| Swap | 사용량 급증 | 미발생 | 급증 확인 없음 |
| HikariCP | pending connection 발생 | 미발생 | pending connection 0 |

## 9. 결과 해석

Scenario C는 5 users부터 30 users까지 모든 단계에서 전체 오류율 0.00%로 정상 처리되었다.

전체 p95는 5 users 284ms, 10 users 274ms, 20 users 258ms, 30 users 290ms로 측정되었다. 모든 단계에서 중단 기준인 3초를 초과하지 않았다.

이전 병목 구간이었던 게시글 작성 API는 30 users 조건에서도 평균 12ms, p95 16ms로 측정되었다. 이는 `post.title` UNIQUE INDEX 적용 후 title 중복 검사 쿼리가 풀스캔이 아니라 인덱스 기반 조회로 처리되었기 때문으로 판단한다.

CPU는 동시 사용자가 증가하면서 순간적인 spike가 있었지만 지속적인 고사용률은 아니었다. JVM Heap은 증가 후 GC로 회수되는 패턴을 보였고, OOM 또는 GC 병목 징후는 확인되지 않았다.

DB Connection Active는 30 users 기준 순간적으로 2 수준까지 증가했으나, Pending connection은 모든 단계에서 0으로 유지되었다. 따라서 connection pool 고갈 문제는 발생하지 않았다.

## 10. 결론

Scenario C 5 users부터 30 users까지의 부하 테스트 결과, 로그인, 게시글 작성, 게시글 상세 조회, 댓글 작성, 댓글 조회 흐름은 모두 안정적으로 처리되었다.

이전에는 title 중복 검사 쿼리의 인덱스 부재로 인해 게시글 작성 API p95가 13초 이상 증가했으나, `post.title`에 `uk_post_title` UNIQUE INDEX를 적용한 뒤 30 users 조건에서도 p95는 16ms로 유지되었다.

전체 시나리오 기준으로도 30 users / Ramp-up 60초 / Loop Count 10에서 p95는 290ms였고, 오류율은 0.00%였다.

따라서 `post.title` UNIQUE INDEX 적용 후 게시글 작성 성능 개선 효과가 30 users 조건까지 유지된 것으로 판단한다. 현재 로컬 테스트 기준에서 Scenario C의 주요 API는 중단 기준 없이 안정적으로 동작한다.