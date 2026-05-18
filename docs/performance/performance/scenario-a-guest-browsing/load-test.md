# Load Test(무슨 현상이 보였는지)

## 1. 테스트 목적

Scenario A - 비로그인 사용자의 게시판 탐색 흐름에서 동시 사용자 증가에 따른 API 응답 시간, p95, 오류율, 처리량 변화를 확인한다.

특히 게시글 목록 조회는 Page 기반 조회이므로 count query, 정렬, 작성자 조회 과정에서 병목이 발생하는지 관찰한다.

## 2. 테스트 대상 시나리오

| 항목      | 내용                            |
|---------|-------------------------------|
| 시나리오명   | Scenario A - 비로그인 사용자의 게시판 탐색 |
| 테스트 환경  | local                         |
| 실행 일자   | 2026-05-18                    |
| 실행 도구   | JMeter                        |
| 모니터링 도구 | Prometheus / Grafana          |

## 3. 부하 조건

| 단계 | Number of Threads | Ramp-up Period | Duration / Loop Count | 예상 요청 수 |
|---:|------------------:|---------------:|----------------------:|--------:|
|  1 |                 5 |            60초 |         Loop Count 10 |     250 |
|  2 |                10 |            60초 |         Loop Count 10 |     500 |
|  3 |                20 |            60초 |         Loop Count 10 |    1000 |
|  4 |                30 |            60초 |         Loop Count 10 |    1500 |

## 4. 실행 API

| 순서 | API                                               | 설명               |
|---:|---------------------------------------------------|------------------|
|  1 | GET /boards                                       | 게시판 목록 조회        |
|  2 | GET /boards/1/posts?page=0&size=10&direction=DESC | 게시글 목록 첫 페이지 조회  |
|  3 | GET /boards/1/posts?page=1&size=10&direction=DESC | 게시글 목록 다음 페이지 조회 |
|  4 | GET /posts/6                                      | 게시글 상세 조회        |
|  5 | GET /posts/6/comments?page=0&size=10              | 댓글 목록 조회         |

## 5. 단계별 전체 결과

|       단계 | 총 요청 수 | 평균 응답 시간 | p95 |  최소 |   최대 |  오류율 |       처리량 |
|---------:|-------:|---------:|----:|----:|-----:|-----:|----------:|
|  5 users |    250 |      3ms | 6ms | 2ms | 16ms | 0.0% |   5.2/sec |
| 10 users |    500 |      2ms | 4ms | 1ms | 14ms | 0.0% |  9.24/sec |
| 20 users |   1000 |      1ms | 3ms | 1ms |  7ms | 0.0% | 17.51/sec |
| 30 users |   1500 |      1ms | 3ms | 1ms |  7ms | 0.0% | 25.82/sec |

## 6. API별 결과

### 6.1 5 users

| API                                | 평균 응답 시간 | p95 |  최소 |   최대 |  오류율 |      처리량 | 비고                    |
|------------------------------------|---------:|----:|----:|-----:|-----:|---------:|-----------------------|
| GET /boards                        |      3ms | 5ms | 2ms | 14ms | 0.0% | 1.04/sec | 게시판 목록 조회             |
| GET /boards/{boardId}/posts page 0 |      2ms | 4ms | 2ms |  5ms | 0.0% | 1.04/sec | 게시글 목록 첫 페이지 조회       |
| GET /boards/{boardId}/posts page 1 |      3ms | 5ms | 2ms |  5ms | 0.0% | 1.04/sec | 게시글 목록 다음 페이지 조회      |
| GET /posts/{postId}                |      5ms | 7ms | 3ms | 16ms | 0.0% | 1.04/sec | 다른 API보다 상대적으로 높게 측정됨 |
| GET /posts/{postId}/comments       |      3ms | 5ms | 2ms |  5ms | 0.0% | 1.04/sec | 댓글 목록 조회              |

### 6.2 10 users

| API                                | 평균 응답 시간 | p95 |  최소 |   최대 |  오류율 |      처리량 | 비고               |
|------------------------------------|---------:|----:|----:|-----:|-----:|---------:|------------------|
| GET /boards                        |      2ms | 4ms | 1ms | 14ms | 0.0% | 1.85/sec | 게시판 목록 조회        |
| GET /posts/{postId}                |      3ms | 4ms | 2ms |  9ms | 0.0% | 1.85/sec | 게시글 상세 조회        |
| GET /boards/{boardId}/posts page 1 |      2ms | 3ms | 1ms |  4ms | 0.0% | 1.85/sec | 게시글 목록 다음 페이지 조회 |
| GET /boards/{boardId}/posts page 0 |      1ms | 3ms | 1ms |  3ms | 0.0% | 1.85/sec | 게시글 목록 첫 페이지 조회  |
| GET /posts/{postId}/comments       |      2ms | 3ms | 1ms |  4ms | 0.0% | 1.85/sec | 댓글 목록 조회         |

