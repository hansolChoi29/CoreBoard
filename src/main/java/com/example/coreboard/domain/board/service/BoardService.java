package com.example.coreboard.domain.board.service;

import com.example.coreboard.domain.board.dto.CreateBoardDto;
import com.example.coreboard.domain.board.dto.command.CreateBoardCommand;
import com.example.coreboard.domain.board.entity.Board;
import com.example.coreboard.domain.board.repository.BoardRepository;
import com.example.coreboard.domain.common.exception.auth.AuthErrorCode;
import com.example.coreboard.domain.common.exception.auth.AuthErrorException;
import com.example.coreboard.domain.common.exception.board.BoardErrorCode;
import com.example.coreboard.domain.common.exception.board.BoardErrorException;
import com.example.coreboard.domain.users.entity.Users;
import com.example.coreboard.domain.users.repository.UsersRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public CreateBoardDto createBoard(CreateBoardCommand command, String username) {
        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new AuthErrorException(AuthErrorCode.NOT_FOUND));
        // slug는 주소라서 중복이면 절대 안 된다
        if (boardRepository.existsBySlug(command.slug())) {
            throw new BoardErrorException(BoardErrorCode.BOARD_SLUG_DUPLICATE);
        }
        if (boardRepository.existsByName(command.name())) {
            throw new BoardErrorException(BoardErrorCode.BOARD_NAME_DUPLICATE);
        }
        Board board = Board.create(
                command.name(),
                command.slug(),
                command.commentEnabled(),
                command.answerAcceptedEnabled(),
                command.requireAttachment(),
                command.maxAttachmentCount(),
                command.maxContentLength()
        );
        boardRepository.save(board);

        return new CreateBoardDto(board.getId());
    }
}
