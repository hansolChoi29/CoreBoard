package com.example.coreboard.domain.comment.repository;

import com.example.coreboard.domain.comment.entity.Comment;
import com.example.coreboard.domain.comment.entity.CommentStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    // 댓글 목록 응답에 작성자 nickname이 필요하므로,
    // 이 조회 경로에서는 user를 fetch join해 LAZY 로딩으로 인한 N+1 추가 조회를 방지한다
    @Query("""
                select c
                from Comment c
                join fetch c.user
                where c.post.id = :postId
                and c.status = :status
            """)
    Slice<Comment> findByPostIdAndStatusWithUser(
            @Param("postId") Long postId,
            @Param("status") CommentStatus status,
            Pageable pageable
    );
}
