package com.example.coreboard.domain.post.repository;

import com.example.coreboard.domain.post.entity.Post;
import com.example.coreboard.domain.post.entity.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    Optional<Post> findByIdAndStatus(Long id, PostStatus status);
    boolean existsByTitle(String title);

    boolean existsByBoardId(Long boardId);

    // getOne
    @Query("""
            select p
            from Post p
            join fetch p.user
            where p.board.id = :boardId
            and p.status = :status
            """)
    List<Post> findAllByBoardIdWithUser(
            @Param("boardId") Long boardId,
            @Param("status") PostStatus status
    );

    // 전체조회
    @Query("""
                    select p
                    from Post p
                    join fetch p.user
                    where p.board.id = :boardId
                    and p.status = :status
            """)
    Page<Post> findAllByBoardId(
            @Param("boardId") Long boardId,
            @Param("status") PostStatus status,
            Pageable pageable
    );
}
