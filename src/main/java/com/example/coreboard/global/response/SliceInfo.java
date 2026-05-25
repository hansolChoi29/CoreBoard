package com.example.coreboard.global.response;

public record SliceInfo(
        int size,
        int numberOfElement,
        boolean hasNext
) {
}
