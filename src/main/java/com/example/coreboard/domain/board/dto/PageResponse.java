package com.example.coreboard.domain.board.dto;

import java.util.List;

// 컨테이너 안에 담길 리스트가 필요한데 안에 담길 리스트 아이템 타입T 필요함
// 엔티티로 쓰면 레이어 위반, DTO 같은 걸 둬서 엔티티를 바깥으로 안 보내게 하기
public class PageResponse<T> {
    private final List<T> content;
    private final int page;
    private final int size;
    private final long totalElements;

    public PageResponse(
            List<T> content,
            int page,
            int size,
            long totalElements

    ) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
    }

    public List<T> getContent() {
        return content;
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
}
