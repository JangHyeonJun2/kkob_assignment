package com.kkob.assignment.exceptions;

import com.kkob.assignment.enums.exception.ErrorCode;

public class UserException extends BadRequestException{
    public UserException(ErrorCode errorCode) {
        super(errorCode);
    }
}
