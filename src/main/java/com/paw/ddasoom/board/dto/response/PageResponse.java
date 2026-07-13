package com.paw.ddasoom.board.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

/**
 * 페이징 목록 응답 공용 래퍼.
 * Spring Data의 Page<T>를 그대로 노출하지 않고 프론트에 필요한 메타데이터만 골라 내려줌
 * (Page 원본은 pageable/sort 등 내부 구현 정보가 과다 노출되고, 직렬화 구조가 Spring 버전에 따라 바뀔 수 있음).
 * 현재 board 전용 — 2개 이상 도메인에서 필요해지는 시점에 common 승격 (CONVENTIONS 1장 원칙).
 */
@Getter
public class PageResponse<T> {

    private final List<T> content;
    private final int page;            // 현재 페이지 번호 (0부터 시작)
    private final int size;            // 페이지당 요청 개수
    private final long totalElements;  // 전체 게시글 수
    private final int totalPages;      // 전체 페이지 수 — 프론트 "1페이지 2페이지" 버튼 렌더링 기준
    private final boolean hasNext;
    private final boolean hasPrevious;

    @Builder
    private PageResponse(List<T> content, int page, int size,
                         long totalElements, int totalPages,
                         boolean hasNext, boolean hasPrevious) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.hasNext = hasNext;
        this.hasPrevious = hasPrevious;
    }

    public static <T> PageResponse<T> from(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }
}