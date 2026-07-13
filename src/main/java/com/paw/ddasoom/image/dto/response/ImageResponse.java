package com.paw.ddasoom.image.dto.response;

import com.paw.ddasoom.image.domain.Image;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ImageResponse {

    private final Long imageId;
    private final String url;
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