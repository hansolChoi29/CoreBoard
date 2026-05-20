`ORDER BY created_at DESC`를 제거한 실행 계획에서는 `Using filesort`가 사라졌다.
따라서 현재 검색 지연에는 FULLTEXT 검색 결과를 최신순으로 정렬하는 비용이 포함되어 있는 것으로 판단했다.
ORDER BY 있음: 약 8초
ORDER BY 제거: 0.33초
ORDER BY 제거 시 단일 요청 시간이 약 8초에서 0.33초로 감소했다.
반면 FULLTEXT relevance score 기준 정렬을 적용해도 단일 요청 시간은 약 8초 수준으로 유지되었다.

따라서 남은 병목은 특정 정렬 컬럼의 문제가 아니라, FULLTEXT 검색 결과 후보를 정렬하는 작업 자체로 판단한다.

