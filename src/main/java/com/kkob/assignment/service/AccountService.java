package com.kkob.assignment.service;

import com.kkob.assignment.domain.Account;
import com.kkob.assignment.domain.TransferHistory;
import com.kkob.assignment.domain.User;
import com.kkob.assignment.dto.request.KakaoBankTransferMoneyRequest;
import com.kkob.assignment.dto.response.KakaoBankTransferMoneyResponse;
import com.kkob.assignment.enums.TransferStatus;
import com.kkob.assignment.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.InvalidParameterException;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final UserService userService;
    private final TransferHistoryService transferHistoryService;


    @Transactional
    public KakaoBankTransferMoneyResponse sendMoney(KakaoBankTransferMoneyRequest request, Long senderId) {
        User sender = userService.getUser(senderId);
        Account senderAccount = accountRepository.findByAccountNumberAndUser(request.getSenderAccNumber(), sender).orElseThrow();
        Account receiverAccount = accountRepository.findByAccountNumberAndUser(request.getReceiverAccNumber(), request.getReceiver()).orElseThrow();

        senderAccount.checkStatus();

        if (BooleanUtils.isFalse(senderAccount.checkPassword(request.getPassword()))) {
            transferHistoryService.addTransferHistory(new TransferHistory(request.getAmount(), TransferStatus.FAIL, sender, request.getReceiver(), senderAccount, receiverAccount));
            throw new InvalidParameterException("계좌 비밀번호가 잘못되었습니다.");
        }

        if (BooleanUtils.isFalse(senderAccount.checkBalance(request.getAmount()))) {
            transferHistoryService.addTransferHistory(new TransferHistory(request.getAmount(), TransferStatus.FAIL, sender, request.getReceiver(), senderAccount, receiverAccount));
            throw new InvalidParameterException("잔액이 부족합니다.");
        }

        if (BooleanUtils.isFalse(receiverAccount.checkReceiver(request.getReceiver()))) {
            transferHistoryService.addTransferHistory(new TransferHistory(request.getAmount(), TransferStatus.FAIL, sender, request.getReceiver(), senderAccount, receiverAccount));
            throw new InvalidParameterException("받는분이 잘 못되었습니다.");
        }

        senderAccount.decreaseMoney(request.getAmount());
        receiverAccount.increaseMoney(request.getAmount());
        transferHistoryService.addTransferHistory(new TransferHistory(request.getAmount(), TransferStatus.SUCCESS, sender, request.getReceiver(), senderAccount, receiverAccount));
        return new KakaoBankTransferMoneyResponse(sender.getUsername(), request.getAmount());
    }
}
