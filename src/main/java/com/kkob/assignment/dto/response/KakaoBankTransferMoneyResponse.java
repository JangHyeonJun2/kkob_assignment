package com.kkob.assignment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
public class KakaoBankTransferMoneyResponse {
    private String receiverName;
    private Long amount;
}
