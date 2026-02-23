package com.example.coreboard.domain.common.response;

import java.util.List;

public class CursorResponse<T> {
    private List<T> contents;
    private String nextCursorTitle;
    private Long nextCursorId;
    private boolean hasNext;

    public CursorResponse(
            List<T> contents,
            String nextCursorTitle,
            Long nextCursorId,
            boolean hasNext
        ) {
        this.contents = contents;
        this.nextCursorTitle = nextCursorTitle;
        this.nextCursorId = nextCursorId;
        this.hasNext = hasNext;
    }

    public List<T> getContents() {
        return contents;
    }

    public String getNextCursorTitle() {
        return nextCursorTitle;
    }

    public Long getNextCursorId() {
        return nextCursorId;
    }

    public boolean isHasNext() {
        return hasNext;
    }
}
