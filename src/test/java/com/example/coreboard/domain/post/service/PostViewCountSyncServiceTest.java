package com.example.coreboard.domain.post.service;

import com.example.coreboard.domain.post.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;


import java.util.function.Consumer;

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

    @Mock
    Cursor<String> cursor;

    @InjectMocks
    PostViewCountSyncService postViewCountSyncService;

    @Test
    @DisplayName("SCAN_결과가_비어있으면_DB반영_금지")
    void notSaveWhenScanResultEmpty() {
        given(template.scan(any(ScanOptions.class))).willReturn(cursor);

        postViewCountSyncService.saveViewCountDelta();

        verify(template).scan(any(ScanOptions.class));
        verify(cursor).forEachRemaining(any());
        verify(cursor).close();

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
        given(template.scan(any(ScanOptions.class))).willReturn(cursor);
        given(template.opsForValue()).willReturn(valueOperations);

        doAnswer(invocation -> {
            Consumer<String> consumer = invocation.getArgument(0);
            consumer.accept("post:view-count-delta:10");
            return null;
        }).when(cursor).forEachRemaining(any());

        if ("NULL".equals(valueCase)) {
            given(valueOperations.get("post:view-count-delta:10")).willReturn(null);
        }

        if ("BLANK".equals(valueCase)) {
            given(valueOperations.get("post:view-count-delta:10")).willReturn("    ");
        }

        postViewCountSyncService.saveViewCountDelta();

        verify(template).scan(any(ScanOptions.class));
        verify(cursor).forEachRemaining(any());
        verify(valueOperations).get("post:view-count-delta:10");
        verify(cursor).close();

        verifyNoInteractions(postRepository);
        verify(valueOperations, never()).decrement(anyString(), anyLong());
        verify(template, never()).delete(anyString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "0",
            "-1"
    })
    @DisplayName("redis_delta_value가_0이하이면_DB반영_금지")
    void notSaveWhenDeltaIsZeroOrNegative(String value) {
        given(template.scan(any(ScanOptions.class))).willReturn(cursor);
        given(template.opsForValue()).willReturn(valueOperations);

        doAnswer(invocation -> {
            Consumer<String> consumer = invocation.getArgument(0);
            consumer.accept("post:view-count-delta:10");
            return null;
        }).when(cursor).forEachRemaining(any());

        given(valueOperations.get("post:view-count-delta:10")).willReturn(value);

        postViewCountSyncService.saveViewCountDelta();

        verify(template).scan(any(ScanOptions.class));
        verify(cursor).forEachRemaining(any());
        verify(valueOperations).get("post:view-count-delta:10");
        verify(cursor).close();

        verifyNoInteractions(postRepository);
        verify(valueOperations, never()).decrement(anyString(), anyLong());
        verify(template, never()).delete(anyString());
    }

    @ParameterizedTest
    @CsvSource({
            "1, 0, true",
            "1, 3, false",
            "0, 0, false"
    })
    @DisplayName("DB_update_결과와_Redis_잔여_delta에_따라_decrement와_delete_여부_결정")
    void decreaseRedisDeltaOnlyWhenDbUpdated(
            int updatedCount,
            long remain,
            boolean deleteExpected
    ) {
        given(template.scan(any(ScanOptions.class))).willReturn(cursor);
        given(template.opsForValue()).willReturn(valueOperations);

        doAnswer(invocation -> {
            Consumer<String> consumer = invocation.getArgument(0);
            consumer.accept("post:view-count-delta:10");
            return null;
        }).when(cursor).forEachRemaining(any());

        given(valueOperations.get("post:view-count-delta:10")).willReturn("7");
        given(postRepository.increaseViewCount(10L, 7L)).willReturn(updatedCount);

        if (updatedCount > 0) {
            given(valueOperations.decrement("post:view-count-delta:10", 7L)).willReturn(remain);
        }

        postViewCountSyncService.saveViewCountDelta();

        verify(template).scan(any(ScanOptions.class));
        verify(cursor).forEachRemaining(any());
        verify(valueOperations).get("post:view-count-delta:10");
        verify(postRepository).increaseViewCount(10L, 7L);
        verify(cursor).close();

        if (updatedCount > 0) {
            verify(valueOperations).decrement("post:view-count-delta:10", 7L);
        } else {
            verify(valueOperations, never()).decrement(anyString(), anyLong());
        }

        if (deleteExpected) {
            verify(template).delete("post:view-count-delta:10");
        } else {
            verify(template, never()).delete(anyString());
        }
    }

    @Test
    @DisplayName("Redis_decrement_결과가_null이면_delete_하지_않는다")
    void notDeleteWhenRemainIsNull() {
        given(template.scan(any(ScanOptions.class))).willReturn(cursor);
        given(template.opsForValue()).willReturn(valueOperations);

        doAnswer(invocation -> {
            Consumer<String> consumer = invocation.getArgument(0);
            consumer.accept("post:view-count-delta:10");
            return null;
        }).when(cursor).forEachRemaining(any());

        given(valueOperations.get("post:view-count-delta:10")).willReturn("7");
        given(postRepository.increaseViewCount(10L, 7L)).willReturn(1);
        given(valueOperations.decrement("post:view-count-delta:10", 7L)).willReturn(null);

        postViewCountSyncService.saveViewCountDelta();

        verify(template).scan(any(ScanOptions.class));
        verify(cursor).forEachRemaining(any());
        verify(valueOperations).get("post:view-count-delta:10");
        verify(postRepository).increaseViewCount(10L, 7L);
        verify(valueOperations).decrement("post:view-count-delta:10", 7L);
        verify(template, never()).delete(anyString());
        verify(cursor).close();
    }
}