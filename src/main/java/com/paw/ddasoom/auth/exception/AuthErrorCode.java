package com.paw.ddasoom.auth.exception;

import org.springframework.http.HttpStatus;

import com.paw.ddasoom.common.exception.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode{

  EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "AUTH_001", "이미 사용 중인 이메일입니다."),
  NICKNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "AUTH_002", "이미 사용 중인 닉네임입니다."),
  EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "AUTH_003", "이메일 인증이 완료되지 않았습니다."),
  INVALID_AUTH_CODE(HttpStatus.BAD_REQUEST, "AUTH_004", "인증 번호가 일치하지 않거나 만료되었습니다."),
  MAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH_005", "메일 전송에 실패했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
