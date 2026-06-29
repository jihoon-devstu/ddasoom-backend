package com.paw.ddasoom.member.exception;

import org.springframework.http.HttpStatus;

import com.paw.ddasoom.common.exception.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberErrorCode implements ErrorCode{

  MEMBER_NOT_FOUNDE(HttpStatus.NOT_FOUND, "Member001", "해당 회원을 찾을 수 없습니다.");


  
  // 실제 상태(필드)는 Enum이 가집니다.
    private final HttpStatus status;
    private final String code;
    private final String message;
}
