package com.example.coreboard.domain.board.service;

import com.example.coreboard.domain.board.dto.BoardCreateCommand;
import com.example.coreboard.domain.board.dto.BoardCreateDto;
import com.example.coreboard.domain.board.dto.BoardCreateRequest;
import com.example.coreboard.domain.board.dto.BoardUpdateRequest;
import com.example.coreboard.domain.board.entity.Board;
import com.example.coreboard.domain.board.repository.BoardRepository;
import com.example.coreboard.domain.common.exception.GlobalExceptionHandler;
import com.example.coreboard.domain.common.exception.auth.AuthErrorCode;
import com.example.coreboard.domain.common.exception.auth.AuthErrorException;
import com.example.coreboard.domain.common.exception.board.BoardErrorCode;
import com.example.coreboard.domain.common.exception.board.BoardErrorException;
import com.example.coreboard.domain.users.entity.Users;
import com.example.coreboard.domain.users.repository.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@Import(GlobalExceptionHandler.class)
class BoardServiceTest {

    @Mock
    BoardRepository boardRepository;

    @Mock
    UsersRepository usersRepository;

    @InjectMocks
    BoardService boardService;

    BoardCreateRequest boardCreateRequest;
    BoardUpdateRequest boardUpdateRequest;
    BoardCreateCommand boardCreateCommand;
    // 실행 전마다 초기화
    @BeforeEach
    void setUpCreate() {
        boardCreateRequest = new BoardCreateRequest(
                "제목",
                "내용"
        );
    }

    // AAA패턴
    // given : 상황이 이렇다(조건 주입) (가짜데이터, mock 설정)
    // when : 이걸 실행했을 때(행동주입) (실제로 메서드를 호출)
    // then : 결과는 이렇게 나와야 한다(기대결과) (검증, verify, assert)

    @Test
    @DisplayName("게시글_생성")
    void create() {
        // given
        Users users = mock(Users.class); // Users는 DB에서 온 엔티티라 가짜로 만든다
        given(usersRepository.findByUsername("tester")).willReturn(Optional.of(users)); // findByUsername에 tester가 오면
        // 서비스가 먼저 유저를 찾으니까, 유저가 있다고 응답해 줘야 다음 단계 진행 가능

        // 제목이 중복이 아니어야 save 단계로 간다
        given(boardRepository.existsByTitle("제목")).willReturn(false);

        // save가 돌려줄 '저장된 엔티티' 준비
        Board saved = new Board(
                1L,
                "제목",
                "내용",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        // save를 호출하면 saved를 돌려주라고 약속
        given(boardRepository.save(any())).willReturn(saved);

        //when
        BoardCreateDto result = boardService.create(boardCreateCommand, "tester");

        // then 1. assert는 리턴값을 검증, 2. verify는 메서드 호출자체를 검증
        assertNotNull(result);
        assertEquals("제목", result.getTitle());
        assertEquals("내용", result.getContent());
        assertNotNull(result.getCreatedDate());

        // time(1) : usersRepository.findByUsername("tester")가 1번 호출되었는지
        // 2번 이상이면 TooManyActualInvocations 에러 던지게
        verify(usersRepository, times(1)).findByUsername("tester");
        verify(boardRepository, times(1)).existsByTitle("제목");
        verify(boardRepository, times(1)).save(any(Board.class));
    }

    @Test
    @DisplayName("게시글_생성_예외_유저없음_404")
    void createUserNotFound() {
        // given(mock, stub)
        // 없는 유저
        given(usersRepository.findByUsername("tester")).willReturn(Optional.empty());
        AuthErrorException notFoundUser = assertThrows(
                AuthErrorException.class,
                () -> boardService.create(
                        boardCreateCommand,
                        "tester"
                )
        );
        assertEquals(404, notFoundUser.getStatus());

        verify(boardRepository, never()).existsByTitle(anyString());
        verify(boardRepository, never()).save(any());
    }

    @Test
    @DisplayName("게시글_생성_중복_제목_409")
    void createTitleDuplicated() {
        // 유저는 있어야 함 (Optional.empty()가 아님)
        Users users = mock(Users.class);
        given(usersRepository.findByUsername("tester")).willReturn(Optional.of(users));

        // 제목 중복
        given(boardRepository.existsByTitle("제목")).willReturn(true);
        BoardErrorException duplicatedBoard = assertThrows(
                BoardErrorException.class,
                () -> boardService.create(
                        boardCreateCommand,
                        "tester"
                )
        );
        assertEquals(409, duplicatedBoard.getStatus());

        verify(usersRepository).findByUsername("tester");
        verify(boardRepository).existsByTitle("제목");
        verify(boardRepository, never()).save(any());
    }

    @Test
    @DisplayName("게시글_단건_조회_성공")
    void findOne() {
        Long id = 1L;
        Board board = mock(Board.class);// Board는 DB에서 온 엔티티라 가짜로 만든다
        given(board.getId()).willReturn(id); // board.getId()가 호출되었을 때 id 반환
        given(boardRepository.findById(id)).willReturn(Optional.of(board));
        // 유저 필요없음
        // id 추출해서 조회
        Board result = boardService.findOne(id);
        assertNotNull(result);
        assertEquals(id, result.getId()); // ??????? 왜 0임?? 저장이안되는듯
        verify(boardRepository, times(1)).findById(1L);
    }

    // 예외처리 - 존재하지 않는 게시글
    @Test
    @DisplayName("게시글_단건_조회_미존재_404")
    void findOnNotFound() {

    }

    @Test
    void findAll() {
    }

    @Test
    void update() {
    }

    @Test
    void delete() {
    }
}