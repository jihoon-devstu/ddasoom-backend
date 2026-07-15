package com.paw.ddasoom.animal.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.paw.ddasoom.animal.domain.Animal;
import com.paw.ddasoom.animal.domain.AnimalGender;
import com.paw.ddasoom.animal.domain.AnimalKind;
import com.paw.ddasoom.animal.dto.request.AnimalListPageRequest;
import com.paw.ddasoom.animal.dto.request.AnimalNicknameUpdateRequest;
import com.paw.ddasoom.animal.dto.response.AnimalListPageResponse;
import com.paw.ddasoom.animal.service.AnimalLikeService;
import com.paw.ddasoom.animal.service.AnimalListPageServiceImpl;
import com.paw.ddasoom.animal.service.AnimalNicknameService;
import com.paw.ddasoom.animal.service.AnimalSyncService;
import com.paw.ddasoom.common.dto.ApiResponse;
import com.paw.ddasoom.common.dto.PageResponse;
import com.paw.ddasoom.common.security.CustomUserDetails;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/animals")
@RequiredArgsConstructor
@Slf4j
public class AnimalController {

  private final AnimalNicknameService animalNicknameService;
  private final AnimalSyncService animalSyncService;
  private final AnimalLikeService animalLikeService;
  private final AnimalListPageServiceImpl animalListPageServiceImpl;

  /**
   * API에서 받아온 모든 동물 정보를 DB에 저장 (관리자용/동기화용)
   * @return Httpstatus, message, DTO
   */
  @PostMapping("/sync")
  public ResponseEntity<ApiResponse<Void>> syncAnimals() {
      List<Animal> savedAnimals = animalSyncService.syncAnimals();
      log.info("API 동물 {}건이 DB에 저장/갱신되었습니다.", savedAnimals.size());
      return ResponseEntity.ok(ApiResponse.success("API 유기동물이 DB에 저장/갱신되었습니다."));
  }

  /**
   * 닉네임 이름 수정 요청 시, 변경된 닉네임 저장 (임보 보호자용)
   * @param animalId
   * @param request
   * @return Httpstatus, message, DTO
   */
  @PatchMapping("/{animalId}/nickname")
  public ResponseEntity<ApiResponse<Void>> updateNickname(
      @PathVariable Long animalId,
      @Valid @RequestBody AnimalNicknameUpdateRequest request) {
    animalNicknameService.updateNickname(animalId, request.nickname());
    log.info("닉네임 이름이 변경되었습니다: {}", request.nickname());
    return ResponseEntity.ok(ApiResponse.success("유기동물 이름이 변경되었습니다."));
  }

  /**
   * 좋아요 버튼 클릭 시, 좋아요 카운트 업데이트
   * @param animalId
   * @param userDetails
   * @param request
   * @return Httpstatus, message,
   */
  @PostMapping("/{animalId}/likes")
  public ResponseEntity<ApiResponse<Void>> like(
    @PathVariable Long animalId,
    @AuthenticationPrincipal CustomUserDetails userDetails) {
    animalLikeService.like(animalId, userDetails.getMemberId());
    log.info("좋아요가 반영되었습니다. 동물ID: {}, 유저ID: {}", animalId, userDetails.getMemberId());
    return ResponseEntity.ok(ApiResponse.success("좋아요가 반영되었습니다."));
  }

  /**
   * 좋아요 취소 시, 좋아요 카운트 업데이트
   * @param animalId
   * @param userDetails
   * @return
   */
  @DeleteMapping("/{animalId}/likes")
  public ResponseEntity<ApiResponse<Void>> unlike(
    @PathVariable Long animalId,
    @AuthenticationPrincipal CustomUserDetails userDetails) {
    animalLikeService.unlike(animalId, userDetails.getMemberId());
    log.info("좋아요가 취소되었습니다. 동물ID: {}, 유저ID: {}", animalId, userDetails.getMemberId());
    return ResponseEntity.ok(ApiResponse.success("좋아요가 취소되었습니다."));
  }

  /**
   * 메인화면에서 유기동물 더 보기 클릭 시
   * 배너에서 유기동물 조회 클릭 시
   * @return
   */
  @GetMapping("/list")
  public ResponseEntity<ApiResponse<PageResponse<AnimalListPageResponse>>> showAnimalList(
    // @RequestParam의 받아올 값을 Enum값으로 설정하면, WebConfig의 converter에서 컨트롤러에서 컨트롤러 진입 전에 Enum으로 바꿔놓음
    @RequestParam(name = "kind", required = false) AnimalKind kind, 
    @RequestParam(name = "location", required = false) String location,
    @RequestParam(name = "isFostered", required = false) boolean isFostered,
    @RequestParam(name = "gender", required = false) AnimalGender gender,
    @RequestParam(name = "page", defaultValue = "0") int page,
    @RequestParam(name = "size", defaultValue = "10") int size
  ) {
    // requrest params가 많아서 dto로 담아서 보냄
    AnimalListPageRequest request = AnimalListPageRequest.builder()
      .kind(kind)
      .location(location)
      .isFostered(isFostered)
      .gender(gender)
      .build();
    return ResponseEntity.ok(ApiResponse.success(animalListPageServiceImpl.search(request, PageRequest.of(page, size))));
  }
  
}
