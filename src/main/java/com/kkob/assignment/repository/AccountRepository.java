package com.kkob.assignment.repository;

import com.kkob.assignment.domain.Account;
import com.kkob.assignment.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountNumberAndUser(String accountNumber, User user);
}
