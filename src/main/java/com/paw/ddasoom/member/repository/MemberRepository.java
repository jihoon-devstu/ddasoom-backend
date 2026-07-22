package com.paw.ddasoom.member.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.paw.ddasoom.member.domain.Member;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {
  Optional<Member> findByEmail(String email);
  boolean existsByEmail(String email);
  boolean existsByNickname(String nickname);

  // 기간별 가입자 수 집계 — 대시보드(금일)·통계(일별/월별 추이)가 공통 재사용
  long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
