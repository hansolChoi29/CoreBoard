# CoreBoard Performance Test

CoreBoard의 주요 사용자 흐름을 기준으로 JMeter 부하 테스트를 수행하고,
Prometheus/Grafana를 통해 응답 시간과 서버 자원 사용률을 관찰한다.

## 문서 목록

- [부하 테스트 계획서](./load-test-plan.md)

## 시나리오별 결과

### Scenario A - 비로그인 게시판 탐색

- [Smoke Test](./scenario-a-guest-browsing/smoke-test.md)
- [Load Test](./scenario-a-guest-browsing/load-test.md)
- [Bottleneck Analysis](./scenario-a-guest-browsing/bottleneck-analysis.md)

### Scenario B - 검색 흐름

- [Smoke Test](./scenario-b-search/smoke-test.md)
- [Load Test](./scenario-b-search/load-test.md)
- [Bottleneck Analysis](./scenario-b-search/bottleneck-analysis.md)

### Scenario C - 로그인 후 글 작성 및 댓글 작성

- [Smoke Test](./scenario-c-write-comment/smoke-test.md)
- [Load Test](./scenario-c-write-comment/load-test.md)
- [Bottleneck Analysis](./scenario-c-write-comment/bottleneck-analysis.md)

### Scenario D - 첨부파일이 있는 게시글 조회

- [Smoke Test](./scenario-d-attachment-detail/smoke-test.md)
- [Load Test](./scenario-d-attachment-detail/load-test.md)
- [Bottleneck Analysis](./scenario-d-attachment-detail/bottleneck-analysis.md)