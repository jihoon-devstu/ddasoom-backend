package com.paw.ddasoom.support.dto.response;

import com.paw.ddasoom.support.domain.FaqCategory;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FaqCategoryResponse {

    private String value;   // Enum 상수명 (예: "FOSTER") — 프론트가 서버로 보낼 값
    private String label;   // 화면 표기 (예: "임시보호") — 사용자에게 보여줄 값

    public static FaqCategoryResponse from(FaqCategory category) {
        return FaqCategoryResponse.builder()
                .value(category.name())
                .label(category.getLabel())
                .build();
    }
}