package com.example.coreboard.domain.board.service;

import com.example.coreboard.domain.board.dto.*;
import com.example.coreboard.domain.board.dto.command.BoardCreateCommand;
import com.example.coreboard.domain.board.dto.command.BoardGetOneCommand;
import com.example.coreboard.domain.board.dto.command.BoardUpdateCommand;
import com.example.coreboard.domain.board.dto.request.BoardCreateRequest;
import com.example.coreboard.domain.board.dto.response.BoardSummaryKeysetResponse;
import com.example.coreboard.domain.board.dto.response.BoardSummaryResponse;
import com.example.coreboard.domain.board.entity.Board;
import com.example.coreboard.domain.board.repository.BoardRepository;
import com.example.coreboard.domain.common.exception.auth.AuthErrorException;
import com.example.coreboard.domain.common.exception.board.BoardErrorException;
import com.example.coreboard.domain.common.response.CursorResponse;
import com.example.coreboard.domain.common.response.PageResponse;
import com.example.coreboard.domain.users.entity.Users;
import com.example.coreboard.domain.users.repository.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoardServiceTest {
    private static final LocalDateTime FIXED_TIME = LocalDateTime.of(2026, 1, 1, 0, 0);

    @Mock
    BoardRepository boardRepository;

    @Mock
    UsersRepository usersRepository;

    @InjectMocks
    BoardService boardService;

    BoardCreateRequest boardCreateRequest;
    BoardCreateCommand boardCreateCommand;

    @BeforeEach
    void setUpCreate() {
        boardCreateRequest = new BoardCreateRequest("제목", "내용");
        boardCreateCommand = new BoardCreateCommand("제목", "내용");
    }

    @Test
    @DisplayName("게시글_생성")
    void create() {
        Users users = new Users("tester", "password", "user01@naver.com", "01012341234");

        ReflectionTestUtils.setField(users, "userId", 10L);

        given(usersRepository.findByUsername("tester")).willReturn(Optional.of(users));
        given(boardRepository.existsByTitle("제목")).willReturn(false);

        Board saved = new Board(1L, 10L, "제목", "내용", FIXED_TIME, FIXED_TIME);

        given(boardRepository.save(any(Board.class))).willReturn(saved);

        BoardCreateDto result = boardService.create(boardCreateCommand, "tester");

        assertNotNull(result);
        assertEquals("제목", result.getTitle());
        assertEquals("내용", result.getContent());

        ArgumentCaptor<Board> captor = ArgumentCaptor.forClass(Board.class);
        verify(boardRepository).save(captor.capture());
        Board toSave = captor.getValue();

        assertEquals(10L, toSave.getUserId());
        assertEquals("제목", toSave.getTitle());
        assertEquals("내용", toSave.getContent());

        verify(usersRepository).findByUsername("tester");
        verify(boardRepository).existsByTitle("제목");
    }

    @Test
    @DisplayName("게시글_생성_예외_유저없음_404")
    void createUserNotFound() {
        given(usersRepository.findByUsername("tester")).willReturn(Optional.empty());
        AuthErrorException notFoundUser = assertThrows(
                AuthErrorException.class,
                () -> boardService.create(
                        boardCreateCommand,
                        "tester"));
        assertEquals(HttpStatus.NOT_FOUND, notFoundUser.getStatus());

        verify(boardRepository, never()).existsByTitle(anyString());
        verify(boardRepository, never()).save(any());
    }

    @Test
    @DisplayName("게시글_생성_중복_제목_409")
    void createTitleDuplicated() {
        Users users = mock(Users.class);
        given(usersRepository.findByUsername("tester")).willReturn(Optional.of(users));
        given(boardRepository.existsByTitle("제목")).willReturn(true);

        BoardErrorException duplicatedBoard = assertThrows(
                BoardErrorException.class,
                () -> boardService.create(
                        boardCreateCommand,
                        "tester"));
        assertEquals(HttpStatus.CONFLICT, duplicatedBoard.getStatus());

        verify(usersRepository).findByUsername("tester");
        verify(boardRepository).existsByTitle("제목");
        verify(boardRepository, never()).save(any());
    }

    @Test
    @DisplayName("게시글_단건_조회_성공")
    void findOne() {
        Long id = 1L;
        Board entity = new Board(
                1L,
                10L,
                "제목",
                "본문",
                LocalDateTime.now(),
                LocalDateTime.now());
        given(boardRepository.findById(id)).willReturn(Optional.of(entity));

        BoardGetOneCommand boardGetOneCommand = new BoardGetOneCommand(id);

        BoardGetOneDto out = boardService.findOne(boardGetOneCommand);

        assertNotNull(out);
        assertEquals(id, out.getId());
        assertEquals("제목", out.getTitle());
        assertEquals("본문", out.getContent());
        assertNotNull(out.getCreatedDate());

        verify(boardRepository, times(1)).findById(id);
        verifyNoMoreInteractions(boardRepository);
    }

    @Test
    @DisplayName("게시글_단건_조회_미존재_404")
    void findOnNotFound() {
        Long id = 1L;
        given(boardRepository.findById(id)).willReturn(Optional.empty());
        BoardGetOneCommand command = new BoardGetOneCommand(id);
        BoardErrorException findOneNotFound = assertThrows(
                BoardErrorException.class,
                () -> boardService.findOne(command));
        assertEquals(HttpStatus.NOT_FOUND, findOneNotFound.getStatus());
        verify(boardRepository, times(1)).findById(id);
        verifyNoMoreInteractions(boardRepository);
    }

    @Test
    @DisplayName("게시글_전체_조회_첫페이지_커서_없음_hasNext_false")
    void findAll_firstPage_noNextPage() {
        List<Board> boards = List.of(
                new Board(3L, 10L, "title1", "content1", FIXED_TIME, FIXED_TIME),
                new Board(2L, 10L, "title2", "content2", FIXED_TIME, FIXED_TIME),
                new Board(1L, 10L, "title3", "content3", FIXED_TIME, FIXED_TIME));
        given(boardRepository.findFirstPage(11)).willReturn(boards);
        CursorResponse<BoardSummaryKeysetResponse> result = boardService.findAll(null, null, 10, null);

        assertEquals(3, result.getContents().size());
        assertFalse(result.isHasNext());
        assertNull(result.getNextCursorTitle());
        assertNull(result.getNextCursorId());

        verify(boardRepository, times(1)).findFirstPage(11);
        verifyNoMoreInteractions(boardRepository);
    }

    @Test
    @DisplayName("게시글_전체_조회_첫페이지_커서_없음_hasNext_true_커서세팅")
    void findAll_firstPage_hasNextPage() {
        List<Board> boards=new ArrayList<>();
        for(long i = 11; i >= 1; i--){
            boards.add(new Board(i, 10L, "title" + i, "content" + i, FIXED_TIME, FIXED_TIME));
        }
        given(boardRepository.findFirstPage(11)).willReturn(boards);
        CursorResponse<BoardSummaryKeysetResponse> result = boardService.findAll(null, null, 10, null);
        
        assertEquals(10L, result.getContents().size());
        assertTrue(result.isHasNext());
        assertEquals("title2", result.getNextCursorTitle());
        assertEquals(2L, result.getNextCursorId());

        verify(boardRepository, times(1)).findFirstPage(11);
        verifyNoMoreInteractions(boardRepository);
    }

    @Test
    @DisplayName("게시글_전체_조회_다음페이지_커서_있음_hasNext_false")
    void findAll_nextPage_noNextPage() {
        List<Board> boards = List.of(
                new Board(2L, 10L, "title", "content", FIXED_TIME, FIXED_TIME),
                new Board(1L, 10L, "title", "content", FIXED_TIME, FIXED_TIME));
        given(boardRepository.findNextPage("title", 5L, 11)).willReturn(boards);
        CursorResponse<BoardSummaryKeysetResponse> result = boardService.findAll("title", 5L, 10, null);
        assertEquals(2, result.getContents().size());
        assertFalse(result.isHasNext());
        assertNull(result.getNextCursorTitle());
        assertNull(result.getNextCursorId());

        verify(boardRepository, times(1)).findNextPage("title", 5L, 11);
        verifyNoMoreInteractions(boardRepository);
    }

    @Test
    @DisplayName("게시글_전체_조회_다음페이지_커서_있음_hasNext_true_커서세팅")
    void findAll_nextPage_hasNextPage() {
        List<Board> boards = new ArrayList<>();
        for (long i = 11; i >= 1; i--) {
            boards.add(
                    new Board(i, 10L, "title" + i, "content" + i, FIXED_TIME, FIXED_TIME));
        }
        given(boardRepository.findNextPage("title2", 12L, 11)).willReturn(boards);
        CursorResponse<BoardSummaryKeysetResponse> result = boardService.findAll("title2", 12L, 10, null);

        assertEquals(10, result.getContents().size());
        assertTrue(result.isHasNext());
        assertEquals("title2", result.getNextCursorTitle());
        assertEquals(2L, result.getNextCursorId());

        verify(boardRepository).findNextPage("title2", 12L, 11);
        verifyNoMoreInteractions(boardRepository);
    }

    @Test
    @DisplayName("게시글_수정_성공")
    void update() {
        Long id = 1L;
        Users users = mock(Users.class);
        Board board = mock(Board.class);
        BoardUpdateCommand cmd = new BoardUpdateCommand(
                "tester",
                id,
                "새제목",
                "새본문");
        given(usersRepository.findByUsername("tester")).willReturn(Optional.of(users));
        given(boardRepository.findById(id)).willReturn(Optional.of(board));
        given(users.getUserId()).willReturn(10L);
        given(board.getUserId()).willReturn(10L);
        given(board.getId()).willReturn(id);

        BoardUpdatedDto result = boardService.update(cmd);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(board).update("새제목", "새본문");
    }

    @Test
    @DisplayName("게시글_수정_권한없음_403")
    void updateFobiddern() {
        Users users = mock(Users.class);
        Board board = mock(Board.class);

        BoardUpdateCommand cmd = new BoardUpdateCommand(
                "tester",
                1L,
                "title",
                "content");

        given(usersRepository.findByUsername("tester")).willReturn(Optional.of(users));
        given(boardRepository.findById(1L)).willReturn(Optional.of(board));

        given(users.getUserId()).willReturn(10L);
        given(board.getUserId()).willReturn(98L);

        AuthErrorException forbiddern = assertThrows(
                AuthErrorException.class,
                () -> boardService.update(cmd));

        assertEquals(HttpStatus.FORBIDDEN, forbiddern.getStatus());
        verify(board, never()).update(anyString(), anyString());
    }

    @Test
    @DisplayName("게시글_삭제_성공")
    void delete() {

        Users users = mock(Users.class);
        Board board = mock(Board.class);
        String username = "tester";
        Long id = 1L;

        given(usersRepository.findByUsername(username)).willReturn(Optional.of(users));
        given(boardRepository.findById(id)).willReturn(Optional.of(board));

        given(users.getUserId()).willReturn(10L);
        given(board.getUserId()).willReturn(10L);

        boardService.delete(username, id);

        verify(usersRepository, times(1)).findByUsername(username);
        verify(boardRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("게시글_삭제_권한없음_403")
    void deleteFobiddern() {
        Users users = mock(Users.class);
        Board board = mock(Board.class);
        String username = "tester";

        given(usersRepository.findByUsername(username)).willReturn(Optional.of(users));
        given(boardRepository.findById(1L)).willReturn(Optional.of(board));

        given(users.getUserId()).willReturn(10L);
        given(board.getUserId()).willReturn(98L);

        AuthErrorException fodibbern = assertThrows(
                AuthErrorException.class,
                () -> boardService.delete("tester", 1L));
        assertEquals(HttpStatus.FORBIDDEN, fodibbern.getStatus());

        verify(usersRepository, times(1)).findByUsername("tester");
        verify(boardRepository, times(1)).findById(1L);
    }
}