# Smoke Test

## 1. 테스트 목적

본격적인 부하 테스트를 수행하기 전에, JMeter 시나리오가 Redis 조회수 기록 및 인기글 조회 API를 올바른 경로와 파라미터로 호출하는지 확인한다.

이 테스트는 성능을 판단하기 위한 테스트가 아니라, 시나리오 E의 API 호출 흐름이 정상적으로 동작하는지 검증하기 위한 사전 점검이다.

Scenario E는 게시글 상세 조회 시 Redis 기반 조회수 기록이 함께 수행되고, Redis ZSET 기반 인기글 목록을 조회하는 흐름이다.

게시글 상세 조회는 기존 DB 조회 흐름에 Redis 중복 조회 방지, 조회수 delta 증가, 인기글 점수 증가 로직이 추가되었으므로, 요청이 정상적으로 처리되는지 먼저 확인한다.

인기글 조회는 Redis ZSET에서 게시글 ID를 가져온 뒤 DB에서 게시글 정보를 조회하는 구조이므로, API 호출 자체가 정상 동작하는지 확인한다.

## 2. 테스트 대상 시나리오

| 항목 | 내용 |
|---|---|
| 시나리오명 | Scenario E - Redis 조회수 및 인기글 흐름 |
| 테스트 환경 | local |
| 실행 URL | http://localhost:8080 |
| 실행 도구 | JMeter |
| 인증 여부 | 비로그인 |

## 3. 실행 조건

| 항목 | 값 |
|---|---:|
| Number of Threads | 1 |
| Ramp-up Period | 1초 |
| Loop Count | 1 |
| 총 요청 수 | 2 |

## 4. 실행 API

| 순서 | API | 설명 |
|---:|---|---|
| 1 | GET /posts/1 | 게시글 상세 조회 및 Redis 조회수 기록 |
| 2 | GET /posts/popular?size=10 | Redis ZSET 기반 인기글 목록 조회 |

## 5. 실행 결과

| API | 표본 수 | 평균 응답 시간 | 최소 | 최대 | 오류율 |
|---|---:|---:|---:|---:|---:|
| GET /posts/{postId} | 1 | 223ms | 223ms | 223ms | 0.0% |
| GET /posts/popular?size=10 | 1 | 17ms | 17ms | 17ms | 0.0% |
| 총계 | 2 | 120ms | 17ms | 223ms | 0.0% |

## 6. 결과 해석

시나리오 E의 Smoke Test 결과, 총 2개의 API 요청이 모두 정상 응답했다.

고정된 `postId=1`을 사용하여 게시글 상세 조회 API를 호출했고, 이후 `GET /posts/popular?size=10` API를 호출하여 인기글 목록 조회 흐름을 확인했다.

게시글 상세 조회는 Redis 조회수 기록 로직이 포함된 상태에서도 223ms로 응답했다.

인기글 조회는 Redis ZSET 기반 조회 흐름에서 17ms로 응답했다.

전체 오류율은 0.0%였다.

Grafana 기준으로 CPU 사용률은 낮은 수준을 유지했고, JVM Heap Memory는 약 76MB에서 99MB 수준으로 증가했으나 급격한 메모리 증가로 보기는 어렵다.

DB Connection Active는 거의 0 수준이었고, DB Connection Pending은 0으로 유지되었다.

다만 이 테스트는 사용자 1명, Loop 1회 조건에서 수행한 사전 검증이므로 성능 판단 근거로 사용하지 않는다.

이번 결과는 JMeter 요청 경로, HTTP Method, Query Parameter, 고정 테스트 데이터가 올바르게 설정되었음을 확인한 결과다.

## 7. 확인된 문제

| 문제 | 원인 | 해결 |
|---|---|---|
| 확인된 문제 없음 | Smoke Test 기준 모든 요청이 정상 응답함 | 다음 단계에서 동시 사용자 수를 증가시키며 Load Test 수행 |

## 8. 결론

Scenario E - Redis 조회수 및 인기글 흐름 Smoke Test를 통과했다.

이번 테스트를 통해 JMeter가 Redis 조회수 기록이 포함된 게시글 상세 조회 API와 Redis ZSET 기반 인기글 조회 API를 올바른 순서로 호출할 수 있음을 확인했다.

따라서 다음 단계에서는 동일한 시나리오를 기준으로 동시 사용자 수를 5명, 10명, 20명, 30명으로 증가시키며 Load Test를 수행한다.