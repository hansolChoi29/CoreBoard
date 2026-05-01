package com.example.coreboard.domain.post.repository;

import com.example.coreboard.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    Optional<Post> findById(Long id);
    List<Post> findByBoardId(Long boardId);
    boolean existsByTitle(String title);

    @Query("""
            select b from Post b
            order by b.title desc, b.id desc
            """)
    List<Post> findFirstPageDesc(Pageable pageable);

    @Query("""
            select b from Post b
            where(b.title < :cursorTitle)
            or(b.title = :cursorTitle and b.id < :cursorId)
            order by b.title desc, b.id desc
            """)
    List<Post> findNextPageDesc(
            @Param("cursorTitle") String cursorTitle,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    @Query("""
            select b from Post b
            order by b.title asc , b.id asc
            """)
    List<Post> findFirstPageAsc(Pageable pageable);

    @Query("""
             select b from Post b
            where(b.title > :cursorTitle)
            or(b.title = :cursorTitle and b.id > :cursorId)
            order by b.title asc, b.id asc 
            """)
    List<Post> findNextPageAsc(
            @Param("cursorTitle") String cursorTitle,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    // concat : 문자열 붙이기
    @Query("""
            select b
            from Post b
            where b.title like concat('%', :keyword, '%')
            or b.content like concat('%', :keyword, '%')    
            """)
    List<Post> searchByKeyword(@Param("keyword") String keyword);
}
