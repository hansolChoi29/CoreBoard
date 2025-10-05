package com.example.coreboard.domain.board.controller;

import com.example.coreboard.domain.board.dto.BoardResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@RequestMapping("/api/board")
public class Controller {
    
    //단건 조회
    @GetMapping("/{board}")
    public ResponseEntity<BoardResponseDto> readBoard(){ // 응답이어야 함
        List<BoardResponseDto> responseList = new ArrayList<>(); // 순서 보장, 중복 보장
        return new ResponseEntity<>(new BoardResponseDto(), HttpStatus.OK);
    }
}
