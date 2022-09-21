package com.kkob.assignment.dto.request;

import com.kkob.assignment.domain.User;
import com.sun.istack.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class KakaoBankTransferMoneyRequest {
    @NotNull
    private User receiver;
    @NotBlank
    private String receiverAccNumber;
    @NotBlank
    private String senderAccNumber;
    @NotNull
    private Long amount;
    @NotBlank
    private String password;
}
