package com.paw.ddasoom.support.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FaqCreateRequest {

  @NotBlank(message = "카테고리는 필수입니다.")
  @Size(max = 50, message = "카테고리는 50자를 초과할 수 없습니다.")
  private String category;

  @NotBlank(message = "질문은 필수입니다.")
  @Size(max = 255, message = "질문은 255자를 초과할 수 없습니다.")
  private String question;

  @NotBlank(message = "답변은 필수입니다.")
  private String answer;
}
