# Load Test

## 1. 테스트 목적

Scenario C에서 로그인 후 게시글 작성, 작성 게시글 조회, 댓글 작성, 댓글 조회 흐름이 부하 상황에서 안정적으로 동작하는지 확인한다.

특히 쓰기 요청이 포함된 흐름에서 게시글 작성 API의 응답 시간, DB connection 사용량, JVM memory 변화, 오류율을 확인한다.

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
| 2 | 10 | 60초 | 미실행 | - |
| 3 | 20 | 60초 | 미실행 | - |
| 4 | 30 | 60초 | 미실행 | - |

## 4. 실행 API

| 순서 | API | 설명 |
|---:|---|---|
| 1 | POST /auth/token | 로그인 및 accessToken 발급 |
| 2 | GET /boards | 게시판 목록 조회 |
| 3 | POST /boards/2/posts | 게시글 작성 |
| 4 | GET /posts/{postId} | 작성한 게시글 상세 조회 |
| 5 | POST /posts/{postId}/comments | 댓글 작성 |
| 6 | GET /posts/{postId}/comments | 댓글 목록 조회 |

## 5. 단계별 전체 결과

| 단계 | 총 요청 수 | 평균 응답 시간 | p95 | 최소 | 최대 | 오류율 | 처리량 |
|---:|---:|---:|---:|---:|---:|---:|---:|
| 5 users | 300 | 935ms | 3798ms | 2ms | 18400ms | 0.00% | 3.2/sec |
| 10 users | - | - | - | - | - | - | - |
| 20 users | - | - | - | - | - | - | - |
| 30 users | - | - | - | - | - | - | - |

## 6. API별 결과

| API | 평균 응답 시간 | p95 | 최소 | 최대 | 오류율 | 처리량 | 비고 |
|---|---:|---:|---:|---:|---:|---:|---|
| POST /auth/token | 295ms | 313ms | 256ms | 314ms | 0.00% | 33.9/min | 정상 |
| GET /boards | 4ms | 5ms | 2ms | 19ms | 0.00% | 34.0/min | 정상 |
| POST /boards/2/posts | 5270ms | 13824ms | 3446ms | 18400ms | 0.00% | 32.5/min | 병목 발생 |
| GET /posts/{postId} | 13ms | 18ms | 5ms | 82ms | 0.00% | 33.9/min | 정상 |
| POST /posts/{postId}/comments | 27ms | 30ms | 15ms | 115ms | 0.00% | 33.9/min | 정상 |
| GET /posts/{postId}/comments | 4ms | 6ms | 3ms | 10ms | 0.00% | 33.9/min | 정상 |

## 7. 모니터링 결과

### 7.1 CPU

| 단계 | 관찰 결과 | 특이사항 |
|---|---|---|
| 5 users | CPU 사용률은 낮은 수준으로 유지됨 | CPU 병목으로 보기는 어려움 |
| 10 users | 미실행 | - |
| 20 users | 미실행 | - |
| 30 users | 미실행 | - |

### 7.2 JVM Memory

| 단계 | 관찰 결과 | 특이사항 |
|---|---|---|
| 5 users | Heap 사용량은 증가 후 감소하는 패턴을 보임 | GC로 회수되는 흐름으로 보이며 즉시 OOM 징후는 없음 |
| 10 users | 미실행 | - |
| 20 users | 미실행 | - |
| 30 users | 미실행 | - |

### 7.3 DB Connection

| 단계 | 관찰 결과 | 특이사항 |
|---|---|---|
| 5 users | Active connection이 최대 5개까지 증가함 | 사용자 수 5명과 유사하게 증가 |
| 10 users | 미실행 | - |
| 20 users | 미실행 | - |
| 30 users | 미실행 | - |

### 7.4 Swap

| 단계 | 관찰 결과 | 특이사항 |
|---|---|---|
| 5 users | 별도 급증 확인 없음 | 현재 단계에서는 Swap 병목으로 판단하지 않음 |
| 10 users | 미실행 | - |
| 20 users | 미실행 | - |
| 30 users | 미실행 | - |

## 8. 중단 기준 발생 여부

| 항목 | 기준 | 발생 여부 | 비고 |
|---|---|---|---|
| p95 응답 시간 | 3초 초과 지속 | 발생 | 전체 p95 3798ms, 게시글 작성 p95 13824ms |
| 5xx 오류 | 0% 초과 | 미발생 | 오류율 0.00% |
| CPU | 80% 이상 지속 | 미발생 | CPU 사용률 낮음 |
| Memory | available 급감 | 미확인 | Heap은 증가 후 회수 패턴 |
| Swap | 사용량 급증 | 미발생 | 특이사항 없음 |
| HikariCP | pending connection 발생 | 미발생 | pending connection 0 유지 |

## 9. 결과 해석

5 users 단계에서 전체 오류율은 0.00%로 기능 흐름은 정상 동작했다.

하지만 전체 p95 응답 시간이 3798ms로 공통 중단 기준인 3초를 초과했다. 병목은 전체 API가 아니라 `POST /boards/2/posts` 게시글 작성 API에 집중되어 있다.

게시글 작성 API는 평균 5270ms, p95 13824ms, 최대 18400ms로 측정되었다. 반면 로그인, 게시판 목록 조회, 작성글 상세 조회, 댓글 작성, 댓글 조회 API는 모두 비교적 낮은 응답 시간을 보였다.

모니터링 지표상 CPU 사용률은 낮았고, HikariCP pending connection도 발생하지 않았다. 따라서 현재 단계에서는 CPU 부족이나 DB connection pool 고갈보다는 게시글 작성 API 내부 처리, DB INSERT 전후 검증 로직, 중복 검사, 트랜잭션 구간, 관련 쿼리 실행 시간이 주요 병목 후보로 보인다.

## 10. 결론

Scenario C의 5 users 부하 테스트 결과, 전체 요청은 오류 없이 성공했지만 게시글 작성 API에서 명확한 응답 지연이 발생했다.

공통 중단 기준인 p95 3초 초과가 발생했으므로 10 users, 20 users, 30 users 단계로 확장하지 않고 병목 분석으로 전환한다.

다음 단계에서는 게시글 작성 API의 검증 로직, title 중복 검사 쿼리, attachment 관련 처리 여부, 트랜잭션 범위, INSERT 실행 시간을 중심으로 원인을 좁힌다.