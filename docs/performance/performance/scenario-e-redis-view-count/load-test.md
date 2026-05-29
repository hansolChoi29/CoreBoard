# Load Test - Scenario E Redis 조회수 및 인기글 흐름

## 1. 테스트 목적

Scenario E - Redis 조회수 및 인기글 흐름에서 Redis 기반 조회수 기록과 인기글 조회 기능이 게시글 조회 성능에 미치는 영향을 확인한다.

게시글 상세 조회는 기존에는 DB에서 게시글을 조회한 뒤 응답하는 흐름이었지만, 조회수 기능 추가 이후 Redis에 조회수 기록을 함께 수행한다.

조회수 기록은 Redis TTL 기반 중복 조회 방지, 조회수 delta 적립, 인기글 점수 증가 흐름을 포함한다.

따라서 Redis 기록 로직이 게시글 상세 조회 응답 시간을 크게 증가시키는지 확인한다.

또한 인기글 조회는 Redis ZSET에서 게시글 ID를 조회한 뒤 DB에서 게시글 정보를 조회하는 구조이므로, 인기글 조회 응답 시간과 DB Connection 상태를 함께 확인한다.

단, 인기글 조회의 작성자 조회 N+1 여부는 응답 시간만으로 단정할 수 없으므로 Hibernate SQL 로그를 통해 별도로 확인한다.

---

## 2. 테스트 대상 시나리오

| 항목 | 내용 |
|---|---|
| 시나리오명 | Scenario E - Redis 조회수 및 인기글 흐름 |
| 테스트 환경 | local |
| 실행 URL | http://localhost:8080 |
| 실행 일자 | 2026-05-27 |
| 실행 도구 | JMeter |
| 모니터링 도구 | Prometheus / Grafana |
| 주요 확인 대상 | Redis 조회수 기록, Redis 인기글 조회, DB Connection 상태 |

---

## 3. 부하 조건

| 단계 | Number of Threads | Ramp-up Period | Duration / Loop Count | 예상 요청 수 |
|---:|---:|---:|---:|---:|
| 1 | 5 | 60초 | Loop Count 10 | 100 |
| 2 | 10 | 60초 | Loop Count 10 | 200 |
| 3 | 20 | 60초 | Loop Count 10 | 400 |
| 4 | 30 | 60초 | Loop Count 10 | 600 |

---

## 4. 실행 API

| 순서 | API | 설명 |
|---:|---|---|
| 1 | GET /posts/1 | 게시글 상세 조회 및 Redis 조회수 기록 |
| 2 | GET /posts/popular?size=10 | Redis 기반 인기글 목록 조회 |

---

## 5. 단계별 전체 결과

| 단계 | 총 요청 수 | 평균 응답 시간 | p95 | 최소 | 최대 | 오류율 | 처리량 |
|---:|---:|---:|---:|---:|---:|---:|---:|
| 5 users | 100 | 7ms | 11ms | 4ms | 20ms | 0.0% | 2.1/sec |
| 10 users | 200 | 5ms | 9ms | 2ms | 30ms | 0.0% | 3.7/sec |
| 20 users | 400 | 3ms | 7ms | 2ms | 19ms | 0.0% | 7.0/sec |
| 30 users | 600 | 3ms | 7ms | 2ms | 12ms | 0.0% | 10.3/sec |

---

## 6. API별 결과

### 6.1 5 users

| API | 평균 응답 시간 | p95 | 최소 | 최대 | 오류율 | 처리량 | 비고 |
|---|---:|---:|---:|---:|---:|---:|---|
| GET /posts/{postId} | 8ms | 12ms | 6ms | 20ms | 0.0% | 1.0/sec | 정상 |
| GET /posts/popular?size=10 | 5ms | 8ms | 4ms | 8ms | 0.0% | 1.0/sec | 정상 |

### 6.2 10 users

| API | 평균 응답 시간 | p95 | 최소 | 최대 | 오류율 | 처리량 | 비고 |
|---|---:|---:|---:|---:|---:|---:|---|
| GET /posts/{postId} | 6ms | 10ms | 4ms | 30ms | 0.0% | 1.8/sec | 정상 |
| GET /posts/popular?size=10 | 3ms | 6ms | 2ms | 9ms | 0.0% | 1.9/sec | 정상 |

