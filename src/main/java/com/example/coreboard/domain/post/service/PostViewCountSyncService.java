package com.example.coreboard.domain.post.service;

import com.example.coreboard.domain.post.repository.PostRepository;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class PostViewCountSyncService {
    private static final String VIEW_COUNT_DELTA_KEY_PREFIX = "post:view-count-delta:";

    private final StringRedisTemplate template;
    private final PostRepository postRepository;

    public PostViewCountSyncService(
            StringRedisTemplate template,
            PostRepository postRepository
    ) {
        this.template = template;
        this.postRepository = postRepository;
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void saveViewCountDelta() {
        // SCAN으로 바꾸는 이유 : redis 서버를 멈칫둠칫하게 만들지 않기 위해
        // 원래 KEY 방식이었는데, redis 안의 키를 한 번에 전부 뒤지는 명령이었음
        // SCAN이나 Set 고려하라고 공식문서에서 안내함

        ScanOptions options = ScanOptions.scanOptions()
                .match(VIEW_COUNT_DELTA_KEY_PREFIX + "*") // 이 패턴에 맞는 키를
                .count(100) // 100개 정도씩 훑어라
                .build();
        Cursor<String> cursor = template.scan(options);
        try {
            cursor.forEachRemaining(key -> {
                String value = template.opsForValue().get(key);
                if (value == null || value.isBlank()) return;

                Long postId = extractPostId(key);
                long delta = Long.parseLong(value);

                if (delta <= 0) return;

                int updatedCount = postRepository.increaseViewCount(postId, delta);
                if (updatedCount > 0) {
                    Long remain = template.opsForValue().decrement(key, delta);

                    if (remain != null && remain <= 0) {
                        template.delete(key);
                    }
                }
            });
        } finally {
            cursor.close();
        }
    }

    private Long extractPostId(String key) {
        String postId = key.substring(VIEW_COUNT_DELTA_KEY_PREFIX.length());

        return Long.valueOf(postId);
    }
}
