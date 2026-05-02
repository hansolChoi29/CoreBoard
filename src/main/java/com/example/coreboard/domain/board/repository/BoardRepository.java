package com.example.coreboard.domain.board.repository;

import com.example.coreboard.domain.board.entity.Board;
import com.example.coreboard.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
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
