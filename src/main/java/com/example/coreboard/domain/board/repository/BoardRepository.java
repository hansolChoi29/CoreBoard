package com.example.coreboard.domain.board.repository;

import com.example.coreboard.domain.board.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long> {

    // Page<Board> : 반환타입, 게시글 목록과 페이지 정보를 함께 담은 객체
    // (Pageable pageable) : 어떻게 가져올지 정보를 담음
    // Pageable에 들어가는 값 예시 : PageRequest.of(0, 10, Sort.by("createdDate").descending());
    Page<Board> findAll(Pageable pageable); // JPA가 제공하는 Pageable 이용하여 PageRequest 기반 오프셋 만들기
}
