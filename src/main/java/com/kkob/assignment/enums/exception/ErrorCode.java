package com.kkob.assignment.enums.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCode {
    INVALID_PASSWORD(404, "ACCOUNT_ERROR", "계좌 비밀번호가 잘못되었습니다."),
    LACK_OF_BALANCE(500, "ACCOUNT_ERROR", "잔액이 부족합니다."),
    INVALID_RECEIVER(404, "USER_ERROR", "받는분이 잘 못되었습니다."),
    NOT_FOUND_ACCOUNT(404, "ACCOUNT_ERROR", "계좌를 찾을 수 없습니다."),
    NOT_FOUND_USER(404, "USER_ERROR", "사용자를 찾을 수 없습니다.");


    private final int status;
    private final String errorCode;
    private final String message;
}
