package com.paw.ddasoom.foster.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.paw.ddasoom.common.dto.PageResponse;
import com.paw.ddasoom.foster.domain.Foster;
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

  /** 관리자 임시보호신청 조회(디테일) */
  @Transactional(readOnly = true)
  public FosterAdminDetailResponse getFosterDetail(Long fosterId){
    Foster foster = fosterRepository.findById(fosterId)
                    .orElseThrow(() -> new FosterException(FosterErrorCode.FOSTER_NOT_FOUND));

    return FosterAdminDetailResponse.from(foster);
  }
  /** 관리자 임시보호신청 조회(리스트) */
  @Transactional(readOnly = true)
  public PageResponse<FosterAdminListResponse> getFosterList(Pageable pageable){
    Page<Foster> fosterList = fosterRepository.findAllByOrderByCreatedAtDesc(pageable);

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
  }
  
}
