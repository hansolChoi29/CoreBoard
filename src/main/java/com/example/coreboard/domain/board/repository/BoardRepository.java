package com.example.coreboard.domain.board.repository;

import com.example.coreboard.domain.board.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {
    Optional<Board> findById(Long id);

    Page<Board> findAll(Pageable pageable);
    boolean existsByTitle(String title);
}
