package com.example.coreboard.global.response;

public record PageInfo(
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
