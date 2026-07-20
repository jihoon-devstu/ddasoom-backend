package com.paw.ddasoom.qna.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.paw.ddasoom.qna.domain.Qna;
import com.paw.ddasoom.qna.domain.QnaStatus;

  // ==========================
  // [조회 전략]
  // ==========================

public interface QnaRepository extends JpaRepository<Qna, Long> {

  // 1) 상세 조회: 소유권 검증 및 상세 응답 시 사용
  @EntityGraph(attributePaths = "questioner")
  Optional<Qna> findByIdAndDeletedAtIsNull(Long id);

  // 2) 유저용: 본인 문의 목록 조회
  @EntityGraph(attributePaths = "questioner")
  Page<Qna> findByQuestioner_IdAndDeletedAtIsNullOrderByCreatedAtDesc(Long questionerId, Pageable pageable);

  // 3) 관리자용 (전체): 상태 미지정 전체 목록
  @EntityGraph(attributePaths = "questioner")
  Page<Qna> findByDeletedAtIsNullOrderByCreatedAtDesc(Pageable pageable);

  // 4) 관리자용 (필터): 특정 상태 필터링 목록 (정렬 + status 조건 인덱스 의존)
  @EntityGraph(attributePaths = "questioner")
  Page<Qna> findByStatusAndDeletedAtIsNullOrderByCreatedAtDesc(QnaStatus status, Pageable pageable);
}