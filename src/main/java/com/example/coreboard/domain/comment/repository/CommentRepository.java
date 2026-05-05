package com.example.coreboard.domain.comment.repository;

import com.example.coreboard.domain.comment.entity.Comment;
import com.example.coreboard.domain.comment.entity.CommentStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Slice<Comment> findByPostIdAndStatus(
            Long postId,
            CommentStatus status,
            Pageable pageable
    );
}
