package com.example.coreboard.domain.board.repository;

import com.example.coreboard.domain.board.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long> {

    // 오프셋
    // page가 커질수록 OFFSET이 커져서 느려짐 (대용량 테이블에서 비효율)
    // Spring Date JPA 기본 지원(Pageable, Page), 표준 정렬 지원(Sort)
    // LIMIT : 한 페이지에 몇 개 보여줄지
    // OFFSET : 몇 개 건너뛸지 계산 (page * size)
    // COUNT : 전체 데이터 개수 계산

    Page<Board> findAll(Pageable pageable); // JPA가 제공하는 Pageable 이용하여 PageRequest 기반 오프셋 만들기
    boolean existsByTitle(String title);
}
