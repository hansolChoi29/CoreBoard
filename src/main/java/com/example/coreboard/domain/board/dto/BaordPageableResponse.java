package com.example.coreboard.domain.board.dto;

import com.example.coreboard.domain.board.entity.Board;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

public class BaordPageableResponse {
    // 수정일 기준으로 정렬하지 않은 이유 : 순서가 뒤죽박죽 될 수 있음 (댓글, 조회수가 있는 경우)
    private final Long id;
    private final String username;
    private final String boardTitle;
    private final String boardContents;
    private final LocalDateTime createdDate;

    public BaordPageableResponse(
            Long id,
            String username,
            String boardTitle,
            String boardContents,
            LocalDateTime createdDate
    ) {
        this.id = id;
        this.username = username;
        this.boardTitle = boardTitle;
        this.boardContents = boardContents;
        this.createdDate = createdDate;
    }

    public BaordPageableResponse(Page<Board> board, Long id, String username, String boardTitle, String boardContents, LocalDateTime createdDate) {
        this.id = id;
        this.username = username;
        this.boardTitle = boardTitle;
        this.boardContents = boardContents;
        this.createdDate = createdDate;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getBoardTitle() {
        return boardTitle;
    }

    public String getBoardContents() {
        return boardContents;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    /*
     * 전체 조회 시 응답 값
     * 보드 id
     * 유저id
     * contents
     * title
     * createDate
     * 보드 id
     * 유저id
     * contents
     * title
     * createDate
     * */
}
