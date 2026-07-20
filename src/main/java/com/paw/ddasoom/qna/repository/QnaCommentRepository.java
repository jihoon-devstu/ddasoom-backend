package com.paw.ddasoom.qna.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.paw.ddasoom.qna.domain.QnaComment;


public interface QnaCommentRepository extends JpaRepository<QnaComment, Long> {

  // 특정 문의의 활성 코멘트 스레드 (작성순, member fetch → N+1 제거)
  // 유저/관리자 코멘트가 한 테이블에 있어 UNION 없이 작성순 단일 조회로 스레드 완성
  @EntityGraph(attributePaths = "member")
  List<QnaComment> findByQna_IdAndDeletedAtIsNullOrderByCreatedAtAsc(Long qnaId);
}
