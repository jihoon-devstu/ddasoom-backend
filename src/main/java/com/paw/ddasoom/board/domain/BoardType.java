package com.paw.ddasoom.board.domain;

/**
 * 게시판 종류.
 * DB에는 VARCHAR(30)로 저장 (@Enumerated(EnumType.STRING) — DB 컨벤션 6장: ENUM 타입 금지)
 */
public enum BoardType {
    ADOPTION_REVIEW,   // 입양 후기
    PET_INFO           // 반려 정보
}