package com.paw.ddasoom.image.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paw.ddasoom.image.domain.Image;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ImageResponse {

    private final Long imageId;
    private final String url;
    // Lombok getter가 isThumbnail() → Jackson 기본 직렬화 시 필드명이 "thumbnail"로 축약됨.
    // 프론트 계약(isThumbnail)과 일치시키기 위해 명시 고정.
    @JsonProperty("isThumbnail")
    private final boolean isThumbnail;

    @Builder
    private ImageResponse(Long imageId, String url, boolean isThumbnail) {
        this.imageId = imageId;
        this.url = url;
        this.isThumbnail = isThumbnail;
    }

    // 표준 from(Entity)와 달리 url을 별도로 받는 이유:
    // URL은 엔티티에 없는 계산값 — MinioUtil이 버킷/만료 정책에 따라 발급 (IMAGE_FLOW 3-7 허용 변형)
    public static ImageResponse from(Image image, String url) {
        return ImageResponse.builder()
                .imageId(image.getImageId())
                .url(url)
                .isThumbnail(image.isThumbnail())
                .build();
    }
}