package com.example.coreboard.domain.common.validation;

import com.example.coreboard.domain.common.exception.board.BoardErrorException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoardValidationTest {

    @Test
    @DisplayName("sort=asc 통과")
    void sortDirection_asc_ok() {
        assertDoesNotThrow(() -> BoardValidation.sortDirection("asc"));
    }

    @Test
    @DisplayName("sort=desc 통과")
    void sortDirection_desc_ok() {
        assertDoesNotThrow(() -> BoardValidation.sortDirection("desc"));
    }

    @Test
    @DisplayName("sort가 asc/desc 아니면 예외 발생")
    void sortDirection_invalid_throw() {
        assertThrows(BoardErrorException.class, () -> BoardValidation.sortDirection("aaa"));
    }
}