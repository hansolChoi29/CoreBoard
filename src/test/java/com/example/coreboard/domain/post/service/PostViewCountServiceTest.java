package com.example.coreboard.domain.post.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostViewCountServiceTest {
    @Mock
    StringRedisTemplate template;

    @Mock
    ValueOperations<String, String> valueOperations;

    @Mock
    ZSetOperations<String, String> zSetOperations;

    @InjectMocks
    PostViewCountService postViewCountService;

    @Test
    @DisplayName("최초_조회면_중복방지키를_저장하고_조회수_delta와_인기글_점수를_증가시킴")
    void increaseInFirstViewWhenFirstView() {
        Long postId = 10L;
        String viewerKey = "user:1";

        given(template.opsForValue()).willReturn(valueOperations);
        given(template.opsForZSet()).willReturn(zSetOperations);
        given(valueOperations.setIfAbsent(
                "post:viewed:10:user:1",
                "1",
                Duration.ofSeconds(600)
        )).willReturn(true);

        postViewCountService.increaseIfFirstView(postId, viewerKey);

        verify(valueOperations).setIfAbsent(
                "post:viewed:10:user:1",
                "1",
                Duration.ofSeconds(600)
        );
        verify(valueOperations).increment("post:view-count-delta:10");
        verify(zSetOperations).incrementScore(
                "post:popular:all",
                "post:10",
                1
        );
    }

    @Test
    @DisplayName("Redis_setIfAbsent_결과가_null_이면_조회수delta와_인기글_점수를_증가시키지_않음")
    void notIncreaseWhenSetIfAbsentResultNull() {
        Long postId = 10L;
        String viewerKey = "user:1";

        given(template.opsForValue()).willReturn(valueOperations);
        given(valueOperations.setIfAbsent(
                "post:viewed:10:user:1",
                "1",
                Duration.ofSeconds(600)
        )).willReturn(null);

        postViewCountService.increaseIfFirstView(postId, viewerKey);

        verify(valueOperations).setIfAbsent(
                "post:viewed:10:user:1",
                "1",
                Duration.ofSeconds(600)
        );
        verify(valueOperations, never()).increment(anyString());
        verify(template, never()).opsForZSet();
        verifyNoInteractions(zSetOperations);
    }

    @Test
    @DisplayName("게시글_중복_조회이면_조회수_delta와_인기글_점수를_증가시키지_않음")
    void notIncreaseAlreadyViewed() {
        Long postId = 10L;
        String viewerKey = "user:1";

        given(template.opsForValue()).willReturn(valueOperations);

        given(valueOperations.setIfAbsent(
                "post:viewed:10:user:1",
                "1",
                Duration.ofSeconds(600)
        )).willReturn(false);

        postViewCountService.increaseIfFirstView(postId, viewerKey);

        verify(valueOperations).setIfAbsent(
                "post:viewed:10:user:1",
                "1",
                Duration.ofSeconds(600)
        );
        verify(valueOperations, never()).increment(anyString());
        verify(template, never()).opsForZSet();
        verifyNoInteractions(zSetOperations);
    }

    @ParameterizedTest
    @ValueSource(strings = {"         "})
    @DisplayName("viewerKey가_null_또는_공백이면_Redis_접근불가")
    void notIncreaseWhenViewerKeyInvalid(String viewerKey) {
        postViewCountService.increaseIfFirstView(10L, viewerKey);

        verifyNoInteractions(template);
        verifyNoInteractions(valueOperations);
        verifyNoInteractions(zSetOperations);
    }

    @Test
    @DisplayName("postId가_null이면_Redis에_접근불가")
    void notIncreaseWhenPostIdNull() {
        postViewCountService.increaseIfFirstView(null, "user:1");

        verifyNoInteractions(template);
        verifyNoInteractions(valueOperations);
        verifyNoInteractions(zSetOperations);
    }
}