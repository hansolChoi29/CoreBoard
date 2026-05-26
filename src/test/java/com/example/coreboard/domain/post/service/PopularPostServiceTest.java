package com.example.coreboard.domain.post.service;

import com.example.coreboard.domain.board.entity.Board;
import com.example.coreboard.domain.post.dto.response.PostSummaryResponse;
import com.example.coreboard.domain.post.entity.Post;
import com.example.coreboard.domain.post.entity.PostStatus;
import com.example.coreboard.domain.post.repository.PostRepository;
import com.example.coreboard.domain.users.entity.UserRole;
import com.example.coreboard.domain.users.entity.Users;
import com.example.coreboard.global.type.ContentFormat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.example.coreboard.domain.support.fixture.BoardFixture.freeBoard;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class PopularPostServiceTest {
    @Mock
    StringRedisTemplate template;

    @Mock
    PostRepository postRepository;

    @Mock
    ZSetOperations<String, String> zSetOperations;

    @InjectMocks
    PopularPostService popularPostService;

    @ParameterizedTest
    @ValueSource(strings = {
            "NULL",
            "EMPTY"
    })
    @DisplayName("redis_인기글목록_null_또는_비어있으면_빈_목록_반환")
    void getPopularPostsEmpty(String redisCase) {
        given(template.opsForZSet()).willReturn(zSetOperations);

        if ("NULL".equals(redisCase)) {
            given(zSetOperations.reverseRange("post:popular:all", 0, 9)).willReturn(null);
        }

        if ("EMPTY".equals(redisCase)) {
            given(zSetOperations.reverseRange("post:popular:all", 0, 9)).willReturn(Set.of());
        }

        List<PostSummaryResponse> result = popularPostService.getPopularPosts(10);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(template).opsForZSet();
        verify(zSetOperations).reverseRange("post:popular:all", 0, 9);
        verifyNoInteractions(postRepository);
    }

    @Test
    @DisplayName("redis_인기순서대로_게시글_목록_반환")
    void getPopularPosts() {
        Board board = freeBoard();
        Users user = new Users(
                "username",
                "nickname",
                "password",
                "qwe@qwe.com",
                "01021341234",
                UserRole.USER
        );

        ReflectionTestUtils.setField(user, "userId", 2L);

        Post post2 = new Post(
                board,
                user,
                "title2",
                "content2",
                ContentFormat.MARKDOWN
        );

        ReflectionTestUtils.setField(post2, "id", 2L);

        Post post3 = new Post(
                board,
                user,
                "title3",
                "content3",
                ContentFormat.MARKDOWN
        );
        ReflectionTestUtils.setField(post3, "id", 3L);

        Set<String> members = new LinkedHashSet<>();
        members.add("post:3");
        members.add("post:2");

        given(template.opsForZSet()).willReturn(zSetOperations);
        given(zSetOperations.reverseRange("post:popular:all", 0, 9)).willReturn(members);
        given(postRepository.findAllByIdInAndStatusWithUser(
                List.of(3L, 2L),
                PostStatus.PUBLISHED
        )).willReturn(List.of(post2, post3));

        List<PostSummaryResponse> result = popularPostService.getPopularPosts(10);

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(3L, result.get(0).id());
        assertEquals("nickname", result.get(1).writerName());
        assertEquals("title2", result.get(1).title());

        verify(template).opsForZSet();
        verify(zSetOperations).reverseRange("post:popular:all", 0, 9);
        verify(postRepository).findAllByIdInAndStatusWithUser(
                List.of(3L, 2L),
                PostStatus.PUBLISHED
        );
    }
}