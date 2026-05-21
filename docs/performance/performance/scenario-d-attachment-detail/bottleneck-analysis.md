# Bottleneck Analysis

## 1. 분석 목적

Scenario D Smoke Test에서 게시글 목록 조회 API가 p95 3초 기준을 초과한 원인을 분석한다.

초기에는 첨부파일이 있는 게시글 상세 조회가 병목일 것으로 예상했으나, 실제 측정 결과 게시글 목록 조회 API에서 지연이 발생했다.  
따라서 게시글 목록 조회 API의 SQL 실행 계획, 인덱스 사용 여부, count query 비용, 서버 자원 사용률을 확인하여 병목 원인을 좁힌다.

## 2. 분석 대상 시나리오

| 항목        | 내용                           |
|-----------|------------------------------|
| 시나리오명     | Scenario D - 첨부파일이 있는 게시글 조회 |
| 분석 기준 테스트 | Smoke Test                   |
| 분석 일자     | 2026-05-20                   |
| 참고 문서     | `smoke-test.md`              |

## 3. 병목 의심 구간 요약

| 구간          | 관찰된 현상                                                           | 병목 의심 이유                                                  |
|-------------|------------------------------------------------------------------|-----------------------------------------------------------|
| 게시글 목록 조회   | `GET /boards/2/posts?page=0&size=10` 요청이 3,075ms로 측정됨            | Smoke Test 전체 p95가 3,075ms로 상승한 직접 원인                     |
| Count Query | `board_id = 2`, `status = PUBLISHED` 조건의 게시글 수가 1,420,116건으로 확인됨 | `Page` 응답 생성을 위해 전체 개수를 계산하면서 대량 인덱스 범위를 확인할 가능성이 있음      |
| 정렬 처리       | 초기 EXPLAIN에서 `Using filesort` 발생                                 | `created_at DESC` 정렬을 인덱스로 처리하지 못해 대량 정렬 비용이 발생했을 가능성이 있음 |

## 4. 응답 시간 관점 분석

| API               | 평균 응답 시간 |     p95 | 해석                                              |
|-------------------|---------:|--------:|-------------------------------------------------|
| 게시글 목록 조회         |  3,075ms | 3,075ms | Smoke Test 기준 지연 발생 지점. 상세 조회보다 목록 조회가 병목으로 확인됨 |
| 첨부파일 포함 게시글 상세 조회 |     55ms |    55ms | 첨부파일 상세 조회는 예상과 달리 병목이 아니었음                     |
| 게시글 댓글 목록 조회      |      9ms |     9ms | 댓글 조회도 병목으로 보기 어려움                              |

## 5. 자원 사용률 관점 분석

| 항목            | 관찰 결과                                             | 해석                                  |
|---------------|---------------------------------------------------|-------------------------------------|
| CPU           | 큰 상승 없음                                           | 서버 전체 연산량이 포화된 상황은 아님               |
| JVM Memory    | Heap 사용량은 변동이 있었지만 급격한 고갈은 보이지 않음                 | 메모리 부족 또는 GC 병목 가능성은 낮음             |
| DB Connection | Active Connection이 낮고 Pending Connection은 확인되지 않음 | 커넥션 부족으로 요청이 대기한 상황은 아님             |
| Swap          | Smoke Test 수준에서는 Swap 병목으로 볼 만한 지표 없음             | 메모리 부족으로 인한 디스크 스왑 지연 가능성은 낮음       |
| 기타            | 요청 수가 매우 적음                                       | 부하량 자체보다 특정 SQL 실행 비용 문제로 보는 것이 타당함 |

## 6. 병목 후보

| 병목 후보            | 근거                                                                                            | 추가 확인 방법                                          | 우선순위 |
|------------------|-----------------------------------------------------------------------------------------------|---------------------------------------------------|------|
| Count Query      | `Page` 응답을 사용하고 있으며, 조건에 맞는 게시글 수가 1,420,116건임. `EXPLAIN ANALYZE` 결과 count query가 약 479ms 소요됨 | `Page`를 `Slice`로 변경 후 count query 제거 여부와 응답 시간 비교 | 높음   |
| 정렬 비용            | 초기 EXPLAIN에서 `Using filesort` 발생. `board_id` FK 인덱스만 사용하고 정렬 인덱스가 없었음                         | 복합 인덱스 적용 전후 EXPLAIN 비교                           | 중간   |
| N+1 또는 연관 조회     | 게시글 목록 조회에서 사용자 정보가 함께 필요할 수 있음                                                               | Hibernate SQL 로그에서 추가 select 발생 여부 확인             | 중간   |
| DB Connection 부족 | Grafana상 Active/Pending Connection이 낮음                                                        | 부하 증가 시 커넥션 풀 지표 재확인                              | 낮음   |
| JVM Memory / GC  | Heap 급증이나 긴 GC 정황이 뚜렷하지 않음                                                                    | GC 로그 또는 JVM 메모리 패널 확인                            | 낮음   |
| 네트워크 / Nginx     | local 환경 Smoke Test이며 요청 수가 적음                                                                | 동일 API를 DB/애플리케이션/프록시 경로별로 분리 측정                  | 낮음   |

