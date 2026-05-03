package com.example.coreboard.domain.support.fixture;

import com.example.coreboard.domain.board.entity.Board;
import com.example.coreboard.domain.users.entity.UserRole;

public class BoardFixture {
    private BoardFixture() {
    }

    public static Board freeBoard() {
        return new Board(
                "자유게시판",
                "free",
                true,
                false,
                false,
                3,
                UserRole.USER
        );
    }

    public static Board qnaBoard() {
        return new Board("Q&A",
                "qna",
                true,
                true,
                false,
                3,
                UserRole.USER);
    }

    public static Board noticeBoard() {
        return new Board(
                "공지사항",
                "notice",
                false,
                false,
                false,
                5,
                UserRole.ADMIN);
    }

    public static Board customBoard(String name, String slug) {
        return new Board(
                name,
                slug,
                true,
                false,
                false,
                3,
                UserRole.USER);
    }
}
