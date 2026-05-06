package com.example.coreboard.domain.common.response;

import org.springframework.data.domain.Slice;

import java.util.List;

public record SliceResponse<T>(
        List<T> content,
        SliceInfo sliceInfo
) {
    public static <T> SliceResponse<T> from(Slice<T> slice) {
        return new SliceResponse<>(
                slice.getContent(),
                new SliceInfo(
                        slice.getSize(),
                        slice.getNumberOfElements(),
                        slice.hasNext()
                )
        );
    }
}
