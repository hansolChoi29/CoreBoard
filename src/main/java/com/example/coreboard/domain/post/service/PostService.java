package com.example.coreboard.domain.post.service;

import com.example.coreboard.domain.board.entity.Board;
import com.example.coreboard.domain.board.repository.BoardRepository;
import com.example.coreboard.domain.common.exception.board.BoardErrorCode;
import com.example.coreboard.domain.common.exception.board.BoardErrorException;
import com.example.coreboard.domain.post.dto.command.CreatePostCommand;
import com.example.coreboard.domain.post.dto.command.DeletePostCommand;
import com.example.coreboard.domain.post.dto.command.GetOnePostCommand;
import com.example.coreboard.domain.post.dto.command.UpdatePostCommand;
import com.example.coreboard.domain.post.dto.response.PostSummaryResponse;
import com.example.coreboard.domain.post.dto.result.CreatePostResult;
import com.example.coreboard.domain.post.dto.result.GetOnePostResult;
import com.example.coreboard.domain.post.dto.result.UpdatePostResult;
import com.example.coreboard.domain.post.entity.Post;
import com.example.coreboard.domain.post.repository.PostRepository;
import com.example.coreboard.domain.common.exception.auth.AuthErrorException;
import com.example.coreboard.domain.common.exception.post.PostErrorException;
import com.example.coreboard.domain.common.response.CursorResponse;
import com.example.coreboard.domain.users.entity.Users;
import com.example.coreboard.domain.users.repository.UsersRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static com.example.coreboard.domain.common.exception.auth.AuthErrorCode.*;
import static com.example.coreboard.domain.common.exception.post.PostErrorCode.*;

import java.util.List;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final BoardRepository boardRepository;
    private final UsersRepository usersRepository;

    public PostService(
            PostRepository postRepository,
            BoardRepository boardRepository,
            UsersRepository usersRepository
    ) {
        this.postRepository = postRepository;
        this.boardRepository = boardRepository;
        this.usersRepository = usersRepository;
    }

    @Transactional
    public CreatePostResult create(
            CreatePostCommand commnad,
            String username
    ) {
        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new AuthErrorException(NOT_FOUND));
        if (postRepository.existsByTitle(commnad.title())) {
            throw new PostErrorException(TITLE_DUPLICATED);
        }
        Board board = boardRepository.findById(commnad.boardId())
                .orElseThrow(() -> new BoardErrorException(BoardErrorCode.BOARD_NOT_FOUND));
        Post post = Post.create(
                board,
                user,
                commnad.title(),
                commnad.content());
        Post saved = postRepository.save(post);

        return new CreatePostResult(saved.getId());
    }

    @Transactional(readOnly = true)
    public GetOnePostResult getOne(GetOnePostCommand command) {
        Post post = postRepository.findById(command.id())
                .orElseThrow(() -> new PostErrorException(POST_NOT_FOUND));

        return new GetOnePostResult(
                post.getId(),
                post.getUser().getUserId(),
                post.getTitle(),
                post.getContent(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    @Transactional(readOnly = true)
    public CursorResponse<PostSummaryResponse> getAll(
            String cursorTitle,
            Long cursorId,
            int size,
            String sort
    ) {
        Pageable pageable = PageRequest.of(0, size + 1);
        boolean isDesc = "desc".equalsIgnoreCase(sort);
        List<Post> result = (cursorTitle == null || cursorId == null)
                ? (isDesc ? postRepository.findFirstPageDesc(pageable)
                : postRepository.findFirstPageAsc(pageable))
                : (isDesc ? postRepository.findNextPageDesc(cursorTitle, cursorId, pageable)
                : postRepository.findNextPageAsc(cursorTitle, cursorId, pageable));
        boolean hasNext = result.size() > size;
        if (hasNext) {
            result = result.subList(0, size);
        }
        List<PostSummaryResponse> contents = result.stream()
                .map(b -> new PostSummaryResponse(
                        b.getId(),
                        b.getUser().getNickname(),
                        b.getTitle(),
                        b.getCreatedAt(),
                        b.getUpdatedAt()))
                .toList();
        String nextCursorTitle = hasNext ? result.get(result.size() - 1).getTitle() : null;
        Long nextCursorId = hasNext ? result.get(result.size() - 1).getId() : null;

        return new CursorResponse<>(
                contents,
                nextCursorTitle,
                nextCursorId,
                hasNext
        );
    }

    @Transactional
    public UpdatePostResult update(UpdatePostCommand command) {
        Users user = usersRepository.findByUsername(command.username())
                .orElseThrow(() -> new AuthErrorException(NOT_FOUND));

        Post post = postRepository.findById(command.id())
                .orElseThrow(() -> new PostErrorException(POST_NOT_FOUND));

        if (!post.getUser().getUserId().equals(user.getUserId())) {
            throw new AuthErrorException(FORBIDDEN);
        }
        post.update(
                command.title(),
                command.content(),
                command.contentFormat());

        return new UpdatePostResult(
                post.getId(),
                post.getCreatedAt(),
                post.getUpdatedAt());
    }

    @Transactional
    public void delete(DeletePostCommand command) {
        Users user = usersRepository.findByUsername(command.username())
                .orElseThrow(() -> new AuthErrorException(NOT_FOUND));
        Post post = postRepository.findById(command.id())
                .orElseThrow(() -> new PostErrorException(POST_NOT_FOUND));

        if (!post.getUser().getUserId().equals(user.getUserId())) {
            throw new AuthErrorException(FORBIDDEN);
        }
        post.delete();
    }

    @Transactional
    public CursorResponse<PostSummaryResponse> search(String keyword) {
        List<Post> posts = postRepository.searchByKeyword(keyword);
        List<PostSummaryResponse> contents = posts.stream().map(
                post -> new PostSummaryResponse(
                        post.getId(),
                        post.getUser().getNickname(),
                        post.getTitle(),
                        post.getCreatedAt(),
                        post.getUpdatedAt()
                )).toList();

        return new CursorResponse<>(
                contents,
                null,
                null,
                false
        );
    }
}