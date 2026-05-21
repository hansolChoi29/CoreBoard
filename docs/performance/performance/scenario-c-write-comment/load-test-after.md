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

### 9.1 단계별 해석

| 단계 | 핵심 결과 | 해석 |
|---|---|---|
| 5 users | 총 300건 성공, 오류율 0.00%, p95 284ms | 중단 기준인 p95 3초를 크게 밑돌았다. |
| 10 users | 총 600건 성공, 오류율 0.00%, p95 274ms | 요청 수가 증가했지만 응답 시간은 안정적이었다. |
| 20 users | 총 1,200건 성공, 오류율 0.00%, p95 258ms | 처리량 증가에도 지연은 발생하지 않았다. |
| 30 users | 총 1,800건 성공, 오류율 0.00%, p95 290ms | 가장 높은 부하에서도 안정적으로 처리되었다. |

### 9.2 API 관점

| API | 최종 30 users 기준 | 해석 |
|---|---|---|
| POST `/auth/token` | 평균 279ms, p95 301ms | 시나리오 내에서 가장 느린 API였지만 중단 기준에는 한참 못 미쳤다. |
| GET `/boards` | 평균 2ms, p95 3ms | 게시판 목록 조회는 안정적이었다. |
| POST `/boards/1/posts` | 평균 12ms, p95 16ms | 기존 병목이었던 게시글 작성 지연은 재현되지 않았다. |
| GET `/posts/{postId}` | 평균 4ms, p95 5ms | 작성 후 상세 조회는 안정적이었다. |
| POST `/posts/{postId}/comments` | 평균 11ms, p95 14ms | 댓글 작성도 안정적이었다. |
| GET `/posts/{postId}/comments` | 평균 3ms, p95 4ms | 댓글 조회는 가장 낮은 응답 시간을 보였다. |

### 9.3 개선 효과

| 개선 대상 | 개선 전 문제 | 개선 후 결과 |
|---|---|---|
| 게시글 작성 API | title 중복 검사에서 인덱스 부재로 지연 발생 | 30 users 기준 p95 16ms 유지 |
| `post.title` 조회 | 풀스캔 가능성 | UNIQUE INDEX 기반 조회로 개선 |
| 전체 시나리오 | 게시글 작성 API가 전체 응답 시간을 끌어올림 | 30 users 기준 전체 p95 290ms 유지 |

### 9.4 자원 사용률 관점

| 항목 | 결과 | 해석 |
|---|---|---|
| CPU | 낮은 수준 유지 | 순간적인 spike는 있었지만 병목은 아님 |
| JVM Memory | 증가 후 GC 회수 | OOM 또는 GC 병목 징후 없음 |
| DB Connection Active | 30 users 기준 순간적으로 2 수준 | 커넥션 사용량 안정적 |
| DB Connection Pending | 모든 단계 0 | 커넥션 풀 고갈 없음 |
| Swap | 급증 없음 | 메모리 부족 징후 없음 |

### 9.5 종합 판단

| 항목 | 판단 |
|---|---|
| 오류율 | 모든 단계 0.00% |
| p95 기준 | 모든 단계에서 3초 기준 통과 |
| 병목 재현 여부 | 게시글 작성 API의 기존 지연 재현 안 됨 |
| 개선 효과 | `post.title` UNIQUE INDEX 적용 효과 유지 |

## 10. 결론

Scenario C 부하 테스트는 통과로 판단한다.

5 users부터 30 users까지 모든 단계에서 오류율은 0.00%였다.

최종 30 users 기준 전체 p95는 290ms였다.

기존 병목이었던 게시글 작성 API는 30 users 기준 p95 16ms로 유지되었다.

따라서 `post.title` UNIQUE INDEX 적용 후 게시글 작성 성능 개선 효과가 부하 증가 상황에서도 유지되었다고 판단한다.