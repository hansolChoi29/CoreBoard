package com.example.coreboard.domain.common.validation;

import com.example.coreboard.domain.common.exception.post.PostErrorException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PostValidationTest {

    @Test
    @DisplayName("sort=asc 통과")
    void sortDirection_asc_ok() {
        assertDoesNotThrow(() -> PostValidation.validateSortDirection("asc"));
    }

    @Test
    @DisplayName("sort=desc 통과")
    void sortDirection_desc_ok() {
        assertDoesNotThrow(() -> PostValidation.validateSortDirection("desc"));
    }

    @Test
    @DisplayName("sort가 asc/desc 아니면 예외 발생")
    void sortDirection_invalid_throw() {
        assertThrows(PostErrorException.class, () -> PostValidation.validateSortDirection("aaa"));
    }
}