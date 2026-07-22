package com.paw.ddasoom.animal.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.paw.ddasoom.animal.domain.AnimalGender;
import com.paw.ddasoom.animal.domain.AnimalKind;
import com.paw.ddasoom.animal.dto.request.AnimalListPageRequest;
import com.paw.ddasoom.animal.dto.response.AnimalDetailPageResponse;
import com.paw.ddasoom.animal.dto.response.AnimalListPageResponse;
import com.paw.ddasoom.animal.dto.response.AnimalMainPageResponse;
import com.paw.ddasoom.animal.dto.response.AnimalMyPageResponse;
import com.paw.ddasoom.animal.service.AnimalLikeService;
import com.paw.ddasoom.animal.service.AnimalQueryService;
import com.paw.ddasoom.common.dto.ApiResponse;
import com.paw.ddasoom.common.dto.PageResponse;
import com.paw.ddasoom.common.security.CustomUserDetails;
import com.paw.ddasoom.common.util.PageableSanitizer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(
  name = "유기동물(Animal)",
  description = "유기동물 목록·상세 조회, 좋아요/좋아요 취소, 마이페이지 좋아요 목록 API"
)
@RestController
@RequestMapping("/api/animals")
@RequiredArgsConstructor
@Slf4j
public class AnimalController {

  // private final AnimalNicknameService animalNicknameService;
  private final AnimalLikeService animalLikeService;
  private final AnimalQueryService animalQueryService;

