package com.paw.ddasoom.animal.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
 
import com.paw.ddasoom.animal.service.AnimalSyncService;
import com.paw.ddasoom.common.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
 
/**
 * 관리자용 유기동물 API.
 * /api/admin 하위 = SecurityConfig가 ADMIN으로 자동 잠금 (SECURITY-FLOW 규칙 5)
 * 공공데이터포털 수동 미러링 동기화 버튼이 이 엔드포인트를 호출한다.
 */
@Tag(
    name = "관리자 - 유기동물(Admin Animal)",
    description = "공공데이터포털 유기동물 데이터를 수동으로 동기화하는 관리자 전용 API"
)
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/admin/animals")
@RequiredArgsConstructor
@Slf4j
public class AdminAnimalController {
 
  private final AnimalSyncService animalSyncService;
 
  @Operation(
      summary = "유기동물 데이터 수동 동기화",
      description = """
          공공데이터포털 유기동물 API를 호출해 전체 페이지를 조회하고,
          유효성 검증(품종/성별 enum, 구조일자 파싱)을 통과한 항목만 DB에 upsert합니다.
 
          - abandonmentId 기준 upsert: 신규면 INSERT, 기존이면 나머지 필드 UPDATE
          - like_count, is_fostered는 이 동기화가 절대 덮어쓰지 않음(다른 도메인 소유 컬럼)
          - nickname은 신규 등록일 때만 부여, 기존 동물은 유지
          - 값 하나가 이상해도 해당 건만 건너뛰고 나머지는 정상 upsert (부분 성공 허용)
          - 매일 새벽 4시(KST) 스케줄러가 호출하는 동기화와 완전히 같은 로직
          - 인가: ADMIN
          """
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "동기화 성공(0건 upsert여도 200)"
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "502",
          description = "공공데이터포털 API 호출 실패(ANIMAL_004)"
      )
  })
  @PostMapping("/sync")
  public ResponseEntity<ApiResponse<Void>> syncAnimals() {

    long start = System.nanoTime();

    // AnimalSyncService가 JDBC 벌크 upsert로 바뀌면서 저장된 엔티티 목록 대신 건수(int)를 반환한다.
    int savedCount = animalSyncService.syncAnimals();

    long elapsedMs = (System.nanoTime() - start) / 1_000_000;
    log.info("API 동물 {}건이 DB에 저장/갱신되었습니다. 걸린 시간: {}ms", savedCount, elapsedMs);
    return ResponseEntity.ok(ApiResponse.success("API 유기동물이 DB에 저장/갱신되었습니다."));
  }
}