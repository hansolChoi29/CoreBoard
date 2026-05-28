package com.example.coreboard.domain.post.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import static java.time.Duration.ofSeconds;

@Service
public class PostViewCountService {
    // Redis 조회수 증가, 중복 조회 방지 담당
    // post:view-count-delta:10
    private static final long VIEW_DEDUP_SECONDS = 600;
    private static final String VIEWED_KEY_PREFIX = "post:viewed:";
    private static final String VIEW_COUNT_DELTA_KEY_PREFIX = "post:view-count-delta:";
    private static final String POPULAR_POST_ZSET_KEY = "post:popular:all";
    private static final String POPULAR_POST_MEMBER_PREFIX = "post:";
    private final StringRedisTemplate template;

    public PostViewCountService(StringRedisTemplate template) {
        this.template = template;
    }

    // 1단계 : 조회 이벤트를 Redis 에 기록한다
    // 2단계 : Redis에 임시로 쌓아둔 조회수 (Redis delta)를 DB view_count에 반영한다
    // 3단계 : Redis ZSET 에서 인기글 목록을 조회한다
    // 4단계 : 인기글 목록 API를 만든다

    public void increaseIfFirstView(
            Long postId,
            String viewerKey
    ) {
        if (postId == null ||
                viewerKey == null ||
                viewerKey.isBlank()) {
            return;
        }
        String viewedKey = VIEWED_KEY_PREFIX + postId + ":" + viewerKey;
        // 자동 저장, 600초 후 자동 삭제
        Boolean isFirst = template.opsForValue()
                .setIfAbsent(viewedKey, "1", ofSeconds(VIEW_DEDUP_SECONDS));

        if (Boolean.TRUE.equals(isFirst)) {
            // 처음 조회면 delta
            template.opsForValue().increment(VIEW_COUNT_DELTA_KEY_PREFIX + postId);
            // 인기글 점수 증가
            template.opsForZSet().incrementScore(
                    POPULAR_POST_ZSET_KEY,
                    POPULAR_POST_MEMBER_PREFIX + postId,
                    1
            );
        }
    }
}
