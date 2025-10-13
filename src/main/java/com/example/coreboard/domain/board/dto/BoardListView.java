package com.example.coreboard.domain.board.dto;

import java.time.LocalDateTime;

public interface BoardListView {
    Long getId();
    long getUserId();
    String getBoardTitle();
    String getBoardContents();
    LocalDateTime getCreatedDate();
}
