package com.example.coreboard.domain.integration;


import com.example.coreboard.domain.board.entity.Board;
import com.example.coreboard.domain.board.repository.BoardRepository;
import com.example.coreboard.domain.comment.entity.Comment;
import com.example.coreboard.domain.comment.entity.CommentStatus;
import com.example.coreboard.domain.comment.repository.CommentRepository;
import com.example.coreboard.domain.post.entity.ContentFormat;
import com.example.coreboard.domain.post.entity.Post;
import com.example.coreboard.domain.post.repository.PostRepository;
import com.example.coreboard.domain.users.entity.UserRole;
import com.example.coreboard.domain.users.entity.Users;
import com.example.coreboard.domain.users.repository.UsersRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class CommentTest extends IntegrationTestBase{
    @Autowired
    CommentRepository commentRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    BoardRepository boardRepository;

    @Autowired
    UsersRepository usersRepository;

    @Test
    @DisplayName("댓글_목록_조회시_작성자를_함께_조회한다")
    void getAll_with_fetch_join() {
        Users user1 = usersRepository.save(createUser("username1", "nickname1"));
        Users user2 = usersRepository.save(createUser("username2", "nickname2"));
        Users user3 = usersRepository.save(createUser("username3", "nickname3"));

        Board board = boardRepository.save(createBoard());
        Post post = postRepository.save(createPost(board, user1));

        commentRepository.save(Comment.create(post, user1, "comment1"));
        commentRepository.save(Comment.create(post, user2, "comment2"));
        commentRepository.save(Comment.create(post, user3, "comment3"));

        Pageable pageable = PageRequest.of(
                0,
                10,
                Sort.by(
                        Sort.Order.desc("createdDate"),
                        Sort.Order.desc("id")
                )
        );

        Slice<Comment> comments = commentRepository.findByPostIdAndStatusWithUser(
                post.getId(),
                CommentStatus.ACTIVE,
                pageable
        );

        assertThat(comments.getContent()).hasSize(3);

        comments.getContent().forEach(comment ->
                assertThat(comment.getUser().getNickname()).isNotBlank()
        );
    }

    private Users createUser(String username, String nickname) {
        return new Users(
                username,
                nickname,
                "password",
                username + "@test.com",
                "01012341234",
                UserRole.USER
        );
    }

    private Board createBoard() {
        return Board.create(
                "자유게시판",
                "free",
                true,
                false,
                false,
                0,
                UserRole.USER
        );
    }

    private Post createPost(Board board, Users user) {
        return Post.create(
                board,
                user,
                "title",
                "post content",
                ContentFormat.MARKDOWN
        );
    }
}