### 6.3 20 users

| API | 평균 응답 시간 | p95 | 최소 | 최대 | 오류율 | 처리량 | 비고 |
|---|---:|---:|---:|---:|---:|---:|---|
| GET /posts/{postId} | 4ms | 8ms | 3ms | 19ms | 0.0% | 3.5/sec | 정상 |
| GET /posts/popular?size=10 | 2ms | 4ms | 2ms | 6ms | 0.0% | 3.5/sec | 정상 |

### 6.4 30 users

| API | 평균 응답 시간 | p95 | 최소 | 최대 | 오류율 | 처리량 | 비고 |
|---|---:|---:|---:|---:|---:|---:|---|
| GET /posts/{postId} | 4ms | 8ms | 3ms | 12ms | 0.0% | 5.2/sec | 정상 |
| GET /posts/popular?size=10 | 2ms | 4ms | 2ms | 11ms | 0.0% | 5.2/sec | 정상 |

---

## 7. 모니터링 결과

### 7.1 CPU

| 단계 | 관찰 결과 | 특이사항 |
|---|---|---|
| 5 users | 낮은 사용률 유지 | CPU 병목 징후 없음 |
| 10 users | 낮은 사용률 유지 | CPU 병목 징후 없음 |
| 20 users | 낮은 사용률 유지 | CPU 병목 징후 없음 |
| 30 users | 낮은 사용률 유지 | CPU 병목 징후 없음 |

### 7.2 JVM Memory

| 단계 | 관찰 결과 | 특이사항 |
|---|---|---|
| 5 users | Heap Memory 증가 후 GC로 감소 | OOM 징후 없음 |
| 10 users | Heap Memory 증가 후 GC로 감소 | OOM 징후 없음 |
| 20 users | Heap Memory 증가 후 GC로 감소 | OOM 징후 없음 |
| 30 users | Heap Memory 증가 후 GC로 감소 | OOM 징후 없음 |

### 7.3 DB Connection

| 단계 | 관찰 결과 | 특이사항 |
|---|---|---|
| 5 users | Active Connection은 거의 0 수준으로 유지됨 | Pending Connection 0 유지 |
| 10 users | Active Connection은 거의 0 수준으로 유지됨 | Pending Connection 0 유지 |
| 20 users | Active Connection은 거의 0 수준으로 유지됨 | Pending Connection 0 유지 |
| 30 users | Active Connection은 거의 0 수준으로 유지됨 | Pending Connection 0 유지 |

### 7.4 Swap

| 단계 | 관찰 결과 | 특이사항 |
|---|---|---|
| 5 users | 별도 확인하지 않음 | 판단 제외 |
| 10 users | 별도 확인하지 않음 | 판단 제외 |
| 20 users | 별도 확인하지 않음 | 판단 제외 |
| 30 users | 별도 확인하지 않음 | 판단 제외 |

---

## 8. 중단 기준 발생 여부

| 항목 | 기준 | 발생 여부 | 비고 |
|---|---|---|---|
| p95 응답 시간 | 3초 초과 지속 | 발생하지 않음 | 30 users 전체 p95 7ms |
| 5xx 오류 | 0% 초과 | 발생하지 않음 | 오류율 0.0% |
| CPU | 80% 이상 지속 | 발생하지 않음 | 높은 사용률 지속 없음 |
| Memory | available 급감 | 단정 불가 | JVM Heap 기준 OOM 징후 없음 |
| Swap | 사용량 급증 | 미확인 | 이번 테스트에서 별도 확인하지 않음 |
| HikariCP | pending connection 발생 | 발생하지 않음 | Pending Connection 0 유지 |

---

## 9. 결과 해석

### 9.1 단계별 해석

