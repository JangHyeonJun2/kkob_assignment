package com.kkob.assignment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class KakaoBankTransferMoneyResponse {
    private String senderName;
    private Long amount;
}
