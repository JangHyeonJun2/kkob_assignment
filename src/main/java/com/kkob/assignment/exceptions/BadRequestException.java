package com.kkob.assignment.exceptions;

import com.kkob.assignment.enums.exception.ErrorCode;
import lombok.Getter;

@Getter
public class BadRequestException extends RuntimeException{

    private final ErrorCode errorCode;

    public BadRequestException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
