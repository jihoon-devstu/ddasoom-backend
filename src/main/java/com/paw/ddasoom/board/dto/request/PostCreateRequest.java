package com.paw.ddasoom.board.dto.request;

import com.paw.ddasoom.board.domain.BoardType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class PostCreateRequest {

    private BoardType boardType;
    private String category;
    private String title;
    private String content;
    private List<Long> imageIds;        // 하이브리드 업로드 확정 연결용
    private Long thumbnailImageId;      // 대표 이미지 명시 지정 (nullable)
}
