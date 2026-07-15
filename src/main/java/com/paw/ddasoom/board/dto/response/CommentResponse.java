package com.paw.ddasoom.board.dto.response;

import com.paw.ddasoom.board.domain.PostComment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CommentResponse {

    private final Long commentId;
    private final AuthorResponse author;
    private final String content;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static CommentResponse from(PostComment comment) {
        return CommentResponse.builder()
                .commentId(comment.getId())
                .author(AuthorResponse.from(comment.getMember()))
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
