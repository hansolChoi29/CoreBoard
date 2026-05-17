package com.example.coreboard.domain.integration;

import com.example.coreboard.domain.board.entity.Board;
import com.example.coreboard.domain.board.repository.BoardRepository;
import com.example.coreboard.domain.comment.dto.request.CommentRequest;
import com.example.coreboard.domain.common.type.ContentFormat;
import com.example.coreboard.domain.common.util.JwtUtil;
import com.example.coreboard.domain.post.entity.Post;
import com.example.coreboard.domain.post.repository.PostRepository;
import com.example.coreboard.domain.users.entity.UserRole;
import com.example.coreboard.domain.users.entity.Users;
import com.example.coreboard.domain.users.repository.UsersRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static com.example.coreboard.domain.support.fixture.BoardFixture.freeBoard;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CommentIntegrationTest extends IntegrationTestBase {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    PostRepository postRepository;

    @Autowired
    BoardRepository boardRepository;

    @Autowired
    UsersRepository usersRepository;

    @Test
    @DisplayName("댓글_생성_후_목록조회_성공")
    void createAndGetAllComments() throws Exception {
        Users user = usersRepository.save(createUser("username", "nickname"));
        String accessToken = JwtUtil.createAccessToken(
                user.getUserId(),
                user.getUsername(),
                user.getRole()
        );

        Board board = boardRepository.save(freeBoard());
        Post post = postRepository.save(createPost(board, user));

        CommentRequest request = new CommentRequest("comment content");

        mockMvc.perform(
                        post("/posts/{postId}/comments", post.getId())
                                .header("Authorization", "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data.id").exists());

        mockMvc.perform(
                        get("/posts/{postId}/comments", post.getId())
                                .param("page", "0")
                                .param("size", "10")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].content").value("comment content"))
                .andExpect(jsonPath("$.data.content[0].nickname").value("nickname"));
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