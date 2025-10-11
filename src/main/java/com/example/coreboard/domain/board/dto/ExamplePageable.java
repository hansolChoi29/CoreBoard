package com.example.coreboard.domain.board.dto;

public class ExamplePageable {
    private int page; // 현재 요청한 페이지 번호
    private int size; // 페이지당 데이터 수

    // 클라이언트에서 요청한 페이지 번호와 페이지당 사이즈를 객체로 전달하고 레포/서비스에서 계산 통일하기 위함

    public ExamplePageable(
            int page,
            int size
    ) {
        if (page <= 0) // 페이지 번호가 0이거나 음수로 요청된 경우 1페이지로 강제
            page = 1;

        if (size <= 0) // 한 페이지에 보여줄 데이터 수가 0 또는 음수인 경우 10개로 강제
            size = 10;

        this.page = page;
        this.size = size;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    // 정적 펙터리 메서드 - 생성자 대신, 같은 객체 재사용 하도록
    public static ExamplePageable of(int page, int size) {
        return new ExamplePageable(page, size);
    }

    // 계산식
    // fromIndex = (page - 1) * size
    // toIndex = min(fromIndex + size, totlaElements)

    // DB조회용 계산기 - 리스트의 fromIndex를 정확히 계산해서 페이지 단위 데이터를 자르기 위함
    public int getOf() {
        return (page - 1) * size;
    }
}
