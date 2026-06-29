package com.paw.ddasoom.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
    ErrorResponse response = ErrorResponse.of(
        ex.getErrorCode().getStatus(),
        ex.getErrorCode().getCode(),
        ex.getErrorCode().getMessage()
        );
    return new ResponseEntity<>(response, ex.getErrorCode().getStatus());
  }

    // 모든 처리되지 않은 예외를 위한 핸들러
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleAllUnhandledException(Exception ex) {
      ErrorResponse response = ErrorResponse.of(
              org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
              "GLOBAL_ERROR",
              "예상치 못한 서버 오류가 발생했습니다."
      );
      return new ResponseEntity<>(response, org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
