package com.paw.ddasoom.report.domain;

/*
 * [신고 처리 상태]
 * PENDING에서만 APPROVED/REJECTED로 전이 가능 (Report.validatePending이 강제)
 * 한 번 판정된 신고는 되돌리지 않음 — 재판정이 필요하면 새 신고로 접수
 */
public enum ReportStatus {
  PENDING,   // 미처리 (접수 시 기본값 / 관리자 큐의 주 조회 대상)
  APPROVED,  // 승인 — 대상 숨김 처리까지 수행됨
  REJECTED   // 반려 — 허위 판정, 누적 신고 집계에서 제외
}
