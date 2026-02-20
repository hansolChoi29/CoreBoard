package com.example.coreboard.domain.board.dto.response;

import java.util.List;

public record BoardSummaryKeysetResponse(
    List<BoardSummaryResponse> content,
    Long nextLastId,
    boolean hasNext
) {}
    // 화면에 보여줄 게시글 목록
    // 다음 요청에 넘길 커서
