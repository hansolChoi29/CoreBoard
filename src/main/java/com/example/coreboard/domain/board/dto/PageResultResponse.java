package com.example.coreboard.domain.board.dto;

import java.util.List;

public class PageResultResponse<T> {

    private List<T> contents; // 현제 페이지 데이터
    private int page; // 현재 페이지
    private int size; // 페이지당 데이터 수
    private int totalElements; // 전체 데이터 수
    private int totalPages; // 전체 페이지 수

    public PageResultResponse(
            List<T> contents,
            int page,
            int size,
            int totalElements,
            int totalPages
    ) {
        this.contents = contents;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = (totalElements + size - 1) / size; // 마지막 페이지 올림
    }

    public List<T> getContents() {
        return contents;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public int getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }
}
