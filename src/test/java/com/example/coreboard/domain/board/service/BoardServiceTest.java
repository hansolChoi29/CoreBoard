package com.example.coreboard.domain.board.service;

import com.example.coreboard.domain.board.dto.command.DeleteBoardCommand;
import com.example.coreboard.domain.board.dto.command.UpdateBoardCommand;
import com.example.coreboard.domain.board.dto.query.GetBoardListQuery;
import com.example.coreboard.domain.board.dto.response.GetBoardListResponse;
import com.example.coreboard.domain.board.dto.result.CreateBoardResult;
import com.example.coreboard.domain.board.dto.result.GetOneBoardResult;
import com.example.coreboard.domain.board.dto.command.CreateBoardCommand;
import com.example.coreboard.domain.board.dto.command.GetOneBoardCommand;
import com.example.coreboard.domain.board.dto.result.UpdateBoardResult;
import com.example.coreboard.domain.board.entity.Board;
import com.example.coreboard.domain.board.repository.BoardRepository;
import com.example.coreboard.domain.common.exception.auth.AuthErrorException;
import com.example.coreboard.domain.common.exception.board.BoardErrorException;
import com.example.coreboard.domain.common.response.OffsetPageResponse;
import com.example.coreboard.domain.post.entity.ContentFormat;
import com.example.coreboard.domain.post.entity.Post;
import com.example.coreboard.domain.post.entity.PostStatus;
import com.example.coreboard.domain.post.repository.PostRepository;
import com.example.coreboard.domain.users.entity.UserRole;
import com.example.coreboard.domain.users.entity.Users;
import com.example.coreboard.domain.users.repository.UsersRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class BoardServiceTest {
    @Mock
    BoardRepository boardRepository;

    @Mock
    PostRepository postRepository;

    @Mock
    UsersRepository usersRepository;

    @InjectMocks
    BoardService boardService;

    @Test
    @DisplayName("게시판_생성_성공")
    void createBoard() {
        String username = "username";
        Users user = new Users(
                username,
                "nickname",
                "password",
                "qwe@qwe.com",
                "01012341234",
                UserRole.ADMIN
        );
        given(usersRepository.findByUsername(username)).willReturn(Optional.of(user));
        CreateBoardCommand command = new CreateBoardCommand(
                "자유게시판",
                "free",
                false,
                false,
                false,
                0,
                UserRole.USER
        );
        Board savedBoard = new Board(
                "자유게시판",
                "free",
                false,
                false,
                false,
                0,
                UserRole.USER
        );
        given(boardRepository.existsByNameAndDeletedAtIsNull("자유게시판")).willReturn(false);
        given(boardRepository.existsBySlugAndDeletedAtIsNull("free")).willReturn(false);
        given(boardRepository.save(any(Board.class))).willReturn(savedBoard);

        CreateBoardResult result = boardService.create(command, username);

        assertThat(result).isNotNull();
        verify(boardRepository).existsByNameAndDeletedAtIsNull("자유게시판");
        verify(boardRepository).existsBySlugAndDeletedAtIsNull("free");
        verify(boardRepository).save(any(Board.class));
    }

    @Test
    @DisplayName("게시판_생성_ADMIN_아님_403")
    void createForbidden() {
        String username = "username";
        Users user = new Users(
                username,
                "nickname",
                "password",
                "qwe@qwe.com",
                "01012341234",
                UserRole.USER
        );
        CreateBoardCommand command = new CreateBoardCommand(
                "자유게시판",
                "free",
                false,
                false,
                false,
                0,
                UserRole.USER
        );

        given(usersRepository.findByUsername(username)).willReturn(Optional.of(user));

        AuthErrorException exception = assertThrows(
                AuthErrorException.class,
                () -> boardService.create(command, username)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());

        verify(usersRepository).findByUsername(username);
        verify(boardRepository, never()).existsByNameAndDeletedAtIsNull(anyString());
        verify(boardRepository, never()).existsBySlugAndDeletedAtIsNull(anyString());
        verify(boardRepository, never()).save(any(Board.class));
    }

    @Test
    @DisplayName("게시판생성_이미_사용중인_게시판이름")
    void create_name_conflict() {
        String username = "username";
        Users user = new Users(
                username,
                "nickname",
                "password",
                "qwe@qwe.com",
                "01012341234",
                UserRole.ADMIN
        );
        CreateBoardCommand command = new CreateBoardCommand(
                "자유",
                "free",
                false,
                false,
                false,
                10000,
                UserRole.USER
        );

        given(usersRepository.findByUsername(username)).willReturn(Optional.of(user));
        given(boardRepository.existsByNameAndDeletedAtIsNull(command.name()))
                .willReturn(true);

        BoardErrorException exception = assertThrows(
                BoardErrorException.class,
                () -> boardService.create(command, username)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(usersRepository).findByUsername(username);
        verify(boardRepository).existsByNameAndDeletedAtIsNull(command.name());
        verify(boardRepository, never()).existsBySlugAndDeletedAtIsNull(anyString());
        verify(boardRepository, never()).save(any(Board.class));
    }

    @Test
    @DisplayName("게시판생성_이미_사용중인_slug")
    void create_slug_conflict() {
        String username = "username";
        Users user = new Users(
                username,
                "nickname",
                "password",
                "qwe@qwe.com",
                "01012341234",
                UserRole.ADMIN
        );
        CreateBoardCommand command = new CreateBoardCommand(
                "자유",
                "free",
                false,
                false,
                false,
                10000,
                UserRole.USER
        );

        given(usersRepository.findByUsername(username)).willReturn(Optional.of(user));
        given(boardRepository.existsByNameAndDeletedAtIsNull(command.name()))
                .willReturn(false);
        given(boardRepository.existsBySlugAndDeletedAtIsNull(command.slug()))
                .willReturn(true);

        BoardErrorException exception = assertThrows(
                BoardErrorException.class,
                () -> boardService.create(command, username)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(usersRepository).findByUsername(username);
        verify(boardRepository).existsByNameAndDeletedAtIsNull(command.name());
        verify(boardRepository).existsBySlugAndDeletedAtIsNull(command.slug());
        verify(boardRepository, never()).save(any(Board.class));
    }

    @Test
    @DisplayName("게시판_단건_조회_성공")
    void getOneBoard() {
        Board board = new Board(
                "자유게시판",
                "free",
                false,
                false,
                false,
                0,
                UserRole.USER
        );
        Users user = new Users(
                "username",
                "nickname",
                "password",
                "qwe@qwe.com",
                "01012341234",
                UserRole.ADMIN
        );
        Post post = new Post(
                board,
                user,
                "title",
                "content",
                ContentFormat.MARKDOWN
        );
        GetOneBoardCommand command = new GetOneBoardCommand(1L);

        given(boardRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(board));
        given(postRepository.findAllByBoardIdWithUser(1L, PostStatus.PUBLISHED)).willReturn(List.of(post));

        GetOneBoardResult result = boardService.getOne(command);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("자유게시판");
        assertThat(result.slug()).isEqualTo("free");
        assertThat(result.answerAcceptedEnabled()).isFalse();
        assertThat(result.commentEnabled()).isFalse();
        assertThat(result.requireAttachment()).isFalse();
        assertThat(result.maxAttachmentCount()).isEqualTo(0);
        assertThat(result.allowedWriteRoles()).isEqualTo(UserRole.USER);

        assertThat(result.posts()).hasSize(1);
        assertThat(result.posts().get(0).writerName()).isEqualTo("nickname");
        assertThat(result.posts().get(0).title()).isEqualTo("title");

        verify(boardRepository).findByIdAndDeletedAtIsNull(command.id());
        verify(postRepository).findAllByBoardIdWithUser(command.id(), PostStatus.PUBLISHED);
        verifyNoMoreInteractions(boardRepository, postRepository);
    }

    @Test
    @DisplayName("게시판_전체조회_성공")
    void getAllBoard() {
        GetBoardListQuery query = new GetBoardListQuery(
                0,
                20,
                Sort.Direction.DESC
        );
        Board board = new Board(
                "자유게시판",
                "free",
                false,
                false,
                false,
                0,
                UserRole.USER
        );
        PageRequest pageRequest = PageRequest.of(
                0,
                20,
                Sort.by(Sort.Direction.DESC, "id")
        );
        List<Board> boards = List.of(board);
        Page<Board> boardPage = new PageImpl<>(
                boards,
                pageRequest,
                boards.size()
        );
        given(boardRepository.findByDeletedAtIsNull(pageRequest)).willReturn(boardPage);

        OffsetPageResponse<GetBoardListResponse> response = boardService.getAll(query);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).name()).isEqualTo("자유게시판");
        assertThat(response.getContent().get(0).slug()).isEqualTo("free");

        assertThat(response.getPageInfo().getPage()).isEqualTo(0);
        assertThat(response.getPageInfo().getSize()).isEqualTo(20);
        assertThat(response.getPageInfo().getTotalElements()).isEqualTo(1L);
        assertThat(response.getPageInfo().getTotalPages()).isEqualTo(1);

        verify(boardRepository).findByDeletedAtIsNull(pageRequest);
        verifyNoMoreInteractions(boardRepository);
    }

    @Test
    @DisplayName("게시판_수정_성공")
    void updateBoard() {
        String username = "username";
        Long id = 1L;
        Users user = new Users(
                username,
                "nickname",
                "password",
                "qwe@qwe.com",
                "01012341234",
                UserRole.ADMIN
        );
        Board board = new Board(
                "기존게시판",
                "old-free",
                false,
                false,
                false,
                0,
                UserRole.USER
        );
        UpdateBoardCommand command = new UpdateBoardCommand(
                id,
                "자유게시판",
                "free",
                false,
                false,
                false,
                0
        );
        given(usersRepository.findByUsername(username)).willReturn(Optional.of(user));
        given(boardRepository.findByIdAndDeletedAtIsNull(id)).willReturn(Optional.of(board));
        given(boardRepository.existsByNameAndIdNotAndDeletedAtIsNull("자유게시판", id)).willReturn(false);
        given(boardRepository.existsBySlugAndIdNotAndDeletedAtIsNull("free", id)).willReturn(false);

        UpdateBoardResult result = boardService.update(command, username, id);

        assertThat(result).isNotNull();

        assertThat(board.getName()).isEqualTo("자유게시판");
        assertThat(board.getSlug()).isEqualTo("free");
        assertThat(board.isAnswerAcceptedEnabled()).isFalse();
        assertThat(board.isCommentEnabled()).isFalse();
        assertThat(board.isRequireAttachment()).isFalse();
        assertThat(board.getMaxAttachmentCount()).isEqualTo(0);

        verify(usersRepository).findByUsername(username);
        verify(boardRepository).existsByNameAndIdNotAndDeletedAtIsNull(command.name(), id);
        verify(boardRepository).existsBySlugAndIdNotAndDeletedAtIsNull(command.slug(), id);
        verifyNoMoreInteractions(usersRepository, boardRepository);
    }

    @Test
    @DisplayName("게시판_수정_이미_사용중인_게시판이름")
    void update_name_conflict() {
        String username = "username";
        Long id = 1L;

        Users user = new Users(
                username,
                "nickname",
                "password",
                "qwe@qwe.com",
                "01012341234",
                UserRole.ADMIN
        );

        Board board = new Board(
                "기존게시판",
                "old-free",
                false,
                false,
                false,
                0,
                UserRole.USER
        );

        UpdateBoardCommand command = new UpdateBoardCommand(
                id,
                "자유게시판",
                "free",
                false,
                false,
                false,
                0
        );

        given(usersRepository.findByUsername(username)).willReturn(Optional.of(user));
        given(boardRepository.findByIdAndDeletedAtIsNull(id)).willReturn(Optional.of(board));
        given(boardRepository.existsByNameAndIdNotAndDeletedAtIsNull(command.name(), id))
                .willReturn(true);

        BoardErrorException exception = assertThrows(
                BoardErrorException.class,
                () -> boardService.update(command, username, id)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(usersRepository).findByUsername(username);
        verify(boardRepository).findByIdAndDeletedAtIsNull(id);
        verify(boardRepository).existsByNameAndIdNotAndDeletedAtIsNull(command.name(), id);
        verify(boardRepository, never()).existsBySlugAndIdNotAndDeletedAtIsNull(anyString(), anyLong());
    }

    @Test
    @DisplayName("게시판_수정_이미_사용중인_slug")
    void update_slug_conflict() {
        String username = "username";
        Long id = 1L;

        Users user = new Users(
                username,
                "nickname",
                "password",
                "qwe@qwe.com",
                "01012341234",
                UserRole.ADMIN
        );

        Board board = new Board(
                "기존게시판",
                "old-free",
                false,
                false,
                false,
                0,
                UserRole.USER
        );

        UpdateBoardCommand command = new UpdateBoardCommand(
                id,
                "자유게시판",
                "free",
                false,
                false,
                false,
                0
        );

        given(usersRepository.findByUsername(username)).willReturn(Optional.of(user));
        given(boardRepository.findByIdAndDeletedAtIsNull(id)).willReturn(Optional.of(board));
        given(boardRepository.existsByNameAndIdNotAndDeletedAtIsNull(command.name(), id))
                .willReturn(false);
        given(boardRepository.existsBySlugAndIdNotAndDeletedAtIsNull(command.slug(), id))
                .willReturn(true);

        BoardErrorException exception = assertThrows(
                BoardErrorException.class,
                () -> boardService.update(command, username, id)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(usersRepository).findByUsername(username);
        verify(boardRepository).findByIdAndDeletedAtIsNull(id);
        verify(boardRepository).existsByNameAndIdNotAndDeletedAtIsNull(command.name(), id);
        verify(boardRepository).existsBySlugAndIdNotAndDeletedAtIsNull(command.slug(), id);
    }

    @Test
    @DisplayName("게시판_수정_ADMIN_아님_403")
    void updateForbidden() {
        String username = "username";
        Long id = 1L;

        Users user = new Users(
                username,
                "nickname",
                "password",
                "qwe@qwe.com",
                "01012341234",
                UserRole.USER
        );

        Board board = new Board(
                "기존게시판",
                "old-free",
                false,
                false,
                false,
                0,
                UserRole.USER
        );

        UpdateBoardCommand command = new UpdateBoardCommand(
                id,
                "자유게시판",
                "free",
                false,
                false,
                false,
                0
        );

        given(usersRepository.findByUsername(username)).willReturn(Optional.of(user));
        given(boardRepository.findByIdAndDeletedAtIsNull(id)).willReturn(Optional.of(board));

        AuthErrorException exception = assertThrows(
                AuthErrorException.class,
                () -> boardService.update(command, username, id)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());

        verify(usersRepository).findByUsername(username);
        verify(boardRepository).findByIdAndDeletedAtIsNull(id);
        verify(boardRepository, never()).existsByNameAndIdNotAndDeletedAtIsNull(anyString(), anyLong());
        verify(boardRepository, never()).existsBySlugAndIdNotAndDeletedAtIsNull(anyString(), anyLong());
    }

    @Test
    @DisplayName("게시판_삭제_성공")
    void deleteBoard() {
        String username = "admin";
        Long boardId = 1L;
        Users admin = mock(Users.class);
        Board board = Board.create(
                "자유게시판",
                "free",
                true,
                false,
                false,
                3,
                UserRole.USER
        );
        DeleteBoardCommand command = new DeleteBoardCommand(boardId, username);

        given(usersRepository.findByUsername(username)).willReturn(Optional.of(admin));
        given(admin.getRole()).willReturn(UserRole.ADMIN);

        given(boardRepository.findByIdAndDeletedAtIsNull(boardId)).willReturn(Optional.of(board));

        given(postRepository.existsByBoardId(boardId)).willReturn(false);

        boardService.delete(command);

        assertThat(board.getDeletedAt()).isNotNull();

        verify(usersRepository).findByUsername(username);
        verify(boardRepository).findByIdAndDeletedAtIsNull(boardId);
        verify(postRepository).existsByBoardId(boardId);
        verify(boardRepository, never()).delete(any(Board.class));
        verifyNoMoreInteractions(usersRepository, boardRepository, postRepository);
    }

    @Test
    @DisplayName("게시판_삭제_ADMIN_아님_403")
    void deleteForbidden() {
        String username = "username";
        Long boardId = 1L;

        Users user = new Users(
                username,
                "nickname",
                "password",
                "qwe@qwe.com",
                "01012341234",
                UserRole.USER
        );

        DeleteBoardCommand command = new DeleteBoardCommand(boardId, username);

        given(usersRepository.findByUsername(username)).willReturn(Optional.of(user));

        AuthErrorException exception = assertThrows(
                AuthErrorException.class,
                () -> boardService.delete(command)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());

        verify(usersRepository).findByUsername(username);
        verifyNoInteractions(boardRepository, postRepository);
    }
}