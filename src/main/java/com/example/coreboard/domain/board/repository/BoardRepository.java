package com.example.coreboard.domain.board.repository;

import com.example.coreboard.domain.board.entity.Board;
import com.example.coreboard.domain.users.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {
    Optional<Board> findById(Long id);
    boolean existsBySlug(String slug);
    boolean existsByName(String name);
}
