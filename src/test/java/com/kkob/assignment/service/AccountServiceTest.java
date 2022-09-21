package com.kkob.assignment.service;

import com.kkob.assignment.domain.Account;
import com.kkob.assignment.domain.Address;
import com.kkob.assignment.domain.User;
import com.kkob.assignment.dto.request.KakaoBankTransferMoneyRequest;
import com.kkob.assignment.enums.AccountStatus;
import com.kkob.assignment.enums.Gender;
import com.kkob.assignment.facade.RedissonLockAccountFacade;
import com.kkob.assignment.repository.AccountRepository;
import com.kkob.assignment.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
class AccountServiceTest {
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private AccountService accountService;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedissonLockAccountFacade lockAccountFacade;

    private User sender;
    private User receiver;
    private Account senderAcc;
    private Account receiverAcc;

    @BeforeEach
    void init() {
        sender = User.builder()
                .username("장현준")
                .registrationNumber("950312-1876888")
                .gender(Gender.MALE)
                .phoneNumber("010-1234-4578")
                .address(new Address("서울특별시", "관악구", "당곡2가길", "12-78"))
                .build();
        userRepository.saveAndFlush(sender);

        receiver = User.builder()
                .username("라이언")
                .registrationNumber("890312-1876888")
                .gender(Gender.MALE)
                .phoneNumber("010-8888-4578")
                .address(new Address("경기도", "판교", "판교대로", "78"))
                .build();
        userRepository.saveAndFlush(receiver);

        senderAcc = Account.builder()
                .accountNumber("092-1234-232")
                .balance(10000L)
                .bankName("카카오뱅크")
                .status(AccountStatus.AVAILABLE)
                .password("1234")
                .user(sender)
                .build();
        accountRepository.saveAndFlush(senderAcc);

        receiverAcc = Account.builder()
                .accountNumber("79-5878-247")
                .balance(12000L)
                .bankName("카카오뱅크")
                .status(AccountStatus.AVAILABLE)
                .password("4567")
                .user(receiver)
                .build();
        accountRepository.saveAndFlush(receiverAcc);


    }

    @Test
    @DisplayName("계좌 잔액 테스트")
    void 계좌_잔액_테스트() {
        KakaoBankTransferMoneyRequest request = new KakaoBankTransferMoneyRequest(receiver, receiverAcc.getAccountNumber(), senderAcc.getAccountNumber(), 2000L, senderAcc.getPassword());
        accountService.sendMoney(request, sender.getId());

        Account account = accountRepository.findById(senderAcc.getId()).orElseThrow();
        Assertions.assertThat(account.getBalance()).isEqualTo(8000L);
    }

    @Test
    @DisplayName("잔액 동시성 테스트")
    void 잔액_동시성_테스트() throws InterruptedException {
        KakaoBankTransferMoneyRequest request = new KakaoBankTransferMoneyRequest(receiver, receiverAcc.getAccountNumber(), senderAcc.getAccountNumber(), 100L, senderAcc.getPassword());
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);

        for (int i=0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
//                    accountService.sendMoney(request, sender.getId());
                    lockAccountFacade.sendMoneyFacade(1L, request, sender.getId());
                } finally {
                    countDownLatch.countDown();
                }

            });
        }
        countDownLatch.await();

        Account account = accountRepository.findById(senderAcc.getId()).orElseThrow();
        Assertions.assertThat(account.getBalance()).isEqualTo(0L);
    }
}