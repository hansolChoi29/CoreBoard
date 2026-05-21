# Bottleneck Analysis

## 1. 분석 목적

Scenario A에서 병목은 게시글 목록 조회 API에서 발생했다.

이번 분석의 목적은 게시글 목록 조회가 느려진 원인을 확인하는 것이다.

중점 확인 대상은 다음과 같다.

- Page 조회의 count query
- 최신순 정렬 비용
- post와 users JOIN 비용
- 인덱스 사용 여부
- DB Connection 부족 여부

---

## 2. 분석 대상 시나리오

| 항목        | 내용                                    |
|-----------|---------------------------------------|
| 시나리오명     | Scenario A - 비로그인 사용자의 게시판 탐색         |
| 분석 기준 테스트 | 약 142만 건 게시글 데이터 기준 5 users Load Test |
| 분석 일자     | 2026-05-19                            |
| 참고 문서     | Scenario A Load Test                  |

---

## 3. 병목 의심 구간 요약

| 구간            | 관찰된 현상                  | 판단    |
|---------------|-------------------------|-------|
| 게시글 목록 page 0 | 평균 14164ms, p95 18335ms | 병목 발생 |
| 게시글 목록 page 1 | 평균 15210ms, p95 20080ms | 병목 발생 |
| 게시판 목록 조회     | p95 14ms                | 정상    |
| 게시글 상세 조회     | p95 19ms                | 정상    |
| 댓글 목록 조회      | p95 46ms                | 정상    |

게시글 목록 조회에서만 응답 시간이 크게 증가했다.

따라서 병목 구간은 게시글 목록 조회 API로 좁혔다.

---

## 4. 응답 시간 관점 분석

| API                                               | 평균 응답 시간 |     p95 | 해석 |
|---------------------------------------------------|---------:|--------:|----|
| GET /boards                                       |     12ms |    14ms | 정상 |
| GET /boards/2/posts?page=0&size=10&direction=DESC |  14164ms | 18335ms | 병목 |
| GET /boards/2/posts?page=1&size=10&direction=DESC |  15210ms | 20080ms | 병목 |
| GET /posts/1640239                                |     11ms |    19ms | 정상 |
| GET /posts/1640239/comments?page=0&size=10        |     14ms |    46ms | 정상 |

---

## 5. 자원 사용률 관점 분석

| 항목            | 관찰 결과                   | 해석                   |
|---------------|-------------------------|----------------------|
| CPU           | 높은 사용률이 지속되지는 않음        | CPU 병목 가능성 낮음        |
| JVM Memory    | OOM 징후 없음               | JVM Memory 병목 가능성 낮음 |
| DB Connection | Pending Connection 0 유지 | DB 연결 부족 아님          |
| Swap          | 별도 확인하지 않음              | 판단 제외                |
| 기타            | 5xx 오류 없음               | 서버 장애보다 쿼리 비용 문제로 판단 |

서버 전체 자원 부족보다는 특정 조회 쿼리 문제가 더 유력하다.

---

## 6. 병목 후보

| 병목 후보            | 근거                                                | 우선순위 |
|------------------|---------------------------------------------------|------|
| JOIN 목록 조회       | 실제 Hibernate SQL에서 post와 users JOIN 쿼리가 약 12초 소요됨 | 높음   |
| 정렬 비용            | JOIN 쿼리에서 Using temporary, Using filesort 발생      | 높음   |
| Count Query      | Page 조회로 count query가 실행됨                         | 중    |
| N+1              | 반복 users 조회 없음                                    | 낮음   |
| DB Connection 부족 | Pending Connection 0 유지                           | 낮음   |
| JVM Memory / GC  | OOM 징후 없음                                         | 낮음   |
| 네트워크 / Nginx     | local 테스트 환경                                      | 낮음   |

---

## 7. 추가 확인 내용

### 7.1 초기 쿼리 확인

처음에는 게시글 목록 조회의 정렬 비용과 count query를 의심했다.

```sql
EXPLAIN
SELECT *
FROM post
WHERE board_id = 2
  AND status = 'PUBLISHED'
ORDER BY created_at DESC LIMIT 10
OFFSET 0;
```

```sql
EXPLAIN
SELECT COUNT(*)
FROM post
WHERE board_id = 2
  AND status = 'PUBLISHED';
```

