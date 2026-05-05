package com.example.coreboard.domain.board.entity;

import com.example.coreboard.domain.users.entity.UserRole;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "board",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_board_name", columnNames = "name"),
                @UniqueConstraint(name = "uk_board_slug", columnNames = "slug")
        }
)
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 사람이 보는 이름: 자유게시판, Q&A, 공지사항
    @Column(name = "name", nullable = false)
    private String name; // 2~20

    // 시스템 주소 이름: free, qna, notice
    @Column(nullable = false, unique = true)
    private String slug; // 2~50
    // 첨부파일 필수냐 (갤러리=true)
    @Column(name = "require_attachment", nullable = false)
    private boolean requireAttachment;
    // 첨부파일 몇 개까지 (자료실=2)
    @Column(name = "max_attachment_count", nullable = false)
    private int maxAttachmentCount;

    // 답변 채택 허용 여부
    @Column(nullable = false)
    private boolean answerAcceptedEnabled;
    // 댓글 허용 여부
    @Column(nullable = false)
    private boolean commentEnabled;

    // 누가 쓸 수 있냐 (예 : 공지사항=ADMIN)
    @Enumerated(EnumType.STRING)
    @Column(name = "required_write_role", nullable = false, length = 20)
    private UserRole allowedWriteRoles;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public Board(
            String name,
            String slug,
            boolean commentEnabled,
            boolean answerAcceptedEnabled,
            boolean requireAttachment,
            int maxAttachmentCount,
            UserRole allowedWriteRoles
    ) {
        this.name = name;
        this.slug = slug;
        this.commentEnabled = commentEnabled;
        this.answerAcceptedEnabled = answerAcceptedEnabled;
        this.requireAttachment = requireAttachment;
        this.maxAttachmentCount = maxAttachmentCount;
        this.allowedWriteRoles = allowedWriteRoles;
    }

    protected Board() {
    }

    public static Board create(
            String name,
            String slug,
            boolean commentEnabled,
            boolean answerAcceptedEnabled,
            boolean requireAttachment,
            int maxAttachmentCount,
            UserRole allowedWriteRoles
    ) {
        Board board = new Board();
        board.name = name;
        board.slug = slug;
        board.commentEnabled = commentEnabled;
        board.answerAcceptedEnabled = answerAcceptedEnabled;
        board.requireAttachment = requireAttachment;
        board.maxAttachmentCount = maxAttachmentCount;
        board.allowedWriteRoles = allowedWriteRoles;
        return board;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public void update(
            String name,
            String slug,
            boolean commentEnabled,
            boolean answerAcceptedEnabled,
            boolean requireAttachment,
            int maxAttachmentCount
    ) {
        this.name = name;
        this.slug = slug;
        this.commentEnabled = commentEnabled;
        this.answerAcceptedEnabled = answerAcceptedEnabled;
        this.requireAttachment = requireAttachment;
        this.maxAttachmentCount = maxAttachmentCount;
    }

    public String getSlug() {
        return slug;
    }

    public boolean isAnswerAcceptedEnabled() {
        return answerAcceptedEnabled;
    }

    public boolean isCommentEnabled() {
        return commentEnabled;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isRequireAttachment() {
        return requireAttachment;
    }

    public int getMaxAttachmentCount() {
        return maxAttachmentCount;
    }

    public UserRole getAllowedWriteRoles() {
        return allowedWriteRoles;
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
    /*
     * 공지사항 게시판은 관리자만 작성 가능하다
     * 갤러리 게시판은 첨부파일이 필수다
     * 자료실 게시판은 첨부파일이 최대 5개다
     * */

    /* TODO : 첨부파일 몇개까지 허용할지, 장단점 파악할 것
     * 설정 컬럼
     * 공지사항 : 첨부파일 선택, 최대 첨부 2
     * 자유게시판 : 첨부파일 선택, 최대 첨부, 코드블록 필요
     * 큐앤에이 : 첨부파일 선택, 최대 첨부, 답변 채택 기능 추가할지
     * */
}
