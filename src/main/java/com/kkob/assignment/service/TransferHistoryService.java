package com.kkob.assignment.service;

import com.kkob.assignment.domain.Account;
import com.kkob.assignment.domain.TransferHistory;
import com.kkob.assignment.dto.response.TransferHistoryResponse;
import com.kkob.assignment.repository.TransFerHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class TransferHistoryService {
    private final TransFerHistoryRepository transFerHistoryRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void addTransferHistory(TransferHistory transferHistory) {
        transFerHistoryRepository.save(transferHistory);
    }

    public List<TransferHistoryResponse> getTransferHistoryByAcc(Account senderAcc) {
        return transFerHistoryRepository.findAllBySenderAccountOrderByCreatedAtDesc(senderAcc)
                        .stream()
                        .map(transferHistory ->
                                TransferHistoryResponse.builder()
                                        .transferAmount(transferHistory.getTransferAmount())
                                        .status(transferHistory.getStatus())
                                        .sender(transferHistory.getSender())
                                        .receiver(transferHistory.getReceiver())
                                        .senderAccount(transferHistory.getSenderAccount())
                                        .receiverAccount(transferHistory.getReceiverAccount())
                                        .memo(transferHistory.getMemo())
                                        .createdAt(transferHistory.getCreatedAt())
                                        .build())
                        .collect(Collectors.toList());
    }
}
