package com.paw.ddasoom.member.exception;

import org.springframework.http.HttpStatus;

import com.paw.ddasoom.common.exception.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberErrorCode implements ErrorCode{

  MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "Member001", "해당 회원을 찾을 수 없습니다."),
  EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "MEMBER_002", "이미 사용 중인 이메일입니다."),
  NICKNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "MEMBER_003", "이미 사용 중인 닉네임입니다."),
  EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "MEMBER_004", "이메일 인증이 완료되지 않았습니다."),
  ALREADY_USER_ROLE(HttpStatus.BAD_REQUEST, "MEMBER_005", "이미 추가 정보 입력이 완료된 회원입니다."),
  ALREADY_DELETED_MEMBER(HttpStatus.BAD_REQUEST, "MEMBER_006", "이미 탈퇴 처리된 회원입니다.");


  
  // 실제 상태(필드)는 Enum이 가집니다.
    private final HttpStatus status;
    private final String code;
    private final String message;
}
