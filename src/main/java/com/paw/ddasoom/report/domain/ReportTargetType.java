package com.paw.ddasoom.report.domain;

/*
 * [신고 대상 종류 — 논리 참조의 타입 축]
 * Report는 (targetType, targetId)로 대상을 가리키므로 FK가 없음
 * 대상이 추가돼도 테이블/컬럼 변경 없이 이 enum에 상수 하나만 추가하면 됨
 */
public enum ReportTargetType {
  POST,          // 게시글
  POST_COMMENT,  // 게시글 댓글
  MEMBER         // 회원
}
