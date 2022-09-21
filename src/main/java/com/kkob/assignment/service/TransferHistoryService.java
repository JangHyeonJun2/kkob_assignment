package com.kkob.assignment.service;

import com.kkob.assignment.domain.TransferHistory;
import com.kkob.assignment.repository.TransFerHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TransferHistoryService {
    private final TransFerHistoryRepository transFerHistoryRepository;

    public void addTransferHistory(TransferHistory transferHistory) {
        transFerHistoryRepository.save(transferHistory);
    }
}
