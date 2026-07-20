package com.paw.ddasoom.qna.domain;

/*
 * [문의 스레드의 응대 상태]
 * 전이 방향이 작성자에 따라 반대 — 관리자 답변은 ANSWERED로, 유저 재질문은 PENDING으로 되돌림
 */
public enum QnaStatus {
    PENDING,   // 답변 대기 (생성 시 기본값 / 유저 재질문 시 복귀)
    ANSWERED   // 답변 완료 (관리자 코멘트 작성 시)
}