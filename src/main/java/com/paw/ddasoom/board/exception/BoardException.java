package com.paw.ddasoom.board.exception;

import com.paw.ddasoom.common.exception.BusinessException;

public class BoardException extends BusinessException {

    public BoardException(BoardErrorCode errorCode) {
        super(errorCode);
    }

}
