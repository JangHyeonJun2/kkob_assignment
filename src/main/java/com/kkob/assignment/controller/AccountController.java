package com.kkob.assignment.controller;

import com.kkob.assignment.dto.request.KakaoBankTransferMoneyRequest;
import com.kkob.assignment.dto.response.KakaoBankTransferMoneyResponse;

import com.kkob.assignment.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/kkob", produces = MediaType.APPLICATION_JSON_VALUE)
public class AccountController {
    private final AccountService accountService;

    @PostMapping(value = "/send/{senderId}/money")
    public KakaoBankTransferMoneyResponse sendMoney(@RequestBody @Valid KakaoBankTransferMoneyRequest request,
                                                    @PathVariable Long senderId) {
            return accountService.sendMoney(request, senderId);
    }
}
