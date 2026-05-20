# Load Test - Scenario A 개선 후 재측정

## 1. 테스트 목적

Scenario A - 비로그인 사용자의 게시판 탐색 흐름에서 게시글 목록 조회 개선 후 API 응답 시간이 실제로 개선되었는지 확인한다.

개선 전 테스트에서는 약 142만 건의 게시글 데이터 조건에서 5 users 단계부터 게시글 목록 조회 API의 p95가 18초 이상으로 증가했다.

병목 분석 결과, 주요 원인은 게시글 목록 조회에서 `post JOIN users` 상태로 최신순 정렬이 수행되는 구조였다.

이번 테스트는 목록 조회에서 `join fetch p.user`를 제거하고, post 목록을 먼저 조회한 뒤 필요한 user 정보를 별도로 조회하도록 개선한 뒤 수행한다.

---

## 2. 테스트 대상 시나리오

| 항목      | 내용                               |
|---------|----------------------------------|
| 시나리오명   | Scenario A - 비로그인 사용자의 게시판 탐색    |
| 테스트 환경  | local                            |
| 실행 일자   | 2026-05-19                       |
| 실행 도구   | JMeter                           |
| 모니터링 도구 | Prometheus / Grafana             |
| 데이터 조건  | 게시글 약 142만 건                     |
| 개선 내용   | 게시글 목록 조회에서 `post JOIN users` 제거 |

---

## 3. 부하 조건

| 단계 | Number of Threads | Ramp-up Period | Duration / Loop Count | 예상 요청 수 |
|---:|------------------:|---------------:|----------------------:|--------:|
|  1 |                 5 |            60초 |         Loop Count 10 |     250 |
|  2 |                10 |            60초 |         Loop Count 10 |     500 |
|  3 |                20 |            60초 |         Loop Count 10 |    1000 |
|  4 |                30 |            60초 |         Loop Count 10 |    1500 |

---

## 4. 실행 API

| 순서 | API                                               | 설명               |
|---:|---------------------------------------------------|------------------|
|  1 | GET /boards                                       | 게시판 목록 조회        |
|  2 | GET /boards/2/posts?page=0&size=10&direction=DESC | 게시글 목록 첫 페이지 조회  |
|  3 | GET /boards/2/posts?page=1&size=10&direction=DESC | 게시글 목록 다음 페이지 조회 |
|  4 | GET /posts/1640239                                | 게시글 상세 조회        |
|  5 | GET /posts/1640239/comments?page=0&size=10        | 댓글 목록 조회         |

---

## 5. 단계별 전체 결과

|       단계 | 총 요청 수 | 평균 응답 시간 |   p95 |  최소 |    최대 |  오류율 |      처리량 |
|---------:|-------:|---------:|------:|----:|------:|-----:|---------:|
|  5 users |    250 |    142ms | 354ms | 2ms | 610ms | 0.0% |  4.5/sec |
| 10 users |    500 |    145ms | 379ms | 1ms | 568ms | 0.0% |  8.2/sec |
| 20 users |   1000 |    151ms | 416ms | 1ms | 765ms | 0.0% | 15.4/sec |
| 30 users |   1500 |    164ms | 479ms | 1ms | 655ms | 0.0% | 22.9/sec |

---

## 6. API별 결과

### 6.1 5 users

| API                                | 평균 응답 시간 |   p95 |    최소 |    최대 |  오류율 |      처리량 | 비고    |
|------------------------------------|---------:|------:|------:|------:|-----:|---------:|-------|
| GET /boards                        |      4ms |   6ms |   2ms |  28ms | 0.0% | 55.2/min | 정상    |
| GET /posts/{postId}                |      6ms |   9ms |   3ms |  51ms | 0.0% | 55.3/min | 정상    |
| GET /boards/{boardId}/posts page 1 |    345ms | 363ms | 332ms | 423ms | 0.0% | 55.0/min | 개선 확인 |
| GET /boards/{boardId}/posts page 0 |    350ms | 384ms | 334ms | 610ms | 0.0% | 55.0/min | 개선 확인 |
| GET /posts/{postId}/comments       |      4ms |   6ms |   3ms |  10ms | 0.0% | 55.6/min | 정상    |

