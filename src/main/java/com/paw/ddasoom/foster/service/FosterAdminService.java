package com.paw.ddasoom.foster.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.paw.ddasoom.common.dto.PageResponse;
import com.paw.ddasoom.foster.domain.Foster;
import com.paw.ddasoom.foster.domain.FosterStatus;
import com.paw.ddasoom.foster.dto.request.FosterAdminUpdateRequest;
import com.paw.ddasoom.foster.dto.response.FosterAdminDetailResponse;
import com.paw.ddasoom.foster.dto.response.FosterAdminListResponse;
import com.paw.ddasoom.foster.exception.FosterErrorCode;
import com.paw.ddasoom.foster.exception.FosterException;
import com.paw.ddasoom.foster.repository.FosterRepository;
import com.paw.ddasoom.member.domain.Member;
import com.paw.ddasoom.member.exception.MemberErrorCode;
import com.paw.ddasoom.member.exception.MemberException;
import com.paw.ddasoom.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FosterAdminService {

  private final FosterRepository fosterRepository;
  private final MemberRepository memberRepository;
  /** 임시보호가 진행 중인 상태를 판단하는 목록 */
  private static final List<FosterStatus> ACTIVE_FOSTER_STATUSES = List.of(
    FosterStatus.FOSTERING, // 임시보호중
    FosterStatus.EXTENDED   // 임시보호연장
  );

  /** 관리자 임시보호신청 조회(디테일) */
  @Transactional(readOnly = true)
  public FosterAdminDetailResponse getFosterDetail(Long fosterId){
    Foster foster = fosterRepository.findById(fosterId)
                    .orElseThrow(() -> new FosterException(FosterErrorCode.FOSTER_NOT_FOUND));

    return FosterAdminDetailResponse.from(foster);
  }
  /** 관리자 임시보호신청 조회(리스트) */
  @Transactional(readOnly = true)
  public PageResponse<FosterAdminListResponse> getFosterList(
    FosterStatus status,
    boolean includeDeleted,
    Pageable pageable){
    Page<Foster> fosterList = fosterRepository.findAllForAdmin(status, includeDeleted, pageable);

    return PageResponse.of(fosterList, FosterAdminListResponse::from);
  }

  /** 관리자 임시보호신청 수정 */
  @Transactional
  public void updateFoster(Long adminId, Long fosterId, FosterAdminUpdateRequest request){
    Foster foster = fosterRepository.findById(fosterId)
                    .orElseThrow(() -> new FosterException(FosterErrorCode.FOSTER_NOT_FOUND));
    Member reviewer = memberRepository.findById(adminId)
                    .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

      foster.updateAdminReview(
        reviewer,
        request.getAnswer(),
        request.getStatus(),
        request.getFosterStartAt(),
        request.getFosterEndAt(),
        request.getFosterExtendAt(),
        request.getFosterCompleteAt());
    
        syncAnimalFosterStatus(foster);
  }
  /** 업데이트시 상태값에 따른 animal 데이터 임시보호 여부 변경 */
  private void syncAnimalFosterStatus(Foster foster){
    // 현재 animalId를 가져와 임시보호중인지 연장중인지 확인
    boolean isFostered = fosterRepository.existsByAnimal_IdAndStatusInAndDeletedAtIsNull(
      foster.getAnimal().getId(), ACTIVE_FOSTER_STATUSES);

    //animal에 임시보호 값 반영
    foster.getAnimal().updateFosteredStatus(isFostered);
  }
  
}
