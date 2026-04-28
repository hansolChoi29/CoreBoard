package com.example.coreboard.domain.post.entity;

import com.example.coreboard.domain.board.entity.Board;
import com.example.coreboard.domain.users.entity.UserRole;
import com.example.coreboard.domain.users.entity.Users;
import org.junit.jupiter.api.Test;


import static com.example.coreboard.domain.support.fixture.BoardFixture.*;
import static org.junit.jupiter.api.Assertions.*;

class PostTest {

    @Test
    void update() {
        Board board = freeBoard();
        Post post = new Post(
                board,
                new Users("username", "nickname", "password", "qwe@qwe.com", "01012341234", UserRole.USER),
                "기존 제목",
                "기존 내용",
                ContentFormat.MARKDOWN
        );

        post.update("새 제목", "새 내용", ContentFormat.MARKDOWN);

        assertEquals("새 제목", post.getTitle());
        assertEquals("새 내용", post.getContent());
    }

    @Test
    void update_isBlank() {
        Board board = freeBoard();

        Post post = new Post(
                board,
                new Users("username", "nickname", "password", "qwe@qwe.com", "01012341234", UserRole.USER),
                "기존 데이터",
                "기존 데이터",
                ContentFormat.MARKDOWN
        );

        post.update("   ", "     ", ContentFormat.MARKDOWN);
        assertEquals("기존 데이터", post.getTitle());
        assertEquals("기존 데이터", post.getContent());
    }

    @Test
    void update_isEmpty() {
        Board board = freeBoard();

        Post post = new Post(
                board,
                new Users("username", "nickname", "password", "qwe@qwe.com", "01012341234", UserRole.USER),
                "기존 데이터",
                "기존 데이터",
                ContentFormat.MARKDOWN
        );
        post.update("  ", "   ", ContentFormat.MARKDOWN);

        assertEquals("기존 데이터", post.getTitle());
        assertEquals("기존 데이터", post.getContent());
    }

    @Test
    void update_null() {
        Board board = freeBoard();

        Post post = new Post(
                board,
                new Users("username", "nickname", "password", "qwe@qwe.com", "01012341234", UserRole.USER),
                "기존 데이터",
                "기존 데이터",
                ContentFormat.MARKDOWN
        );
        post.update(null, null, ContentFormat.MARKDOWN);

        assertEquals("기존 데이터", post.getTitle());
        assertEquals("기존 데이터", post.getContent());
    }
}