| 단계 | 핵심 결과 | 해석 |
|---|---|---|
| 5 users | 총 100건 성공, 오류율 0.0%, p95 11ms | 중단 기준인 p95 3초를 통과했다. |
| 10 users | 총 200건 성공, 오류율 0.0%, p95 9ms | 요청 수가 증가해도 응답 시간은 안정적이었다. |
| 20 users | 총 400건 성공, 오류율 0.0%, p95 7ms | 처리량 증가에 따른 급격한 지연은 없었다. |
| 30 users | 총 600건 성공, 오류율 0.0%, p95 7ms | 가장 높은 부하에서도 응답 시간은 안정적이었다. |

### 9.2 Redis 조회수 기록 영향

| API | 30 users 기준 | 해석 |
|---|---:|---|
| GET /posts/{postId} | 평균 4ms, p95 8ms | Redis 조회수 기록이 포함된 상세 조회에서도 응답 지연은 확인되지 않았다. |

게시글 상세 조회는 Redis 기반 조회수 기록 로직이 포함된 상태에서도 30 users 기준 p95 8ms로 응답했다.

따라서 현재 부하 조건에서는 Redis 조회수 기록이 상세 조회 응답 시간을 크게 증가시키지는 않았다.

### 9.3 인기글 조회 결과

| API | 30 users 기준 | 해석 |
|---|---:|---|
| GET /posts/popular?size=10 | 평균 2ms, p95 4ms | Redis 기반 인기글 조회는 응답 시간 기준으로 안정적이었다. |

인기글 조회는 30 users 기준 p95 4ms로 응답했다.

응답 시간 기준으로는 병목이 확인되지 않았다.

다만 이 결과만으로 작성자 조회 N+1이 없다고 판단할 수는 없다.

N+1은 응답 시간이 아니라 SQL 실행 횟수로 확인해야 하므로, `GET /posts/popular?size=10` 단일 호출 시 Hibernate SQL 로그를 통해 작성자 조회 쿼리 반복 여부를 별도로 확인한다.

### 9.4 API 관점

| API | 30 users 기준 | 해석 |
|---|---|---|
| GET `/posts/{postId}` | 평균 4ms, p95 8ms | Redis 조회수 기록이 포함된 상세 조회는 안정적이었다. |
| GET `/posts/popular?size=10` | 평균 2ms, p95 4ms | 인기글 조회는 응답 시간 기준으로 안정적이었다. |

### 9.5 자원 사용률 관점

| 항목 | 결과 | 해석 |
|---|---|---|
| CPU | 높은 사용률 지속 없음 | CPU 병목 징후 없음 |
| JVM Memory | 증가 후 GC로 감소 | OOM 징후 없음 |
| DB Connection Active | 거의 0 수준 유지 | 커넥션 사용량 안정적 |
| DB Connection Pending | 모든 단계 0 | 커넥션 풀 고갈 없음 |
| Swap | 별도 확인하지 않음 | 판단 제외 |

### 9.6 종합 판단

| 항목 | 판단 |
|---|---|
| 오류율 | 모든 단계 0.0% |
| p95 기준 | 모든 단계에서 3초 기준 통과 |
| Redis 조회수 기록 영향 | 상세 조회 응답 시간 기준 병목 없음 |
| 인기글 조회 | 응답 시간 기준 병목 없음 |
| N+1 여부 | 응답 시간만으로 판단 불가, SQL 로그로 별도 확인 필요 |

---

## 10. 결론

Scenario E 부하 테스트는 통과로 판단한다.

5 users부터 30 users까지 모든 단계에서 오류율은 0.0%였다.

최종 30 users 기준 전체 p95는 7ms였다.

Redis 조회수 기록이 포함된 게시글 상세 조회는 30 users 기준 p95 8ms로 유지되었다.

Redis 기반 인기글 조회는 30 users 기준 p95 4ms로 유지되었다.

따라서 현재 측정한 30 users 조건까지는 Redis 조회수 기록과 인기글 조회가 응답 시간 기준 병목을 만들지 않는다고 판단한다.

다만 인기글 조회의 N+1 여부는 응답 시간만으로 판단할 수 없으므로, Hibernate SQL 로그를 통해 작성자 조회 쿼리 반복 여부를 별도로 확인한다.