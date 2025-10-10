package com.example.coreboard.domain.board.repository;

import com.example.coreboard.domain.board.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long> {
}
