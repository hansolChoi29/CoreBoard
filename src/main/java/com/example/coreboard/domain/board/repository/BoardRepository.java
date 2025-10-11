package com.example.coreboard.domain.board.repository;

import com.example.coreboard.domain.board.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long> {

    // 클라이언트 요청(page=2, size=10)
    // -> 컨트롤러(page, size)
    // -> 서비스(page, size)
    // -> 페이지 계산
    // -> PageResult<Board> 생성 (contents, page, size, totalElements, totalPages)
    // -> 응답
    Page<Board> findAllByUsername(String username, Pageable pageable);
}
