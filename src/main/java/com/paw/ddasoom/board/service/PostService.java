package com.paw.ddasoom.board.service;

import com.paw.ddasoom.board.domain.BoardType;
import com.paw.ddasoom.board.domain.Post;
import com.paw.ddasoom.board.dto.projection.PostListProjection;
import com.paw.ddasoom.board.dto.request.PostCreateRequest;
import com.paw.ddasoom.board.dto.request.PostUpdateRequest;
import com.paw.ddasoom.board.dto.response.PageResponse;
import com.paw.ddasoom.board.dto.response.PostDetailResponse;
import com.paw.ddasoom.board.dto.response.PostResponse;
import com.paw.ddasoom.board.exception.BoardErrorCode;
import com.paw.ddasoom.board.exception.BoardException;
import com.paw.ddasoom.board.repository.PostRepository;
import com.paw.ddasoom.image.domain.OwnerType;
import com.paw.ddasoom.image.dto.response.ImageResponse;
import com.paw.ddasoom.image.service.ImageService;
import com.paw.ddasoom.member.domain.Member;
import com.paw.ddasoom.member.exception.MemberErrorCode;
import com.paw.ddasoom.member.exception.MemberException;
import com.paw.ddasoom.member.repository.MemberRepository;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

    /**
     * 카테고리 화이트리스트 — 프론트 select 제약은 신뢰 대상이 아니므로 서버에서 재검증.
     * 생성/수정 시에만 엄격 검증. 목록 조회 필터는 검증하지 않음 (미존재 값 → 자연히 빈 결과).
     */
    private static final Map<BoardType, Set<String>> CATEGORY_WHITELIST = Map.of(
            BoardType.ADOPTION_REVIEW, Set.of("강아지", "고양이"),
            BoardType.PET_INFO, Set.of("강아지", "고양이", "예방접종")
    );

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final ImageService imageService;

    @Transactional
    public Long createPost(Long memberId, PostCreateRequest request) {
        validateCategory(request.getBoardType(), request.getCategory());
        Member member = getMember(memberId);

        Post post = request.toEntity(member);
        postRepository.save(post);

        // 하이브리드 업로드 확정 연결 — attach(순서 확정) → setThumbnail 순 (IMAGE_FLOW 3-6)
        imageService.attach(request.getImageIds(), OwnerType.POST, post.getId());
        if (request.getThumbnailImageId() != null) {
            imageService.setThumbnail(request.getThumbnailImageId(), OwnerType.POST, post.getId());
        }
        return post.getId();
    }

    @Transactional(readOnly = true)
    public PageResponse<PostResponse> getPostList(BoardType boardType, String category, Pageable pageable) {
        Page<PostListProjection> page = postRepository.findPostList(boardType, category, pageable);

        List<Long> postIds = page.getContent().stream()
                .map(PostListProjection::getPostId)
                .toList();
        // 썸네일 배치 조회 — 게시글 수와 무관하게 쿼리 1번 (N+1 방지)
        Map<Long, String> thumbnailUrls = imageService.getThumbnailUrls(OwnerType.POST, postIds);

        Page<PostResponse> mapped = page.map(projection ->
                PostResponse.from(projection, thumbnailUrls.get(projection.getPostId())));
        return PageResponse.from(mapped);
    }

    @Transactional  // readOnly 아님 — 조회수 증가(쓰기) 포함
    public PostDetailResponse getPostDetail(Long postId) {
        Post post = getPost(postId);
        post.increaseViewCount();  // dirty checking으로 UPDATE 반영

        List<ImageResponse> images = imageService.getImages(OwnerType.POST, postId);
        return PostDetailResponse.from(post, images);
    }

    @Transactional
    public void updatePost(Long memberId, Long postId, PostUpdateRequest request) {
        validateCategory(request.getBoardType(), request.getCategory());
        Post post = getPost(postId);
        validateAuthor(post, memberId);

        post.update(request.getBoardType(), request.getCategory(),
                request.getTitle(), request.getContent());

        // 수정은 diff 동기화 — 빠진 이미지 soft delete + 순서 갱신 (IMAGE_FLOW 3-6)
        imageService.syncImages(request.getImageIds(), OwnerType.POST, postId);
        if (request.getThumbnailImageId() != null) {
            imageService.setThumbnail(request.getThumbnailImageId(), OwnerType.POST, postId);
        }
    }

    @Transactional
    public void deletePost(Long memberId, Long postId) {
        Post post = getPost(postId);
        validateAuthor(post, memberId);

        post.softDelete();
        // 소유자 삭제 시 이미지 일괄 정리 = 빈 리스트 sync (IMAGE_FLOW 3-6 패턴)
        imageService.syncImages(List.of(), OwnerType.POST, postId);
    }

    // ===== private =====

    private Post getPost(Long postId) {
        return postRepository.findDetailById(postId)
                .orElseThrow(() -> new BoardException(BoardErrorCode.POST_NOT_FOUND));
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    private void validateCategory(BoardType boardType, String category) {
        Set<String> allowed = CATEGORY_WHITELIST.get(boardType);
        if (allowed == null || !allowed.contains(category)) {
            throw new BoardException(BoardErrorCode.INVALID_CATEGORY);  // BOARD_008 신규 추가 필요
        }
    }

    private void validateAuthor(Post post, Long memberId) {
        // ⚠️ Long은 == 비교 금지 (128 초과 시 항상 false) — equals 필수
        if (!post.getMember().getId().equals(memberId)) {
            throw new BoardException(BoardErrorCode.POST_ACCESS_DENIED);  // BOARD_002, 403
        }
    }
}