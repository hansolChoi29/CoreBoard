package com.example.coreboard.domain.board.service;

import com.example.coreboard.domain.board.dto.CreateBoardDto;
import com.example.coreboard.domain.board.dto.GetOneBoardDto;
import com.example.coreboard.domain.board.dto.command.CreateBoardCommand;
import com.example.coreboard.domain.board.dto.command.GetOneBoardCommand;
import com.example.coreboard.domain.board.entity.Board;
import com.example.coreboard.domain.board.repository.BoardRepository;
import com.example.coreboard.domain.post.entity.ContentFormat;
import com.example.coreboard.domain.post.entity.Post;
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

import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class BoardServiceTest {
    @Mock
    BoardRepository boardRepository;

    @Mock
    PostRepository postRepository;

    @Mock
    UsersRepository UsersRepository;

    @InjectMocks
    BoardService boardService;

    @Test
    @DisplayName("게시판_생성_성공")
    void createBoard() {
        String username = "username";
        Users user = new Users(username, "nickname", "password", "qwe@qwe.com", "01012341234", UserRole.USER);
        given(UsersRepository.findByUsername(username)).willReturn(Optional.of(user));
        CreateBoardCommand command = new CreateBoardCommand(
                "자유게시판",
                "free",
                false,
                false,
                false,
                0,
                10000,
                UserRole.USER
        );
        // save 저장 후 둘려줌
        Board savedBoard = new Board(
                "자유게시판",
                "free",
                false,
                false,
                false,
                0,
                10000,
                UserRole.USER
        );
        given(boardRepository.existsByName("자유게시판")).willReturn(false);
        given(boardRepository.existsBySlug("free")).willReturn(false);
        given(boardRepository.save(any(Board.class))).willReturn(savedBoard);

        CreateBoardDto result = boardService.create(command, username);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("자유게시판");
        verify(boardRepository).existsByName("자유게시판");
        verify(boardRepository).existsBySlug("free");
        verify(boardRepository).save(any(Board.class));
    }

    @Test
    @DisplayName("")
    void getOneBoard() {
        Board board = new Board(
                "자유게시판",
                "free",
                false,
                false,
                false,
                0,
                10000,
                UserRole.USER);
        Users user = new Users(
                "username",
                "nickname",
                "password",
                "qwe@qwe.com",
                "01012341234",
                UserRole.USER);
        Post post = new Post(
                board, user,
                "title",
                "content",
                ContentFormat.MARKDOWN);
        GetOneBoardCommand command = new GetOneBoardCommand(1L);

        given(boardRepository.findById(1L)).willReturn(Optional.of(board));
        given(postRepository.findByBoardId(command.id())).willReturn(List.of(post));

        GetOneBoardDto result = boardService.getOne(command);

        assertThat(result).isNotNull();
        assertThat(command.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("자유게시판");
        assertThat(result.slug()).isEqualTo("free");
        assertThat(result.answerAcceptedEnabled()).isEqualTo(false);
        assertThat(result.commentEnabled()).isEqualTo(false);
        assertThat(result.requireAttachment()).isEqualTo(false);
        assertThat(result.maxAttachmentCount()).isEqualTo(0);
        assertThat(result.maxContentLength()).isEqualTo(10000);
        assertThat(result.requiredWriteRole()).isEqualTo(UserRole.USER);
        assertThat(result.posts().get(0).title()).isEqualTo("title");

        verify(boardRepository).findById(command.id());
        verify(postRepository).findByBoardId(1L);
    }
}