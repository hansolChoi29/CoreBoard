package com.example.coreboard.domain.board.service;

import com.example.coreboard.domain.board.dto.*;
import com.example.coreboard.domain.board.dto.command.BoardCreateCommand;
import com.example.coreboard.domain.board.dto.command.BoardGetOneCommand;
import com.example.coreboard.domain.board.dto.command.BoardUpdateCommand;
import com.example.coreboard.domain.board.dto.request.BoardCreateRequest;
import com.example.coreboard.domain.board.dto.response.BoardSummaryResponse;
import com.example.coreboard.domain.board.entity.Board;
import com.example.coreboard.domain.board.repository.BoardRepository;
import com.example.coreboard.domain.common.exception.auth.AuthErrorException;
import com.example.coreboard.domain.common.exception.board.BoardErrorException;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
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
        assertEquals(404, notFoundUser.getStatus());

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
        assertEquals(409, duplicatedBoard.getStatus());

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
        assertEquals(404, findOneNotFound.getStatus());
        verify(boardRepository, times(1)).findById(id);
        verifyNoMoreInteractions(boardRepository);
    }

    @Test
    @DisplayName("게시글_전체조회_성공")
    void findAll() {
        int page = 0;
        int size = 10;
        String sort = "asc";

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "title"));

        List<Board> boards = List.of(
                new Board(1L, 10L, "제목1", "내용1", LocalDateTime.now(), LocalDateTime.now()),
                new Board(2L, 20L, "제목2", "내용2", LocalDateTime.now(), LocalDateTime.now()),
                new Board(3L, 10L, "제목3", "내용3", LocalDateTime.now(), LocalDateTime.now()));

        Page<Board> pageResult = new PageImpl<>(boards, pageable, boards.size());
        given(boardRepository.findAll(any(Pageable.class))).willReturn(pageResult);

        PageResponse<BoardSummaryResponse> result = boardService.findAll(page, size, sort);

        assertEquals(page, result.getPage());
        assertEquals(size, result.getSize());
        assertEquals(boards.size(), result.getTotalElements());
        assertEquals(boards.size(), result.getContent().size());
        Board firstEntity = boards.get(0);
        BoardSummaryResponse firstDto = result.getContent().get(0);
        assertEquals(firstEntity.getId(), firstDto.id());
        assertEquals(firstEntity.getUserId(), firstDto.userId());
        assertEquals(firstEntity.getTitle(), firstDto.title());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(boardRepository, times(1)).findAll(captor.capture());
        Pageable used = captor.getValue();
        assertEquals(page, used.getPageNumber());
        assertEquals(size, used.getPageSize());
        assertEquals(Sort.by(Sort.Direction.ASC, "title"), used.getSort());

        verifyNoMoreInteractions(boardRepository);
    }

    @Test
    @DisplayName("게시글_전체_조회_desc_성공")
    void findAllDesc() {
        int page = 0;
        int size = 10;
        String sort = "desc";

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "title"));

        List<Board> boards = List.of(
                new Board(1L, 10L, "제목1", "내용1", LocalDateTime.now(), LocalDateTime.now()),
                new Board(2L, 20L, "제목2", "내용2", LocalDateTime.now(), LocalDateTime.now()),
                new Board(3L, 30L, "제목3", "내용3", LocalDateTime.now(), LocalDateTime.now()));

        Page<Board> pageResult = new PageImpl<>(boards, pageable, boards.size());
        given(boardRepository.findAll(any(Pageable.class))).willReturn(pageResult);

        PageResponse<BoardSummaryResponse> result = boardService.findAll(page, size, sort);

        assertEquals(page, result.getPage());
        assertEquals(size, result.getSize());
        assertEquals(boards.size(), result.getTotalElements());
        assertEquals(boards.size(), result.getContent().size());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(boardRepository, times(1)).findAll(captor.capture());
        Pageable used = captor.getValue();

        assertEquals(page, used.getPageNumber());
        assertEquals(size, used.getPageSize());
        assertEquals(Sort.by(Sort.Direction.DESC, "title"), used.getSort());

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

        assertEquals(403, forbiddern.getStatus());

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
        assertEquals(403, fodibbern.getStatus());

        verify(usersRepository, times(1)).findByUsername("tester");
        verify(boardRepository, times(1)).findById(1L);
    }

}