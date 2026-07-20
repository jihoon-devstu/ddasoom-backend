package com.paw.ddasoom.qna.dto.response;

import java.time.LocalDateTime;

import com.paw.ddasoom.qna.domain.Qna;
import com.paw.ddasoom.qna.domain.QnaStatus;

import lombok.Builder;
import lombok.Getter;

// =========================================================================
// [문의 목록용 응답]
// 정적 팩토리 from()에서 조립 — 엔티티를 컨트롤러 밖으로 노출하지 않음
// 목록에서는 본문/코멘트를 싣지 않아 페이지 조회 비용을 낮춤
// =========================================================================

@Getter
@Builder
public class QnaSummaryResponse {

  private Long qnaId;
  private String questionerNickname;
  private String title;
  private QnaStatus status;
  private LocalDateTime createdAt;
  private LocalDateTime answeredAt;
  private LocalDateTime updatedAt;

  public static QnaSummaryResponse from (Qna qna) {
    return QnaSummaryResponse.builder()
      .qnaId(qna.getId())
      .questionerNickname(qna.getQuestioner().getNickname())
      .title(qna.getTitle())
      .status(qna.getStatus())
      .createdAt(qna.getCreatedAt())
      .answeredAt(qna.getAnsweredAt())
      .updatedAt(qna.getUpdatedAt())
      .build();
  }

}
