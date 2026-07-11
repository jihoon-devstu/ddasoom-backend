package com.paw.ddasoom.image.domain;

import com.paw.ddasoom.common.util.BaseTimeEntity;
import com.paw.ddasoom.image.exception.ImageErrorCode;
import com.paw.ddasoom.image.exception.ImageException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Getter
@Entity
@Table(name = "image",
        indexes = @Index(name = "idx_image_owner", columnList = "owner_type, owner_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Image extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id", nullable = false)
    private Long imageId;

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false, length = 30)
    private OwnerType ownerType;

    @Column(name = "owner_id")
    private Long ownerId;

    @Column(name = "image_key", nullable = false)
    private String imageKey;

    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    @Column(name = "file_size", nullable = false, columnDefinition = "BIGINT UNSIGNED")
    private Long fileSize;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "image_order", nullable = false, columnDefinition = "INT UNSIGNED")
    private int imageOrder;

    @Column(name = "is_thumbnail", nullable = false)
    private boolean isThumbnail;

    @Column(name = "deleted_at", columnDefinition = "DATETIME(6)")
    private LocalDateTime deletedAt;

    @Builder
    public Image(OwnerType ownerType, Long ownerId, String imageKey,
                 String originalFileName, Long fileSize, String mimeType) {
        this.ownerType = ownerType;
        this.ownerId = ownerId;
        this.imageKey = imageKey;
        this.originalFileName = originalFileName;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
    }

    // ========== 리치 도메인 메서드 (상태 변경은 아래 메서드로만) ==========

    /**
     * 소유자에 확정 연결한다. (게시글/문의 저장 시점에 ImageService.attach()가 호출)
     *
     * <p>같은 소유자로의 재연결은 no-op — syncImages()가 diff 없이
     * 전체 리스트로 재호출해도 안전한 이유가 이 분기다.
     *
     * @throws ImageException IMAGE_004 — 삭제된 이미지
     * @throws ImageException IMAGE_006 — ownerType 불일치 또는 이미 다른 소유자에 연결됨
     */
    public void attachTo(OwnerType ownerType, Long ownerId) {
        validateNotDeleted();

        boolean isAttachedToOther = this.ownerId != null && !this.ownerId.equals(ownerId);
        if (this.ownerType != ownerType || isAttachedToOther) {
            throw new ImageException(ImageErrorCode.IMAGE_OWNER_MISMATCH);
        }

        this.ownerId = ownerId;
    }

    /**
     * soft delete — 물리 삭제 금지 (DB 컨벤션 5장). MinIO 객체는 삭제하지 않는다 (고아 객체 수용 — IMAGE_FLOW 7장)
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * 대표 이미지로 지정한다. 소유자당 1개 보장은 ImageService.setThumbnail()이
     * 기존 대표를 unmark하는 방식으로 책임진다 — 엔티티는 자기 상태만 검증.
     *
     * @throws ImageException IMAGE_004 — 삭제된 이미지
     */
    public void markAsThumbnail() {
        validateNotDeleted();
        this.isThumbnail = true;
    }

    public void unmarkAsThumbnail() {
        this.isThumbnail = false;
    }

    /**
     * 노출 순서 갱신 — attach() 호출마다 요청 리스트의 인덱스로 매번 갱신된다.
     * 재연결이어도 갱신하므로, 드래그 재정렬은 재정렬된 imageIds를 syncImages()로 재전송하면 끝.
     */
    public void updateOrder(int order) {
        this.imageOrder = order;
    }

    // 삭제된 이미지는 어떤 상태 변경도 불가 — attachTo / markAsThumbnail 공통 가드
    private void validateNotDeleted() {
        if (this.deletedAt != null) {
            throw new ImageException(ImageErrorCode.IMAGE_NOT_FOUND);
        }
    }

}
