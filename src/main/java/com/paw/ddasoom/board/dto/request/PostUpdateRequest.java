package com.paw.ddasoom.board.dto.request;

import com.paw.ddasoom.board.domain.BoardType;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostUpdateRequest {

    @NotBlank(message = "게시판 종류를 선택해주세요.")
    private String boardType;

    private String category;

    @NotBlank(message = "제목을 입력해주세요.")
    @Size(max = 255, message = "제목은 255자 이하여야 합니다.")
    private String title;

    @NotBlank(message = "내용을 입력해주세요.")
    @Size(max = 10000, message = "본문이 제한된 길이를 초과했습니다.")
    private String content;

    /** 수정 후 최종 이미지 ID 목록 — 기존 활성 중 여기 없는 것은 soft delete (syncImages diff 기준) */
    @Size(max = 20, message = "이미지는 최대 20장입니다.")
    private List<Long> imageIds;

    /** 대표 이미지 재지정 — 미지정 시 null (기존 썸네일이 imageIds에서 빠지면 프론트가 재지정 유도) */
    private Long thumbnailImageId;
}