### 6.2 10 users

| API                                | 평균 응답 시간 |   p95 |    최소 |    최대 |  오류율 |     처리량 | 비고 |
|------------------------------------|---------:|------:|------:|------:|-----:|--------:|----|
| GET /boards                        |      2ms |   5ms |   1ms |  11ms | 0.0% | 1.6/sec | 정상 |
| GET /posts/{postId}                |      3ms |   5ms |   3ms |   8ms | 0.0% | 1.6/sec | 정상 |
| GET /boards/{boardId}/posts page 1 |    355ms | 415ms | 332ms | 521ms | 0.0% | 1.6/sec | 안정 |
| GET /boards/{boardId}/posts page 0 |    361ms | 460ms | 330ms | 568ms | 0.0% | 1.6/sec | 안정 |
| GET /posts/{postId}/comments       |      2ms |   4ms |   2ms |   5ms | 0.0% | 1.6/sec | 정상 |

### 6.3 20 users

| API                                | 평균 응답 시간 |   p95 |    최소 |    최대 |  오류율 |     처리량 | 비고 |
|------------------------------------|---------:|------:|------:|------:|-----:|--------:|----|
| GET /boards                        |      2ms |   5ms |   1ms |  33ms | 0.0% | 3.1/sec | 정상 |
| GET /posts/{postId}                |      3ms |   4ms |   2ms |   7ms | 0.0% | 3.1/sec | 정상 |
| GET /boards/{boardId}/posts page 1 |    375ms | 496ms | 331ms | 660ms | 0.0% | 3.1/sec | 안정 |
| GET /boards/{boardId}/posts page 0 |    374ms | 457ms | 332ms | 765ms | 0.0% | 3.1/sec | 안정 |
| GET /posts/{postId}/comments       |      2ms |   3ms |   2ms |   5ms | 0.0% | 3.1/sec | 정상 |

### 6.4 30 users

| API                                | 평균 응답 시간 |   p95 |    최소 |    최대 |  오류율 |     처리량 | 비고 |
|------------------------------------|---------:|------:|------:|------:|-----:|--------:|----|
| GET /boards                        |      2ms |   4ms |   1ms |   8ms | 0.0% | 4.6/sec | 정상 |
| GET /posts/{postId}                |      3ms |   4ms |   2ms |  10ms | 0.0% | 4.6/sec | 정상 |
| GET /boards/{boardId}/posts page 1 |    407ms | 519ms | 333ms | 656ms | 0.0% | 4.6/sec | 안정 |
| GET /boards/{boardId}/posts page 0 |    408ms | 545ms | 331ms | 665ms | 0.0% | 4.6/sec | 안정 |
| GET /posts/{postId}/comments       |      2ms |   3ms |   2ms |   4ms | 0.0% | 4.6/sec | 정상 |

---

## 7. 모니터링 결과

### 7.1 CPU

| 단계       | 관찰 결과            | 특이사항         |
|----------|------------------|--------------|
| 5 users  | 높은 사용률이 지속되지는 않음 | CPU 병목 징후 없음 |
| 10 users | 높은 사용률이 지속되지는 않음 | CPU 병목 징후 없음 |
| 20 users | 높은 사용률이 지속되지는 않음 | CPU 병목 징후 없음 |
| 30 users | 높은 사용률이 지속되지는 않음 | CPU 병목 징후 없음 |

### 7.2 JVM Memory

