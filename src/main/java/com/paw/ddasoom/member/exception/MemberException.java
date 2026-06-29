package com.paw.ddasoom.member.exception;

import com.paw.ddasoom.common.exception.BusinessException;

public class MemberException extends BusinessException{

  public MemberException(MemberErrorCode errorCode){
    super(errorCode);
  }

}
