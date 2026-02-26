package com.example.coreboard.domain.board.repository;

import com.example.coreboard.domain.board.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {
    Optional<Board> findById(Long id);

    boolean existsByTitle(String title);

    @Query("""
            select b from Board b
            order by b.title desc, b.id desc
            """)
    List<Board> findFirstPageDesc(Pageable pageable);

    @Query("""
            select b from Board b
            where(b.title < :cursorTitle)
            or(b.title = :cursorTitle and b.id < :cursorId)
            order by b.title desc, b.id desc
            """)
    List<Board> findNextPageDesc(
            @Param("cursorTitle") String cursorTitle,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    @Query("""
            select b from Board b
            order by b.title asc , b.id asc
            """)
    List<Board> findFirstPageAsc(Pageable pageable);

    @Query("""
            select b from  Board b
            where (b.title < :cursorTitle)
            or(b.title=:cursorTitle and b.id < :cursorId)
            order by b.title asc, b.id asc
            """)
    List<Board> findNextPageAsc(
            @Param("cursorTitle") String cursorTitle,
            @Param("cursorId") int cursorId,
            Pageable pageable
    );
}
