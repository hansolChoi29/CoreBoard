package com.example.coreboard.domain.comment.dto.response;

import com.example.coreboard.domain.comment.entity.Comment;

import java.time.LocalDateTime;

public record GetAllCommentResponse(
        Long commentId,
        String content,
        String nickname,
        LocalDateTime createdDate
) {
    public static GetAllCommentResponse from(Comment comment){
        return new GetAllCommentResponse(
                comment.getId(),
                comment.getContent(),
                // N+1, 기존 조회 방식에서는 Comment만 조회한 뒤 DTO 변환 과정에서 user.nickname에 접근한다.
                // user가 LAZY 로딩 대상이면 댓글 수만큼 user 추가 조회가 발생할 수 있어 N+1 문제가 생길 수 있다.
                comment.getUser().getNickname(),
                comment.getCreatedDate()
        );
    }
}
