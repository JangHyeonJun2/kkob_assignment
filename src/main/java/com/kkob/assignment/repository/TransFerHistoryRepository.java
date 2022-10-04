package com.kkob.assignment.repository;

import com.kkob.assignment.domain.Account;
import com.kkob.assignment.domain.TransferHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransFerHistoryRepository extends JpaRepository<TransferHistory, Long> {
    List<TransferHistory> findAllBySenderAccountOrderByCreatedAtDesc(Account senderAcc);
}
