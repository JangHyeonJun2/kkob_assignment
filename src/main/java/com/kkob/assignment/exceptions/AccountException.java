package com.kkob.assignment.exceptions;

import com.kkob.assignment.enums.exception.ErrorCode;

public class AccountException extends BadRequestException{
    public AccountException(ErrorCode errorCode) {
        super(errorCode);
    }
}
