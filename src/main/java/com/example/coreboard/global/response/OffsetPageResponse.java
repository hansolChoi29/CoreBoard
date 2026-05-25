package com.example.coreboard.global.response;

import java.util.List;

public record OffsetPageResponse<T>(
        List<T> content,
        PageInfo pageInfo
) {
}
