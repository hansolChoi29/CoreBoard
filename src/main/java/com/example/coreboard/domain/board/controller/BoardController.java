package com.example.coreboard.domain.board.controller;


import com.example.coreboard.domain.board.service.BoardService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class BoardController {
    private final BoardService BoardService;

    public BoardController(BoardService boardService) {
        BoardService = boardService;
    }
}
