package com.paw.ddasoom.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.paw.ddasoom.member.domain.Member;

public interface MemberRepository extends JpaRepository<Member, Long>{
  Optional<Member> findByEmail(String email);
  boolean existsByEmail(String email);
  boolean existsByNickname(String nickname);
}