  // 좋아요 버튼 클릭 시 (Redis dirty 기록 — 배치가 RDB 반영)
  @Operation(
    summary = "유기동물 좋아요",
    description = """
      해당 동물에 좋아요를 등록합니다.

      - 즉시 RDB에 반영되지 않고 Redis에 dirty로 기록되며, 배치가 주기적으로 RDB에 flush합니다.
      - 목록/상세 조회는 Redis의 미반영 변경분을 덮어써서 반영하므로(read-your-writes),
        좋아요 직후 바로 조회해도 isLiked=true로 보입니다.
      - 인가: USER, ADMIN
      """
  )
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
      responseCode = "200",
      description = "좋아요 반영 성공"
    ),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
      responseCode = "404",
      description = "동물 없음(ANIMAL_001)"
    )
  })
  @SecurityRequirement(name = "bearerAuth")
  @PostMapping("/{animalId}/likes")
  public ResponseEntity<ApiResponse<Void>> like(
    @PathVariable Long animalId,
    @AuthenticationPrincipal CustomUserDetails userDetails) {
    animalLikeService.like(animalId, userDetails.getMemberId());
    log.info("좋아요가 반영되었습니다. 동물ID: {}, 유저ID: {}", animalId, userDetails.getMemberId());
    return ResponseEntity.ok(ApiResponse.success("좋아요가 반영되었습니다."));
  }

  // 좋아요 취소 시
  @Operation(
    summary = "유기동물 좋아요 취소",
    description = """
        해당 동물의 좋아요를 취소합니다.

        - like와 동일하게 Redis에 dirty로 기록되며, 배치가 주기적으로 RDB에 flush합니다.
        - 인가: USER, ADMIN
        """
  )
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
      responseCode = "200",
      description = "좋아요 취소 성공"
    ),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
      responseCode = "404",
      description = "동물 없음(ANIMAL_001)"
    )
  })
  @SecurityRequirement(name = "bearerAuth")
  @DeleteMapping("/{animalId}/likes")
  public ResponseEntity<ApiResponse<Void>> unlike(
    @PathVariable Long animalId,
    @AuthenticationPrincipal CustomUserDetails userDetails) {
    animalLikeService.unlike(animalId, userDetails.getMemberId());
    log.info("좋아요가 취소되었습니다. 동물ID: {}, 유저ID: {}", animalId, userDetails.getMemberId());
    return ResponseEntity.ok(ApiResponse.success("좋아요가 취소되었습니다."));
  }

  /**
   * 유기동물 목록 조회 (동적 검색 + 페이징). 공개 API — 로그인 시 isLiked 계산.
   */
  @Operation(
    summary = "유기동물 목록 조회",
    description = """
        품종/지역/임시보호 여부/좋아요 여부/성별로 동적 필터링된 유기동물 목록을 페이징 조회합니다.

        - 인증 없이도 호출 가능(공개 API)
        - 로그인 상태면 응답의 isLiked가 실제 좋아요 여부로 채워지고, isLiked=true 필터도 사용 가능
        - 비로그인 상태에서 isLiked 파라미터를 넘겨도 무시됨(항상 isLiked=false로 응답)
        - kind/gender 미전달 시 필터 미적용(전체 조회)
        """
  )
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
      responseCode = "200",
      description = "조회 성공"
    )
  })
  @GetMapping("/list")
  public ResponseEntity<ApiResponse<PageResponse<AnimalListPageResponse>>> showAnimalList(
    @RequestParam(name = "kind", required = false) AnimalKind kind,
    @RequestParam(name = "location", required = false) String location,
    // primitive boolean은 미전달 시 항상 false로 바인딩돼 "필터 미적용"이 불가능 → Boolean으로 받아 null 허용
    @RequestParam(name = "isFostered", required = false) Boolean isFostered,
    @RequestParam(name = "isLiked", required = false) Boolean isLiked,
    @RequestParam(name = "gender", required = false) AnimalGender gender,
    @AuthenticationPrincipal(errorOnInvalidType = false) CustomUserDetails userDetails,
    @RequestParam(name = "page", defaultValue = "0") int page,
    @RequestParam(name = "size", defaultValue = "10") int size
  ) {
    Long memberId = userDetails != null ? userDetails.getMemberId() : null;
    AnimalListPageRequest request = AnimalListPageRequest.builder()
      .kind(kind)
      .location(location)
      .isFostered(isFostered)
      .isLiked(isLiked)
      .gender(gender)
      .build();
    return ResponseEntity.ok(ApiResponse.success(
      animalQueryService.search(request, memberId, PageableSanitizer.of(page, size))));
  }

  /**
   * 메인페이지 미리보기 — 최근 등록 4건 (공개, 비로그인 노출)
   */
  @Operation(
    summary = "메인페이지 유기동물 미리보기",
    description = """
        최근 등록(PK 내림차순) 4건을 반환합니다.

        - 인증 없이도 호출 가능(공개 API), 비로그인 사용자에게도 그대로 노출
        - 로그인 상태면 각 항목의 isLiked가 실제 좋아요 여부로 채워짐
        """
  )
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
      responseCode = "200",
      description = "조회 성공"
    )
  })
  @GetMapping("/main")
  public ResponseEntity<ApiResponse<List<AnimalMainPageResponse>>> getMainPreview(
    @AuthenticationPrincipal(errorOnInvalidType = false) CustomUserDetails userDetails
  ) {
    Long memberId = userDetails != null ? userDetails.getMemberId() : null;
    return ResponseEntity.ok(ApiResponse.success(animalQueryService.getMainPreview(memberId)));
  }

  /**
   * 유기동물 상세 조회. 공개 API — 로그인 시 isLiked 계산.
   */
  @Operation(
    summary = "유기동물 상세 조회",
    description = """
      동물 PK로 상세 정보를 조회합니다.

      - 인증 없이도 호출 가능(공개 API)
      - 로그인 상태면 isLiked가 실제 좋아요 여부로 채워지고, 비로그인이면 항상 false
      """
  )
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
      responseCode = "200",
      description = "조회 성공"
    ),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
      responseCode = "404",
      description = "동물 없음(ANIMAL_001)"
    )
  })
  @GetMapping("/{animalId}")
  public ResponseEntity<ApiResponse<AnimalDetailPageResponse>> getAnimalDetail(
    @PathVariable Long animalId,
    @AuthenticationPrincipal(errorOnInvalidType = false) CustomUserDetails userDetails
  ) {
    Long memberId = userDetails != null ? userDetails.getMemberId() : null;
    return ResponseEntity.ok(ApiResponse.success(
      animalQueryService.getDetail(animalId, memberId)));
  }

  /**
   * 마이페이지 — 내가 좋아요한 동물 목록 (인증 필요)
   */
  @Operation(
    summary = "내가 좋아요한 동물 목록 조회",
    description = """
      로그인한 사용자가 좋아요한 유기동물 목록을 페이징 조회합니다.

      - 인가: USER, ADMIN
      """
  )
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
      responseCode = "200",
      description = "조회 성공"
    )
  })
  @SecurityRequirement(name = "bearerAuth")
  @GetMapping("/me/likes")
  public ResponseEntity<ApiResponse<PageResponse<AnimalMyPageResponse>>> getMyLikedAnimals(
    @AuthenticationPrincipal CustomUserDetails userDetails,
    @RequestParam(name = "page", defaultValue = "0") int page,
    @RequestParam(name = "size", defaultValue = "10") int size
  ) {
    return ResponseEntity.ok(ApiResponse.success(
      animalQueryService.getMyLikedAnimals(userDetails.getMemberId(), PageableSanitizer.of(page, size))));
  }
}