| 단계       | 관찰 결과                     | 특이사항      |
|----------|---------------------------|-----------|
| 5 users  | Heap Memory 증가 후 감소 패턴 관찰 | OOM 징후 없음 |
| 10 users | Heap Memory 증가 후 감소 패턴 관찰 | OOM 징후 없음 |
| 20 users | Heap Memory 증가 후 감소 패턴 관찰 | OOM 징후 없음 |
| 30 users | Heap Memory 증가와 감소 패턴 관찰  | OOM 징후 없음 |

### 7.3 DB Connection

| 단계       | 관찰 결과                          | 특이사항                    |
|----------|--------------------------------|-------------------------|
| 5 users  | Active Connection은 낮은 수준으로 유지됨 | Pending Connection 0 유지 |
| 10 users | Active Connection 최대 2개 수준     | Pending Connection 0 유지 |
| 20 users | Active Connection 최대 3개 수준     | Pending Connection 0 유지 |
| 30 users | Active Connection 최대 5개 수준     | Pending Connection 0 유지 |

### 7.4 Swap

| 단계       | 관찰 결과      | 특이사항  |
|----------|------------|-------|
| 5 users  | 별도 확인하지 않음 | 판단 제외 |
| 10 users | 별도 확인하지 않음 | 판단 제외 |
| 20 users | 별도 확인하지 않음 | 판단 제외 |
| 30 users | 별도 확인하지 않음 | 판단 제외 |

---

## 8. 중단 기준 발생 여부

| 항목        | 기준                    | 발생 여부   | 비고                      |
|-----------|-----------------------|---------|-------------------------|
| p95 응답 시간 | 3초 초과 지속              | 발생하지 않음 | 5 users 전체 p95 354ms    |
| 5xx 오류    | 0% 초과                 | 발생하지 않음 | 오류율 0.0%                |
| CPU       | 80% 이상 지속             | 발생하지 않음 | 높은 사용률 지속 없음            |
| Memory    | available 급감          | 단정 불가   | JVM Heap 기준 OOM 징후 없음   |
| Swap      | 사용량 급증                | 미확인     | 이번 테스트에서 별도 확인하지 않음     |
| HikariCP  | pending connection 발생 | 발생하지 않음 | Pending Connection 0 유지 |

---

## 9. 결과 해석

개선 후 모든 단계에서 오류율은 0.0%였다.

전체 p95는 다음과 같다.

|       단계 | 전체 p95 |
|---------:|-------:|
|  5 users |  354ms |
| 10 users |  379ms |
| 20 users |  416ms |
| 30 users |  479ms |

모든 단계에서 p95는 중단 기준인 3초 이하였다.

병목이었던 게시글 목록 조회도 안정적으로 개선되었다.

|       단계 | page 0 p95 | page 1 p95 |
|---------:|-----------:|-----------:|
|  5 users |      384ms |      363ms |
| 10 users |      460ms |      415ms |
| 20 users |      457ms |      496ms |
| 30 users |      545ms |      519ms |

개선 전에는 5 users 단계에서 게시글 목록 조회 p95가 18초 이상이었다.

개선 후에는 30 users 조건에서도 게시글 목록 조회 p95가 0.6초 이하로 유지되었다.

CPU, JVM Memory, DB Connection에서도 병목 징후는 확인되지 않았다.

DB Connection Pending은 0으로 유지되었다.

---

## 10. 결론

Scenario A는 개선 후 30 users까지 안정적으로 통과했다.

개선 전 병목은 게시글 목록 조회 API였다.

주요 원인은 `post JOIN users` 상태에서 최신순 정렬이 수행되는 구조였다.

개선 후에는 `join fetch p.user`를 제거했다.

post 목록을 먼저 조회하고, user nickname은 별도로 조회하도록 변경했다.

그 결과 전체 p95는 5 users 354ms에서 30 users 479ms 범위로 유지되었다.

게시글 목록 조회 p95도 30 users 기준 page 0은 545ms, page 1은 519ms로 측정되었다.

따라서 이번 개선은 유효하다고 판단한다.