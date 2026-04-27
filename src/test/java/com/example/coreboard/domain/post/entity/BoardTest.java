package com.example.coreboard.domain.post.entity;

import com.example.coreboard.domain.board.entity.Board;
import com.example.coreboard.domain.users.entity.UserRole;
import com.example.coreboard.domain.users.entity.Users;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    @Test
    void update() {
        Post post = new Post(
                new Board("free", false, 0, 8000, UserRole.USER),
                new Users("username", "password", "qwe@qwe.com", "01012341234", UserRole.USER),
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
        Post post = new Post(
                new Board("free", false, 0, 8000, UserRole.USER),
                new Users("username", "password", "qwe@qwe.com", "01012341234", UserRole.USER),
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
        Post post = new Post(
                new Board("free", false, 0, 8000, UserRole.USER),
                new Users("username", "password", "qwe@qwe.com", "01012341234", UserRole.USER),
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
        Post post = new Post(
                new Board("free", false, 0, 8000, UserRole.USER),
                new Users("username", "password", "qwe@qwe.com", "01012341234", UserRole.USER),
                "기존 데이터",
                "기존 데이터",
                ContentFormat.MARKDOWN
        );
        post.update(null, null, ContentFormat.MARKDOWN);

        assertEquals("기존 데이터", post.getTitle());
        assertEquals("기존 데이터", post.getContent());
    }
}