### 6.3 20 users

| API                                | 평균 응답 시간 | p95 |  최소 |  최대 |  오류율 |      처리량 | 비고               |
|------------------------------------|---------:|----:|----:|----:|-----:|---------:|------------------|
| GET /boards                        |      1ms | 3ms | 1ms | 5ms | 0.0% | 3.50/sec | 게시판 목록 조회        |
| GET /posts/{postId}                |      2ms | 3ms | 2ms | 7ms | 0.0% | 3.50/sec | 게시글 상세 조회        |
| GET /boards/{boardId}/posts page 1 |      1ms | 2ms | 1ms | 3ms | 0.0% | 3.50/sec | 게시글 목록 다음 페이지 조회 |
| GET /boards/{boardId}/posts page 0 |      1ms | 2ms | 1ms | 2ms | 0.0% | 3.50/sec | 게시글 목록 첫 페이지 조회  |
| GET /posts/{postId}/comments       |      1ms | 2ms | 1ms | 4ms | 0.0% | 3.50/sec | 댓글 목록 조회         |

### 6.4 30 users

| API                                | 평균 응답 시간 | p95 |  최소 |  최대 |  오류율 |      처리량 | 비고               |
|------------------------------------|---------:|----:|----:|----:|-----:|---------:|------------------|
| GET /boards                        |      1ms | 3ms | 1ms | 5ms | 0.0% | 5.16/sec | 게시판 목록 조회        |
| GET /posts/{postId}                |      2ms | 3ms | 2ms | 4ms | 0.0% | 5.16/sec | 게시글 상세 조회        |
| GET /boards/{boardId}/posts page 1 |      1ms | 2ms | 1ms | 7ms | 0.0% | 5.16/sec | 게시글 목록 다음 페이지 조회 |
| GET /boards/{boardId}/posts page 0 |      1ms | 2ms | 1ms | 4ms | 0.0% | 5.16/sec | 게시글 목록 첫 페이지 조회  |
| GET /posts/{postId}/comments       |      1ms | 2ms | 1ms | 5ms | 0.0% | 5.16/sec | 댓글 목록 조회         |

## 7. 중단 기준 발생 여부

| 항목        | 기준                    | 발생 여부   | 비고                                                     |
|-----------|-----------------------|---------|--------------------------------------------------------|
| p95 응답 시간 | 3초 초과 지속              | 발생하지 않음 | 모든 단계에서 p95 6ms 이하                                     |
| 5xx 오류    | 0% 초과                 | 발생하지 않음 | 모든 단계 오류율 0.0%                                         |
| CPU       | 80% 이상 지속             | 발생하지 않음 | `process_cpu_usage` 기준 80% 이상 지속 없음                    |
| Memory    | available 급감          | 단정 불가   | JVM heap 사용량 증가는 관찰했으나 OS available memory는 별도 수집하지 않음 |
| Swap      | 사용량 급증                | 미확인     | node exporter 미구성으로 Swap 지표 수집 불가                      |
| HikariCP  | pending connection 발생 | 발생하지 않음 | `hikaricp_connections_pending` 0 유지                    |

## 8. 결과 해석

Scenario A Load Test 결과, 5 users부터 30 users까지 동시 사용자 수를 증가시켜도 모든 단계에서 오류율은 0.0%였다.

전체 p95 응답 시간은 5 users 6ms, 10 users 4ms, 20 users 3ms, 30 users 3ms로 측정되어 목표 기준인 p95 1초 이하를 모두 만족했다.

처리량은 5.2/sec에서 25.82/sec까지 증가했으며, JMeter 기준으로 응답 지연이나 5xx 오류는 관찰되지 않았다.

## 9. 결론

Scenario A - 비로그인 사용자의 게시판 탐색 흐름은 30 users 조건까지 안정적으로 동작했다.

이번 테스트 조건에서는 게시판 목록 조회, 게시글 목록 조회, 게시글 상세 조회, 댓글 목록 조회에서 응답 시간 목표를 만족했고, 오류도 발생하지 않았다.

