# CoreBoard Performance Test

CoreBoard의 주요 사용자 흐름을 기준으로 JMeter 부하 테스트를 수행한다.

Prometheus/Grafana를 통해 API 응답 시간, CPU, JVM Memory, DB Connection 상태를 함께 관찰한다.

## 테스트 개요

- [Performance Test Overview](./overview.md)

## 시나리오별 결과

### Scenario A - 비로그인 게시판 탐색

- [Smoke Test](./scenario-a-guest-browsing/smoke-test.md)
- [Load Test - 개선 전](./scenario-a-guest-browsing/load-test-before.md)
- [Bottleneck Analysis](./scenario-a-guest-browsing/bottleneck-analysis.md)
- [Load Test - 개선 후](./scenario-a-guest-browsing/load-test-after.md)

### Scenario B - 검색 흐름

- [Smoke Test](./scenario-b-search/smoke-test.md)
- [Load Test - 개선 전](./scenario-b-search/load-test-before.md)
- [Bottleneck Analysis](./scenario-b-search/bottleneck-analysis.md)
- [Load Test - 1차 개선 후](./scenario-b-search/load-test-after.md)

### Scenario C - 로그인 후 글 작성 및 댓글 작성

- [Smoke Test](./scenario-c-write-comment/smoke-test.md)
- [Load Test - 개선 전](./scenario-c-write-comment/load-test-before.md)
- [Bottleneck Analysis](./scenario-c-write-comment/bottleneck-analysis.md)
- [Load Test - 개선 후](./scenario-c-write-comment/load-test-after.md)

### Scenario D - 첨부파일이 있는 게시글 조회

- [Smoke Test](./scenario-d-attachment-detail/smoke-test.md)
- [Bottleneck Analysis](./scenario-d-attachment-detail/bottleneck-analysis.md)
- [Load Test](./scenario-d-attachment-detail/load-test.md)

## 최종 요약

| 시나리오       | 주요 병목     | 개선 내용                          | 결과                |
|------------|-----------|--------------------------------|-------------------|
| Scenario A | 게시글 목록 조회 | 목록 조회에서 user 조인 제거             | 통과                |
| Scenario B | 검색 목록 조회  | FULLTEXT, Slice, Projection 적용 | MySQL 검색 정렬 한계 확인 |
| Scenario C | 게시글 작성    | `post.title` UNIQUE INDEX 적용   | 통과                |
| Scenario D | 게시글 목록 조회 | `Page` → `Slice`, 복합 인덱스 적용    | 통과                |