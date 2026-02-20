package com.example.coreboard.domain.common.response;

public class SliceResponse<T> {
    private Long lastId;

    public SliceResponse(Long lastId) {
        this.lastId = lastId;
    }

    public Long getLastId(Long lastId) {
        return lastId;
    }
}