## 7. 추가 확인 내용

### 7.1 쿼리 확인

게시글 목록 조회 API는 다음 조건으로 게시글 목록을 조회한다.

```sql
SELECT p.id,
       p.title,
       p.content,
       p.status,
       p.created_at,
       p.updated_at,
       p.board_id,
       p.user_id
FROM post p
WHERE p.board_id = 2
  AND p.status = 'PUBLISHED'
ORDER BY p.created_at DESC LIMIT 10
OFFSET 0;
```

또한 `Page` 응답 생성을 위해 전체 개수를 계산하는 count query가 함께 실행될 수 있다.

```sql
SELECT COUNT(p.id)
FROM post p
WHERE p.board_id = 2
  AND p.status = 'PUBLISHED';
```

현재 count query 결과는 다음과 같다.

```text
1,420,116건
```

### 7.2 인덱스 확인

초기 EXPLAIN 결과는 다음과 같았다.

```text
key = FK2t7katxxymxif93a9osshl0ns
rows = 702517
Extra = Using where; Using filesort
```

기존에는 `board_id` FK 인덱스만 사용되었고, `status` 조건과 `created_at DESC` 정렬을 함께 처리하지 못했다.  
그 결과 대량 후보군을 대상으로 필터링과 정렬이 발생했다.

이를 개선하기 위해 다음 복합 인덱스를 추가했다.

```sql
CREATE INDEX idx_post_board_status_created_at
    ON post (board_id, status, created_at DESC);
```

복합 인덱스 적용 후 EXPLAIN 결과는 다음과 같이 변경되었다.

```text
key = idx_post_board_status_created_at
Extra = Using index condition
```

정렬 과정에서 발생하던 `Using filesort`는 제거되었다.

### 7.3 EXPLAIN ANALYZE 확인

목록 조회 쿼리와 count query를 분리해서 실제 실행 시간을 확인했다.

#### 목록 조회 쿼리

```text
-> Limit: 10 row(s)  (cost=111488 rows=10) (actual time=0.0405..0.0715 rows=10 loops=1)
    -> Index lookup on p using idx_post_board_status_created_at (board_id=2, status='PUBLISHED'), with index condition: (p.`status` = 'PUBLISHED')  (cost=111488 rows=702517) (actual time=0.0395..0.07 rows=10 loops=1)
```

목록 조회 쿼리는 복합 인덱스를 사용했고, `LIMIT 10` 기준 실제 실행 시간은 약 0.07ms로 측정되었다.  
따라서 게시글 10개를 가져오는 조회 자체는 병목이 아니었다.

#### Count Query

```text
-> Aggregate: count(p.id)  (cost=142646 rows=1) (actual time=479..479 rows=1 loops=1)
    -> Filter: (p.`status` = 'PUBLISHED')  (cost=72394 rows=702517) (actual time=0.0485..440 rows=1.42e+6 loops=1)
        -> Covering index lookup on p using idx_post_board_status_created_at (board_id=2, status='PUBLISHED')  (cost=72394 rows=702517) (actual time=0.0471..279 rows=1.42e+6 loops=1)
```

count query 역시 복합 인덱스를 사용했다.  
하지만 조건에 해당하는 게시글 수가 1,420,116건이었기 때문에, 인덱스를 사용하더라도 대량의 인덱스 범위를 실제로 확인해야 했다.

그 결과 count query의 실제 실행 시간은 약 479ms로 측정되었다.

### 7.4 애플리케이션 로그 확인

현재 단계에서는 애플리케이션 예외나 5xx 오류는 확인되지 않았다.

Smoke Test 결과 오류율은 0.00%였으므로 기능 실패가 아니라 응답 지연 문제로 판단한다.

추가로 확인할 내용은 다음과 같다.

```text
- Hibernate SQL 로그에서 count query 발생 여부
- 게시글 목록 조회 시 추가 select 발생 여부
- fetch join 또는 DTO projection 적용 여부
```

### 7.5 Grafana 지표 확인

