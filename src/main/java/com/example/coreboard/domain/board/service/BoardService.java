package com.example.coreboard.domain.board.service;


import com.example.coreboard.domain.board.dto.*;
import com.example.coreboard.domain.board.entity.Board;
import com.example.coreboard.domain.board.repository.BoardRepository;
import com.example.coreboard.domain.common.exception.auth.AuthErrorException;
import com.example.coreboard.domain.common.exception.board.BoardErrorException;
import com.example.coreboard.domain.common.response.ApiResponse;
import com.example.coreboard.domain.users.entity.Users;
import com.example.coreboard.domain.users.repository.UsersRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.coreboard.domain.common.exception.auth.AuthErrorCode.*;
import static com.example.coreboard.domain.common.exception.board.BoardErrorCode.*;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;

@Service
public class BoardService {
    private final BoardRepository boardRepository;
    private final UsersRepository usersRepository;

    public BoardService(
            BoardRepository boardRepository,
            UsersRepository usersRepository
    ) {
        this.boardRepository = boardRepository;
        this.usersRepository = usersRepository;
    }

    // 보드 생성
    public Board create(
            BoardCreateRequest boardRequestDto,
            String username // 인터셉터에서 가로채 검증을 끝내고 반환된 username을 컨트롤러에서 받아와 board에 저장하기
    ) {
        // users 테이블의 username이 들어있으면 값을 user에 담는다. (반환용)
        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new AuthErrorException(NOT_FOUND));

        // 제목 중복 검사
        if (boardRepository.existsByTitle(boardRequestDto.getTitle())) {
            throw new BoardErrorException(TITLE_DUPLICATED);
        }

        // 보드 저장할 것들 세팅
        Board board = Board.create(
                user.getUserId(),
                boardRequestDto.getTitle(),
                boardRequestDto.getContent()
        );

        boardRepository.save(board); // 저장
        return board;
    }

    // 보드 단건 조회 - 멱등
    public BoardGetOneResponse findOne(
            Long id
    ) {

        Board board = boardRepository.findById(id) // id 추출하는 메서드 이용해서
                .orElseThrow(() -> new BoardErrorException(POST_NOT_FOUND)); // 값이 있으면 반환 없으면 에러 던짐

        // 트러블 - board만 넣었더니 500 에러: 단건 조회용, 타이틀과 본문 응답 반환
        return new BoardGetOneResponse(
                board.getId(),
                board.getUserId(),
                board.getTitle(),
                board.getContent(),
                board.getCreatedDate(),
                board.getLastModifiedDate()
        );
    }

    // 보드 전체 조회 - 멱등
    public PageResponse<BoardSummaryResponse> findAll(int page, int size, String sort
    ) {
        // Sort.Direction : Spring 전용 Enum(Sort.Direction.ASC, Sort.Direction.DESC)
        Sort.Direction direction = sort.equalsIgnoreCase("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "title"));

        // Page<Board>타입의 result 즉 게시글 여러개를 페이지네이션해서 담고 있는 객체.
        Page<Board> result = boardRepository.findAll(pageable);

        // contents는 게시글 응답DTO를 담을 리스트, 엔티티 그대로 반환하지 않고 필요한 정보만 담은 DTO객체들로 변환해서 저장하기 위한 용도
        List<BoardSummaryResponse> contents = new ArrayList<>(); // ArrayList는 비어있는 상태

        // result 안의 게시글들을 하나씩 꺼내서 DTO(summary)로 변환하고 contents 리스트에 차곡차곡 추가
        for (Board board : result.getContent()) { // result.getContent(): 게시글 리스트를 꺼내옴
            contents.add(new BoardSummaryResponse(
                    board.getId(),
                    board.getUserId(),
                    board.getTitle(),
                    board.getCreatedDate()
            ));
        }

        // DB에서 꺼낸 페이지네이션 결과를 API 응답형식(PageResponse)로 감쌈
        PageResponse<BoardSummaryResponse> body = new PageResponse<>(
                contents,
                result.getNumber(),       // 현재 페이지 번호
                result.getSize(),         // 한 페이지에 몇 개
                result.getTotalElements() // 전체 게시글 수
        );

        return ApiResponse.ok(body, "게시글 전체 조회!").getData();
    }

    // 보드 수정 트러블 - 성공응답 나오지만, 조회 시 수정이 안되는 이슈 발생(Transactional)
    @Transactional
    public BoardUpdateResponse update(
            BoardUpdateRequest boardupdateRequest,
            String username,
            Long id
    ) {
        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new AuthErrorException(NOT_FOUND));

        Board board = boardRepository.findById(id) // id 추출하는 메서드 이용해서
                .orElseThrow(() -> new BoardErrorException(POST_NOT_FOUND)); // 값이 있으면 반환 없으면 에러 던짐

        // 권한 체크
        if (board.getUserId() != user.getUserId()) {
            throw new AuthErrorException(FORBIDDEN);
        }

        // 저장
        board.update(
                boardupdateRequest.getTitle(),
                boardupdateRequest.getContent()
        );

        return new BoardUpdateResponse(
                board.getId(),
                user.getUserId(),
                board.getTitle(),
                board.getContent(),
                board.getLastModifiedDate()
        );
    }

    // 보드 삭제
    public BoardDeleteResponse delete(
            String username,
            Long id
    ) {
        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new AuthErrorException(NOT_FOUND));

        Board board = boardRepository.findById(id) // id 추출하는 메서드 이용해서
                .orElseThrow(() -> new BoardErrorException(POST_NOT_FOUND)); // 값이 있으면 반환 없으면 에러 던짐

        if (board.getUserId() != user.getUserId()) { // 권한 체크
            throw new AuthErrorException(FORBIDDEN);
        }

        boardRepository.delete(board); // 스프링에서 제공되는 삭제 메서드

        return new BoardDeleteResponse(
                board
        );
    }
}