초기 EXPLAIN 결과는 다음과 같았다.

| 대상          | 사용 인덱스                        | 예상 rows | Extra                         |
|-------------|-------------------------------|--------:|-------------------------------|
| page 0 조회   | `FK2t7katxxymxif93a9osshl0ns` |  702250 | `Using where; Using filesort` |
| page 1 조회   | `FK2t7katxxymxif93a9osshl0ns` |  702250 | `Using where; Using filesort` |
| count query | `FK2t7katxxymxif93a9osshl0ns` |  702250 | `Using where`                 |

초기 상태에서는 `board_id` 단일 인덱스만 사용했다.

게시글 목록 조회에서는 `Using filesort`가 발생했다.

7.2 복합 인덱스 추가 실험

정렬 비용을 줄이기 위해 복합 인덱스를 추가했다.

```sql
CREATE INDEX idx_post_board_status_created
    ON post (board_id, status, created_at DESC);
ANALYZE
TABLE post;
```

복합 인덱스 추가 후 단순 post 목록 조회 결과는 개선되었다.
| 항목 | 개선 전 | 개선 후 |
|---|---|---|
| key | `FK2t7katxxymxif93a9osshl0ns` | `idx_post_board_status_created` |
| Extra | `Using where; Using filesort` | `Using index condition` |

단순 post 목록 조회에서는 `Using filesort`가 사라졌다.

쿼리 실행 시간도 확인했다.

| 쿼리            |   측정 시간 |
|---------------|--------:|
| 단순 post 목록 조회 | 약 260ms |
| count query   | 약 610ms |

하지만 API 응답 시간은 여전히 약 12초였다.

| API                                                 |   응답 시간 |
|-----------------------------------------------------|--------:|
| `GET /boards/2/posts?page=0&size=10&direction=DESC` | 약 12.5초 |
| `GET /boards/2/posts?page=1&size=10&direction=DESC` | 약 12.3초 |

따라서 단순 post 조회와 count query만으로는 API 지연을 설명할 수 없었다.

### 7.3 애플리케이션 SQL 로그 확인

SQL 로그 확인 결과, API 1회 호출에서 핵심 쿼리는 2개였다.

- 게시글 목록 조회 쿼리
- count query

반복적인 users 조회는 없었다.

따라서 N+1은 원인이 아니었다.

실제 Hibernate 목록 조회 쿼리는 다음과 같았다.

```sql
SELECT p.id,
       p.board_id,
       p.content,
       p.content_format,
       p.created_at,
       p.status,
       p.title,
       p.updated_at,
       p.user_id,
       u.user_id,
       u.email,
       u.nickname,
       u.password,
       u.phone_number,
       u.role,
       u.username,
       p.view_count
FROM post p
         JOIN users u
              ON u.user_id = p.user_id
WHERE p.board_id = 2
  AND p.status = 'PUBLISHED'
ORDER BY p.created_at DESC LIMIT 10;
```

이 JOIN 목록 조회 쿼리는 약 12548ms가 소요되었다.

| 쿼리                      |     측정 시간 |
|-------------------------|----------:|
| 실제 Hibernate JOIN 목록 조회 | 약 12548ms |

EXPLAIN 결과는 다음과 같았다.

| 대상    | key                             |   rows | Extra                                                    |
|-------|---------------------------------|-------:|----------------------------------------------------------|
| post  | `idx_post_board_status_created` | 702250 | `Using index condition; Using temporary; Using filesort` |
| users | `NULL`                          |      1 | `Using where; Using join buffer (hash join)`             |

결론적으로, 실제 병목은 count query가 아니라 `post JOIN users` 목록 조회 쿼리였다.

7.4 Projection 쿼리 실험

조회 컬럼 수가 문제인지 확인하기 위해 필요한 컬럼만 조회했다.

```sql
SELECT p.id,
       p.title,
       p.created_at,
       p.view_count,
       u.nickname
FROM post p
         JOIN users u
              ON u.user_id = p.user_id
WHERE p.board_id = 2
  AND p.status = 'PUBLISHED'
ORDER BY p.created_at DESC LIMIT 10;
```

Projection 쿼리도 약 10458ms가 소요되었다.

