package com.example.coreboard.domain.board.service;

import com.example.coreboard.domain.board.dto.CreateBoardDto;
import com.example.coreboard.domain.board.dto.command.CreateBoardCommand;
import com.example.coreboard.domain.board.entity.Board;
import com.example.coreboard.domain.board.repository.BoardRepository;
import com.example.coreboard.domain.users.entity.UserRole;
import com.example.coreboard.domain.users.entity.Users;
import com.example.coreboard.domain.users.repository.UsersRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class BoardServiceTest {
    @Mock
    BoardRepository boardRepository;

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

        CreateBoardDto result = boardService.createBoard(command, username);

        assertThat(result).isNotNull();

        verify(boardRepository).existsByName("자유게시판");
        verify(boardRepository).existsBySlug("free");
        verify(boardRepository).save(any(Board.class));
    }
}