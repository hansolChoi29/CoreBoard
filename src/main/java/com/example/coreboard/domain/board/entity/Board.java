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
    private String name;

    // 시스템 주소 이름: free, qna, notice
    @Column(nullable = false, unique = true)
    private String slug;
    // 첨부파일 필수냐 (갤러리=true)
    @Column(name = "require_attachment", nullable = false)
    private boolean requireAttachment;
    // 첨부파일 몇 개까지 (자료실=5)
    @Column(name = "max_attachment_count", nullable = false)
    private int maxAttachmentCount;
    // 본문 최대 길이
    @Column(name = "max_content_length", nullable = false)
    private int maxContentLength = 10000;
    /* TODO : adr 004

        1. 사용자 지정 V - 커스텀 취지
        2. 임의 지정
        /boards/job-interview/posts
        {
            "name": "취업/면접",
            "slug": "job-interview"
        }
        */

    // 답변 채택 허용 여부
    @Column(nullable = false)
    private boolean answerAcceptedEnabled;
    // 댓글 허용 여부
    @Column(nullable = false)
    private boolean commentEnabled;

    // 누가 쓸 수 있냐 (공지사항=ADMIN)
    @Enumerated(EnumType.STRING)
    @Column(name = "required_write_role", nullable = false, length = 20)
    private UserRole requiredWriteRole;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public Board(
            String name,
            String slug,
            boolean commentEnabled,
            boolean answerAcceptedEnabled,
            boolean requireAttachment,
            int maxAttachmentCount,
            int maxContentLength,
            UserRole requiredWriteRole
    ) {
        this.name = name;
        this.slug = slug;
        this.commentEnabled = commentEnabled;
        this.answerAcceptedEnabled = answerAcceptedEnabled;
        this.requireAttachment = requireAttachment;
        this.maxAttachmentCount = maxAttachmentCount;
        this.maxContentLength = maxContentLength;
        this.requiredWriteRole = requiredWriteRole;
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
            int maxContentLength
    ) {
        Board board = new Board();
        board.name = name;
        board.slug = slug;
        board.commentEnabled = commentEnabled;
        board.answerAcceptedEnabled = answerAcceptedEnabled;
        board.requireAttachment = requireAttachment;
        board.maxAttachmentCount = maxAttachmentCount;
        board.maxContentLength = maxContentLength;
        board.requiredWriteRole = UserRole.USER;
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
            int maxAttachmentCount,
            int maxContentLength
    ) {
        this.name = name;
        this.slug = slug;
        this.commentEnabled = commentEnabled;
        this.answerAcceptedEnabled = answerAcceptedEnabled;
        this.requireAttachment = requireAttachment;
        this.maxAttachmentCount = maxAttachmentCount;
        this.maxContentLength = maxContentLength;
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

    public int getMaxContentLength() {
        return maxContentLength;
    }

    public UserRole getRequiredWriteRole() {
        return requiredWriteRole;
    }

    /*
     * 공지사항 게시판은 관리자만 작성 가능하다
     * 갤러리 게시판은 첨부파일이 필수다
     * 자료실 게시판은 첨부파일이 최대 5개다
     * */

    /* TODO : 첨부파일 몇개까지 허용할지, 장단점 파악할 것
     * 설정 컬럼
     * 공지사항 : 첨부파일 선택, 최대 첨부 5?
     * 자유게시판 : 첨부파일 선택, 최대 첨부, 코드블록 필요
     * 큐앤에이 : 첨부파일 선택, 최대 첨부, 답변 채택 기능 추가할지
     * */
}
