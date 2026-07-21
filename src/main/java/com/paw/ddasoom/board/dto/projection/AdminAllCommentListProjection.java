package com.paw.ddasoom.board.dto.projection;

import com.paw.ddasoom.board.domain.BoardType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 관리자 <b>전체 댓글 관리</b> 목록 조회 전용 projection — API 응답 DTO 아님(컨트롤러 밖으로 나가지 않음).
 *
 * <p>특정 게시글에 종속된 {@link AdminCommentListProjection}(게시글 상세 안 댓글 목록)과 달리,
 * 이 projection은 <b>사이트 전체 댓글을 한 화면에서 모더레이션</b>하기 위한 것이라 원글 컨텍스트를 함께 담는다:
 * <ul>
 *   <li>{@code postId}/{@code postTitle}/{@code boardType} 포함 — 어느 글의 댓글인지 표시하고
 *       행에서 원글 상세로 이동(프론트 {@code /admin/posts/{postId}})하기 위함.
 *       (마이페이지 {@link MyCommentListProjection}가 원글 제목/보드타입을 평면화하는 것과 동일 전략)</li>
 *   <li>{@code deletedAt} 포함 — 삭제된 댓글도 목록에 노출하고 상태를 뱃지로 구분하기 위함.
 *       (AdminMember 목록이 탈퇴 회원을 deletedAt으로 구분하는 것과 동일 철학)</li>
 * </ul>
 *
 * <p>{@code PostCommentRepository.findAllCommentsForAdmin}의 JPQL SELECT NEW가 직접 생성하며,
 * member/post를 JOIN으로 평면화(닉네임·원글 제목만 추출)해 N+1을 방지한다.
 * from() 팩토리 없음: JPQL 생성자 표현식은 public 생성자 직접 호출만 가능
 * (컨벤션의 from() 규칙은 API 경계 DTO 대상이므로 미적용 — 다른 projection과 동일).
 *
 * <p>⚠️ JPQL SELECT NEW 대상이므로 생성자 파라미터 순서가 쿼리와 일치해야 함.
 */
@Getter
@AllArgsConstructor
public class AdminAllCommentListProjection {

    private final Long commentId;
    private final Long memberId;
    private final String nickname;
    private final String content;
    private final Long postId;
    private final String postTitle;
    private final BoardType boardType;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime deletedAt;   // NULL = 활성, 값 있음 = 삭제됨
}