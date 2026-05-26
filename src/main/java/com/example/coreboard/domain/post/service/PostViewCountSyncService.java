package com.example.coreboard.domain.post.service;

import com.example.coreboard.domain.post.repository.PostRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

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
        Set<String> keys = template.keys(VIEW_COUNT_DELTA_KEY_PREFIX + "*");

        if (keys == null || keys.isEmpty()) {
            return;
        }

        for (String key : keys) {
            String value = template.opsForValue().get(key);

            if (value == null || value.isBlank()) {
                continue;
            }

            Long postId = extractPostId(key);
            long delta = Long.parseLong(value);

            int updatedCount = postRepository.increaseViewCount(postId, delta);
            if (updatedCount > 0) {
                template.delete(key);

            }
        }
    }

    private Long extractPostId(String key) {
        String postId = key.substring(VIEW_COUNT_DELTA_KEY_PREFIX.length());

        return Long.valueOf(postId);
    }
}
