package com.example.coreboard.domain.board.entity;

import com.example.coreboard.domain.users.entity.UserRole;
import jakarta.persistence.*;

@Entity
@Table(
        name = "board",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_board_name", columnNames = "name")
        }
)
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "require_attachment", nullable = false)
    private boolean requireAttachment;   // 첨부파일 필수냐 (갤러리=true)

    @Column(name = "max_attachment_count", nullable = false)
    private int maxAttachmentCount;      // 첨부파일 몇 개까지 (자료실=5)

    @Column(name = "max_content_length", nullable = false)
    private int maxContentLength = 10000;        // 본문 최대 길이

    @Enumerated(EnumType.STRING)
    @Column(name = "required_write_role", nullable = false, length = 20)
    private UserRole requiredWriteRole; // 누가 쓸 수 있냐 (공지사항=ADMIN)

    public Board(
            String name,
            boolean requireAttachment,
            int maxAttachmentCount,
            int maxContentLength,
            UserRole requiredWriteRole
    ) {
        this.name = name;
        this.requireAttachment = requireAttachment;
        this.maxAttachmentCount = maxAttachmentCount;
        this.maxContentLength = maxContentLength;
        this.requiredWriteRole = requiredWriteRole;
    }

    protected Board() {
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