| 쿼리                 |     측정 시간 |
|--------------------|----------:|
| Projection JOIN 조회 | 약 10458ms |

컬럼 수를 줄여도 큰 개선은 없었다.

따라서 핵심 문제는 조회 컬럼 수가 아니라, JOIN 상태에서 최신순 정렬이 수행되는 구조다.

### 7.5 Grafana 지표 확인

Grafana 지표에서는 서버 전체 자원 부족 징후가 보이지 않았다.

| 항목                    | 결과           |
|-----------------------|--------------|
| CPU                   | 높은 사용률 지속 없음 |
| JVM Memory            | OOM 징후 없음    |
| DB Connection Pending | 0 유지         |
| 5xx 오류                | 없음           |

DB 연결 부족이 아니라 쿼리 실행 구조 문제로 판단한다.

---

## 8. 개선 방향

| 개선 대상         | 개선 방향                                   | 기대 효과                          | 우선순위 |
|---------------|-----------------------------------------|--------------------------------|------|
| JOIN 목록 조회    | 게시글 목록 조회에서 `join fetch p.user` 제거      | `post JOIN users` 상태의 정렬 비용 제거 | 높음   |
| 작성자명 조회       | post 목록 조회 후 `userId`로 `nickname` 별도 조회 | N+1 없이 writerName 구성           | 높음   |
| Count Query   | JOIN 제거 후에도 지연이 남으면 재확인                 | Page count 비용 검증               | 중    |
| Scenario B 검색 | Scenario A 개선 후 재분석                     | 일반 목록 병목과 검색 병목 분리             | 중    |

---

## 9. 적용 결과

게시글 목록 조회에서 post JOIN users를 제거했다.

기존에는 PostRepository의 목록 조회에서 join fetch p.user를 사용했다.
개선 후에는 post 목록을 먼저 조회하고, 조회된 post의 userId로 nickname을 별도 조회하도록 변경했다.

개선 후 curl로 API 응답 시간을 먼저 확인했다.

| API                                               | 개선 전    | 개선 후     |
|---------------------------------------------------|---------|----------|
| GET /boards/2/posts?page=0&size=10&direction=DESC | 약 12.5초 | 약 0.682초 |
| GET /boards/2/posts?page=1&size=10&direction=DESC | 약 12.3초 | 약 0.399초 |

이후 Jmeter로 5uesrs 조건을 다시 측정했다.

| 항목          | 개선 전    | 개선 후  |
|-------------|---------|-------|
| 전체 평균 응답 시간 | 5882ms  | 142ms |
| 전체 p95      | 18322ms | 354ms |
| 전체 최대 응답 시간 | 22044ms | 610ms |
| 오류율         | 0.0%    | 0.0%  |

병목이 발생했던 게시글 목록 조회도 개선되었다

| API           | 개선 전 p95 | 개선 후 p95 |
|---------------|----------|----------|
| 게시글 목록 page 0 | 18335ms  | 384ms    |
| 게시글 목록 page 1 | 20080ms  | 363ms    |

---

## 10. 결론

Scenario A의 병목은 게시글 목록 조회 API에서 발생했다.

초기에는 Page 기반 count query와 최신순 정렬 비용을 의심했다. 복합 인덱스를 추가한 뒤 단독 post 목록 조회와 count query는 1초 이내로 확인되었다.

하지만 API 응답 시간은 여전히 약 12초였다. 

SQL 로그와 EXPLAIN 확인 결과, 실제 병목은 post JOIN users 목록 조회 쿼리였다.
해당 쿼리에서 Using temporary, Using filesort, Using join buffer가 발생했다.

개선 후에는 게시글 목록 조회에서 join fetch p.user를 제거했다. post 목록을 먼저 조회하고, 필요한 user nickname만 별도로 조회하도록 변경했다.

그 결과 5 users 기준 전체 p95는 18322ms에서 354ms로 감소했다. 게시글 목록 page 0 p95는 18335ms에서 384ms로, page 1 p95는 20080ms에서 363ms로 감소했다.

따라서 이번 병목의 핵심 원인은 post JOIN users 상태에서 최신순 정렬을 수행하던 구조로 판단한다.

다음 단계에서는 개선된 상태로 10 users, 20 users, 30 users를 순차적으로 측정한다.