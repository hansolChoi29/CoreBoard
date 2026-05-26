package com.example.coreboard.domain.post.service;

import com.example.coreboard.domain.post.dto.response.PostSummaryResponse;
import com.example.coreboard.domain.post.entity.Post;
import com.example.coreboard.domain.post.entity.PostStatus;
import com.example.coreboard.domain.post.repository.PostRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
public class PopularPostService {
    private static final String POPULAR_POST_ZSET_KEY = "post:popular:all";
    private static final String POPULAR_POST_MEMBER_PREFIX = "post:";

    private final StringRedisTemplate template;
    private final PostRepository postRepository;

    public PopularPostService(
            StringRedisTemplate template,
            PostRepository postRepository
    ) {
        this.template = template;
        this.postRepository = postRepository;
    }

    public List<PostSummaryResponse> getPopularPosts(int size) {
        Set<String> members = template.opsForZSet()
                .reverseRange(POPULAR_POST_ZSET_KEY, 0, size - 1);
        if (members == null || members.isEmpty()) {
            return List.of();
        }

        List<Long> postIds = members.stream()
                .map(this::extractPostId)
                .toList();

        List<Post> posts = postRepository.findAllByIdInAndStatusWithUser(
                postIds,
                PostStatus.PUBLISHED
        );
        return posts.stream()
                .sorted(Comparator.comparingInt(post -> postIds.indexOf(post.getId())))
                .map(post -> new PostSummaryResponse(
                        post.getId(),
                        post.getUser().getNickname(),
                        post.getTitle(),
                        post.getCreatedAt(),
                        post.getUpdatedAt()
                )).toList();
    }

    private Long extractPostId(String member) {
        String postId = member.substring(POPULAR_POST_MEMBER_PREFIX.length());
        return Long.valueOf(postId);
    }
}
