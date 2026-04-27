package com.example.coreboard.domain.board.entity;

import com.example.coreboard.domain.users.entity.UserRole;
import jakarta.persistence.*;

@Entity
@Table(name = "board")
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(name = "required_write_role", nullable = false, length = 20)
    private UserRole requiredWriteRole = UserRole.USER;

    protected Board() {
    }

    public Board(
            Long id,
            String name,
            String slug,
            UserRole requiredWriteRole
    ) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.requiredWriteRole = requiredWriteRole;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public UserRole getRequiredWriteRole() {
        return requiredWriteRole;
    }
    /*
     * 공지사항 게시판은 관리자만 작성 가능하다
     * 갤러리 게시판은 첨부파일이 필수다
     * 자료실 게시판은 첨부파일이 최대 5개다
     * */
}
