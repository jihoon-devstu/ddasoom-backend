package com.paw.ddasoom.image.service;

import com.paw.ddasoom.image.domain.Image;
import com.paw.ddasoom.image.domain.OwnerType;
import com.paw.ddasoom.image.dto.response.ImageResponse;
import com.paw.ddasoom.image.exception.ImageErrorCode;
import com.paw.ddasoom.image.exception.ImageException;
import com.paw.ddasoom.image.repository.ImageRepository;
import com.paw.ddasoom.image.util.MinioUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class ImageService {

    // 모바일 원본 사진(3~8MB) 대응 — Spring multipart 제한(11MB)보다 작아야 이 검증이 먼저 걸림 (IMAGE_FLOW 4장)
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    // 확장자/Content-Type 화이트리스트 — 속이는 경로가 달라 이중 검증 (파일명은 사용자 통제, 헤더는 클라이언트 신고값)
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp");

    private final ImageRepository imageRepository;
    private final MinioUtil minioUtil;

    /**
     * 파일을 업로드하고 임시 상태(owner_id NULL)로 저장한다. 소유자 연결은 attach()에서.
     *
     * <p>순서 고정: 검증 → MinIO → DB INSERT — MinIO 실패 시 DB에 흔적을 남기지 않기 위함.
     * (반대 순서면 "DB에 있는데 파일이 없는" 조회 깨짐 상태 가능 — IMAGE_FLOW 부록 1)
     *
     * @throws ImageException IMAGE_001 — 허용 외 형식 / IMAGE_002 — 10MB 초과 / IMAGE_005 — MinIO 실패
     */
    @Transactional
    public ImageResponse upload(MultipartFile file, OwnerType ownerType) {
        validateFile(file);

        String objectKey = minioUtil.upload(file, ownerType);

        Image image = Image.builder()
                .ownerType(ownerType)
                .imageKey(objectKey)
                .originalFileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .mimeType(file.getContentType())
                .build();
        imageRepository.save(image);

        String url = minioUtil.getUrl(ownerType, objectKey);
        return ImageResponse.from(image, url);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ImageException(ImageErrorCode.INVAILD_IMAGE_TYPE);
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ImageException(ImageErrorCode.IMAGE_SIZE_EXCEEDED);
        }

        String extension = extractExtension(file.getOriginalFilename());
        boolean isInvalidType = !ALLOWED_EXTENSIONS.contains(extension)
                || !ALLOWED_MIME_TYPES.contains(file.getContentType());
        if (isInvalidType) {
            throw new ImageException(ImageErrorCode.INVAILD_IMAGE_TYPE);
        }
    }

    // MinioUtil.createObjectKey가 "검증 완료된 확장자"를 전제하는데, 그 전제를 만드는 곳이 여기
    private String extractExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return "";   // 확장자 없음 → 화이트리스트에 없으니 자연히 INVALID_IMAGE_TYPE
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }
}