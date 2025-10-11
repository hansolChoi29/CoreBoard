package com.example.coreboard.domain;

import com.example.coreboard.domain.board.entity.Board;
import com.example.coreboard.domain.board.repository.BoardRepository;
import org.antlr.v4.runtime.misc.LogManager;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

public class BaordLocalDateTimeTest {
    @Test
    void localDateTimeTest(){
        final BoardRepository boardRepository=null;
        final Board board=new Board("title", "contents");

        String username ="fjdklsa12";

        Object pageable =10;

    }
}
