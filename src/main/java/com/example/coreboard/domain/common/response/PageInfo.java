package com.example.coreboard.domain.common.response;

public class PageInfo {
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;

    public PageInfo(
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }
}
