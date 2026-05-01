package com.example.coreboard.domain.board.dto.query;

import org.springframework.data.domain.Sort;

public record GetBoardListQuery(
        int page,
        int size,
        Sort.Direction direction
) {
}
