package com.example.coreboard.domain.board.repository;

import com.example.coreboard.domain.board.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {
    Optional<Board> findById(Long id);

    boolean existsByTitle(String title);

    // 처음 게시판 들어왔을 때
    @Query("""
            select b from Board b
            where(b.title < :cursorTItle)
            or(b.title = :cursorTitle and b.id < :cursorId)
            order by b.title desc, b.id desc
            limit :size
            """)
    List<Board> findNextPage(
            @Param("cursorTitle") String cursorTitle,
            @Param("cursorId") Long cursorId,
            @Param("size") int size);

    // 다음 페이지 눌렀을 때
    @Query("""
            select b from Board b
            order by b.title desc, b.id desc
            limit :size
            """)
    List<Board> findFirstPage(@Param("size") int size);
}
