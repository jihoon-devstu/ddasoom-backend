package com.paw.ddasoom.qna.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.paw.ddasoom.image.dto.response.ImageResponse;
import com.paw.ddasoom.member.domain.Role;
import com.paw.ddasoom.qna.domain.QnaComment;

import lombok.Builder;
import lombok.Getter;

/*
 * [코멘트 단건 응답]
 * 정적 팩토리 from()에서 조립 — 엔티티를 컨트롤러 밖으로 노출하지 않음
 * writerRole을 함께 내려, 유저/관리자 공용 테이블에 쌓인 스레드를 화면에서 구분할 수 있게 함
 */
@Getter
@Builder
public class QnaCommentResponse {

  private Long commentId;
  private String writerNickname;
  private Role writerRole;
  private String content;
  private LocalDateTime createdAt;
  private List<ImageResponse> images;

  public static QnaCommentResponse from(QnaComment comment, List<ImageResponse> images) {
    return QnaCommentResponse.builder()
      .commentId(comment.getId())
      .writerNickname(comment.getMember().getNickname())
      .writerRole(comment.getMember().getRole())
      .content(comment.getContent())
      .createdAt(comment.getCreatedAt())
      .images(images)
      .build();
  }

  
}
