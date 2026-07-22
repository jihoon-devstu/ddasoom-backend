package com.paw.ddasoom.member.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.paw.ddasoom.member.domain.Member;
import com.paw.ddasoom.member.domain.Role;

/**
 * 관리자 회원 검색 — QueryDSL 구현부(MemberRepositoryImpl)의 계약.
 *
 * <p>Spring Data의 custom fragment 방식을 쓴 이유: 기존 호출부(memberRepository.searchForAdmin)를
 * 그대로 두면서 구현만 JPQL → QueryDSL로 갈아끼우기 위함이다. 별도 Repository 클래스로 빼면
 * 서비스가 두 개의 저장소를 알아야 해서 오히려 응집도가 떨어진다.
 */
public interface MemberRepositoryCustom {

    /**
     * 관리자 회원 목록 검색.
     *
     * @param keyword 이메일·닉네임 부분일치 (null이면 조건 미적용)
     * @param role    권한 필터 (null이면 전체)
     * @param status  상태 필터 — "ACTIVE" / "HIDDEN" / "DELETED" (null이면 전체).
     *                탈퇴(deletedAt)가 status 컬럼보다 우선하는 파생 개념이라 문자열로 받는다.
     * @param pageable 페이지·정렬. 정렬은 PageableSanitizer가 화이트리스트로 걸러 넘겨준다.
     */
    Page<Member> searchForAdmin(String keyword, Role role, String status, Pageable pageable);
}