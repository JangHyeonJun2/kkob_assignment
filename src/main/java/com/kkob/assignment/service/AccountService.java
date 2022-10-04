package com.kkob.assignment.service;

import com.kkob.assignment.domain.Account;
import com.kkob.assignment.domain.TransferHistory;
import com.kkob.assignment.domain.User;
import com.kkob.assignment.dto.request.KakaoBankTransferMoneyRequest;
import com.kkob.assignment.dto.response.KakaoBankTransferMoneyResponse;
import com.kkob.assignment.enums.TransferHistoryResult;
import com.kkob.assignment.enums.TransferStatus;
import com.kkob.assignment.enums.exception.ErrorCode;
import com.kkob.assignment.exceptions.AccountException;
import com.kkob.assignment.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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
        Account senderAccount = accountRepository.findByAccountNumberAndUser(request.getSenderAccNumber(), sender)
                                                 .orElseThrow(() -> new AccountException(ErrorCode.NOT_FOUND_ACCOUNT));

        Account receiverAccount = accountRepository.findByAccountNumberAndUser(request.getReceiverAccNumber(), request.getReceiver())
                                                   .orElseThrow(() -> new AccountException(ErrorCode.NOT_FOUND_ACCOUNT));

        senderAccount.checkStatus();

        if (BooleanUtils.isFalse(senderAccount.checkPassword(request.getPassword()))) {
            transferHistoryService.addTransferHistory(new TransferHistory(request.getAmount(), TransferStatus.FAIL, sender, request.getReceiver(), senderAccount, receiverAccount, TransferHistoryResult.INVALID_PASSWORD));
            throw new AccountException(ErrorCode.INVALID_PASSWORD);
        }

        if (BooleanUtils.isFalse(senderAccount.checkBalance(request.getAmount()))) {
            transferHistoryService.addTransferHistory(new TransferHistory(request.getAmount(), TransferStatus.FAIL, sender, request.getReceiver(), senderAccount, receiverAccount, TransferHistoryResult.LACK_BALANCE));
            throw new AccountException(ErrorCode.LACK_OF_BALANCE);
        }

        if (BooleanUtils.isFalse(receiverAccount.checkReceiver(request.getReceiver()))) {
            transferHistoryService.addTransferHistory(new TransferHistory(request.getAmount(), TransferStatus.FAIL, sender, request.getReceiver(), senderAccount, receiverAccount, TransferHistoryResult.INVALID_RECEIVER));
            throw new AccountException(ErrorCode.INVALID_RECEIVER);
        }

        senderAccount.decreaseMoney(request.getAmount());
        receiverAccount.increaseMoney(request.getAmount());
        transferHistoryService.addTransferHistory(new TransferHistory(request.getAmount(), TransferStatus.SUCCESS, sender, request.getReceiver(), senderAccount, receiverAccount, TransferHistoryResult.COMPLETE_TRANSFER));
        return new KakaoBankTransferMoneyResponse(request.getReceiver().getUsername(), request.getAmount());
    }
}
