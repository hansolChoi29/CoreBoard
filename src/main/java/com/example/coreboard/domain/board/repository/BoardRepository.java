package com.example.coreboard.domain.board.repository;

import com.example.coreboard.domain.board.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {
    Optional<Board> findById(Long id);

    Page<Board> findAll(Pageable pageable);

    boolean existsByTitle(String title);

    /*
     * Keyset은 cursor 조건을 직접 넣는 메서드
     * 마지막으로 본 데이터의 값을 기준으로 다음 데이터를 가져온다 - 페이지 개념이 없다
     * 예를들어 lastId = null인 경우 아직 본 데이터가 없다 - 최신부터
     * 
     * @Query + List 반환 + size+1
     */
    @Query("""
            select b from Board b
            where(:lastId is null or b.id < :lastId)
            order by b.id desc
            """)
    List<Board> findNextPage(@Param("lastId") Long lastId, Pageable pageable);
}
