package com.paw.ddasoom.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  // BusinessException 으로 등록된 모든 예외 처리 핸들러
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

  // @Valid (정규식 , Pattern , NotBlank 등) 유효성 검사 실패 예외 처리
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
      // 발생한 에러 중 첫 번째 에러 메시지를 추출 (예: "비밀번호는 대소문자... 8자 이상이어야 합니다.")
      FieldError fieldError = e.getBindingResult().getFieldError();
      String errorMessage = fieldError != null ? fieldError.getDefaultMessage() : "입력값이 올바르지 않습니다.";

      // 만들어둔 ErrorResponse.of() 메서드 활용
      ErrorResponse response = ErrorResponse.of(HttpStatus.BAD_REQUEST, "INVALID_INPUT", errorMessage);

      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }
}
