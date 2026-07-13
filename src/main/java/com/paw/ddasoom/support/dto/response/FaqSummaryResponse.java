package com.paw.ddasoom.support.dto.response;

import com.paw.ddasoom.support.domain.Faq;

import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class FaqSummaryResponse {

  private Long faqId;
  private String category;
  private String question;
  private Boolean isVisible;

  public static FaqSummaryResponse from(Faq faq) {
    return FaqSummaryResponse.builder()
      .faqId(faq.getId())
      .category(faq.getCategory())
      .question(faq.getQuestion())
      .isVisible(faq.getIsVisible())
      .build();
  }
}
