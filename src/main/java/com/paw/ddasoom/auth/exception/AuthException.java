package com.paw.ddasoom.auth.exception;

import com.paw.ddasoom.common.exception.BusinessException;

public class AuthException extends BusinessException{

  public AuthException(AuthErrorCode errorCode){
    super(errorCode);
  }
}
