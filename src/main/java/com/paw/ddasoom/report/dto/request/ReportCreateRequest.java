package com.paw.ddasoom.report.dto.request;

import com.paw.ddasoom.report.domain.ReportReason;
import com.paw.ddasoom.report.domain.ReportTargetType;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
 * [신고 접수 요청]
 * enum 필드는 반드시 @NotNull로 검증 — @NotBlank는 CharSequence 전용이라 enum에 쓰면 검증 시점에 500 발생
 * 'ETC면 content 필수'는 필드 단독으로 판단할 수 없는 cross-field 규칙이라 서비스에서 검증
 */
@Getter
@NoArgsConstructor
public class ReportCreateRequest {

  @NotNull(message = "신고 대상 유형은 필수입니다.")
  private ReportTargetType targetType;

  @NotNull(message = "신고 대상은 필수입니다.")
  private Long targetId;

  @NotNull(message = "신고 사유는 필수입니다.")
  private ReportReason reason;

  private String content; // ETC일 때 필수 사용

}
