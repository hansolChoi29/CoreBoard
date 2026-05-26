package com.example.coreboard.domain.post.service;

import com.example.coreboard.domain.post.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Set;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostViewCountSyncServiceTest {
    @Mock
    StringRedisTemplate template;

    @Mock
    ValueOperations<String, String> valueOperations;

    @Mock
    PostRepository postRepository;

    @InjectMocks
    PostViewCountSyncService postViewCountSyncService;

    @ParameterizedTest
    @ValueSource(strings = {
            "NULL",
            "EMPTY"
    })
    @DisplayName("redis_delta_key목록이_null_또는_비어있으면_DB반영_금지")
    void notSaveWhenKeysInvalid(String keyCase) {
        if ("NULL".equals(keyCase)) {
            given(template.keys("post:view-count-delta:*")).willReturn(null);
        }
        if ("EMPTY".equals(keyCase)) {
            given(template.keys("post:view-count-delta:*")).willReturn(Set.of());
        }

        postViewCountSyncService.saveViewCountDelta();

        verify(template).keys("post:view-count-delta:*");
        verifyNoInteractions(valueOperations);
        verifyNoInteractions(postRepository);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "NULL",
            "BLANK"
    })
    @DisplayName("redis_delta_value가_null_또는_공백이면_DB반영_금지")
    void notSaveWhenValueInvalid(String valueCase) {
        given(template.keys("post:view-count-delta:*")).willReturn(Set.of("post:view-count-delta:10"));
        given(template.opsForValue()).willReturn(valueOperations);

        if ("NULL".equals(valueCase)) {
            given(valueOperations.get("post:view-count-delta:10")).willReturn(null);
        }

        if ("BLANK".equals(valueCase)) {
            given(valueOperations.get("post:view-count-delta:10")).willReturn("    ");
        }

        postViewCountSyncService.saveViewCountDelta();

        verify(template).keys("post:view-count-delta:*");
        verify(valueOperations).get("post:view-count-delta:10");
        verifyNoInteractions(postRepository);
        verify(template, never()).delete(anyString());
    }

    @ParameterizedTest
    @CsvSource({
            "1, true",
            "0, false"
    })
    @DisplayName("DB_update_결과에_따라_Redis_delta_key_삭제여부_결정")
    void deleteRedisKeyOnlyWhenDbUpdated(int updatedCount, boolean deleteExpected) {
        given(template.keys("post:view-count-delta:*")).willReturn(Set.of("post:view-count-delta:10"));
        given(template.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("post:view-count-delta:10")).willReturn("7");
        given(postRepository.increaseViewCount(10L, 7L)).willReturn(updatedCount);

        postViewCountSyncService.saveViewCountDelta();

        verify(template).keys("post:view-count-delta:*");
        verify(valueOperations).get("post:view-count-delta:10");
        verify(postRepository).increaseViewCount(10L, 7L);

        if (deleteExpected) {
            verify(template).delete("post:view-count-delta:10");
        } else {
            verify(template, never()).delete(anyString());
        }
    }
}