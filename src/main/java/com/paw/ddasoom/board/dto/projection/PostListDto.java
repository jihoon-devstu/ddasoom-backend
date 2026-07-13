package com.paw.ddasoom.board.dto.projection;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PostListDto {

    private final Long postId;
    private final String category;
    private final String title;
    private final String contentPreview;
    private final int viewCount;
    private final int commentCount;
    private final LocalDateTime createdAt;
    private final Long authorId;
    private final String authorNickname;
}
