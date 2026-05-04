package com.example.coreboard.domain.board.repository;

import com.example.coreboard.domain.board.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {
    Page<Board> findByDeletedAtIsNull(Pageable pageable);

    // create
    boolean existsBySlugAndDeletedAtIsNull(String slug);

    boolean existsByNameAndDeletedAtIsNull(String name);

    //update
    Optional<Board> findByIdAndDeletedAtIsNull(Long id);

    boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id);

    boolean existsBySlugAndIdNotAndDeletedAtIsNull(String slug, Long id);
}
