package com.paw.ddasoom.board.dto.response;

import com.paw.ddasoom.board.domain.BoardType;
import com.paw.ddasoom.board.dto.projection.AdminAllCommentListProjection;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 관리자 <b>전체 댓글 관리</b> 목록 행 응답.
 *
 * <p>게시글 상세 안 댓글 응답인 {@link AdminCommentResponse}와 달리 원글 컨텍스트
 * ({@code postId}/{@code postTitle}/{@code boardType})를 포함한다 — 사이트 전체 댓글을
 * 한 목록에서 보여줄 때 어느 글의 댓글인지 표시하고, 행에서 원글 상세로 이동하기 위함.
 * {@code deletedAt}으로 활성/삭제 상태를 프론트가 뱃지로 구분하는 규격은 동일하다.
 */
@Getter
@Builder
public class AdminAllCommentResponse {

    private final Long commentId;
    private final AuthorResponse author;
    private final String content;
    private final Long postId;
    private final String postTitle;
    private final BoardType boardType;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime deletedAt;   // null = 활성, 값 있음 = 삭제됨

    public static AdminAllCommentResponse from(AdminAllCommentListProjection projection) {
        return AdminAllCommentResponse.builder()
                .commentId(projection.getCommentId())
                .author(AuthorResponse.from(projection.getMemberId(), projection.getNickname()))
                .content(projection.getContent())
                .postId(projection.getPostId())
                .postTitle(projection.getPostTitle())
                .boardType(projection.getBoardType())
                .createdAt(projection.getCreatedAt())
                .updatedAt(projection.getUpdatedAt())
                .deletedAt(projection.getDeletedAt())
                .build();
    }
}