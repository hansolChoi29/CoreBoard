package com.example.coreboard.domain.comment.repository;

import com.example.coreboard.domain.comment.entity.Comment;
import com.example.coreboard.domain.comment.entity.CommentStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {
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