Grafana 지표상 CPU, JVM Memory, DB Connection에서 명확한 포화 상태는 확인되지 않았다.

특히 DB Connection Pending이 발생하지 않았고, 요청량도 낮았다.  
따라서 이번 지연은 서버 전체 자원 부족보다 특정 SQL 실행 비용 문제로 판단한다.

## 8. 개선 방향

| 개선 대상        | 개선 방향                                             | 기대 효과                                        | 우선순위 |
|--------------|---------------------------------------------------|----------------------------------------------|------|
| 게시글 목록 응답 모델 | `Page`에서 `Slice`로 변경                              | count query 제거. 전체 개수 계산 없이 다음 페이지 존재 여부만 판단 | 높음   |
| 게시글 목록 인덱스   | `board_id`, `status`, `created_at DESC` 복합 인덱스 유지 | 필터링과 정렬을 인덱스로 처리하여 filesort 제거               | 높음   |
| 연관 조회        | 사용자 정보 조회 방식 확인                                   | N+1 발생 시 추가 select 제거                        | 중간   |
| 게시글 수 집계     | 전체 페이지 수가 반드시 필요할 경우 별도 집계/캐시 검토                  | 매 요청마다 count query를 실행하지 않도록 개선              | 중간   |

## 9. 적용 결과

| 개선 항목 | 개선 전 | 개선 후 | 결과 |
|---|---|---|---|
| 응답 모델 | Page | Slice | count query 제거 |
| 목록 조회 인덱스 | board_id FK 인덱스 중심 | `(board_id, status, created_at DESC)` 복합 인덱스 | filesort 제거 |
| 목록 조회 SQL | 대량 후보군 확인 가능 | 인덱스 순서대로 11건 조회 | 조회 비용 감소 |
| 게시글 목록 조회 응답 시간 | 3,075ms | 55ms | 약 98.2% 감소 |
| Smoke Test p95 | 3,075ms | 55ms | 중단 기준 초과 → 통과 |

## 10. 결론

Scenario D Smoke Test에서 최초 병목은 첨부파일 상세 조회가 아니라 게시글 목록 조회 API였다.

초기 실행에서는 게시글 목록 조회가 3,075ms로 측정되어 p95 3초 기준을 초과했다. 원인 분석 결과, `Page` 응답 생성을 위한 count query와 정렬 비용이 주요 병목 후보로 확인되었다.

복합 인덱스 `(board_id, status, created_at DESC)`를 적용하여 정렬 비용을 줄였고, 게시글 목록 응답을 `Page`에서 `Slice`로 변경하여 전체 개수 계산을 제거했다.

개선 후 `EXPLAIN ANALYZE` 결과 게시글 목록 조회는 인덱스를 사용해 11건만 읽고 종료했다.

```text
-> Limit: 11 row(s)  (actual time=0.0342..0.0595 rows=11 loops=1)
    -> Index lookup on p using idx_post_board_status_created_at
       (board_id=2, status='PUBLISHED')
       (actual time=0.0333..0.0579 rows=11 loops=1)
```

Smoke Test 재실행 결과 게시글 목록 조회는 55ms, 전체 p95는 55ms로 개선되었다.

따라서 Scenario D의 병목은 Page 기반 전체 개수 계산과 정렬 비용에서 발생한 것으로 판단하며, 최신순 탐색 중심의 앨범형 게시판에는 Slice 기반 응답이 더 적합하다고 결론 내렸다.



## 10. 결론

Scenario D Smoke Test에서 p95 3초 초과가 발생했으며, 실제 지연 구간은 첨부파일 상세 조회가 아니라 게시글 목록 조회 API였다.

초기 EXPLAIN 결과 게시글 목록 조회는 `board_id` FK 인덱스만 사용하고 있었고, `created_at DESC` 정렬에서 `Using filesort`가 발생했다.  
복합 인덱스를 추가한 뒤 `Using filesort`는 제거되었고, 목록 조회 쿼리 자체는 `EXPLAIN ANALYZE` 기준 약 0.07ms로 측정되었다.

하지만 `Page` 응답 생성을 위한 count query는 조건에 맞는 1,420,116건을 확인해야 했고, `EXPLAIN ANALYZE` 기준 약 479ms가 소요되었다.

따라서 현재 남은 병목은 게시글 10개를 조회하는 쿼리가 아니라, 전체 개수를 계산하는 count query 비용으로 판단한다.

다음 개선 단계에서는 게시글 목록 응답을 `Page`에서 `Slice`로 변경하여 count query를 제거하고, JMeter로 Smoke Test를 재실행해 응답 시간 개선 여부를 확인한다.