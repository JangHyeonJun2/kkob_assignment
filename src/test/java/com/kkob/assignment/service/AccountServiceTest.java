package com.kkob.assignment.service;

import com.kkob.assignment.domain.Account;
import com.kkob.assignment.domain.Address;
import com.kkob.assignment.domain.User;
import com.kkob.assignment.dto.request.KakaoBankTransferMoneyRequest;
import com.kkob.assignment.dto.response.TransferHistoryResponse;
import com.kkob.assignment.enums.AccountStatus;
import com.kkob.assignment.enums.Gender;
import com.kkob.assignment.enums.exception.ErrorCode;
import com.kkob.assignment.exceptions.AccountException;
import com.kkob.assignment.facade.RedissonLockAccountFacade;
import com.kkob.assignment.repository.AccountRepository;
import com.kkob.assignment.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
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
    private TransferHistoryService transferHistoryService;

    @Autowired
    private RedissonLockAccountFacade lockAccountFacade;

    private User sender;
    private User receiver;

    @BeforeEach
    void init() {
        sender = 유저_생성("장현준", "950312-1876888", "010-1234-4578", "서울특별시", "관악구", "당곡2가길", "12-78");
        receiver = 유저_생성("라이언", "890312-1876888", "010-8888-4578", "경기도", "판교", "판교대로", "78");
    }


    @Test
    @DisplayName("계좌 잔액 테스트")
    void 계좌_잔액_테스트() {
        Account senderAcc = 계좌_생성("091-1234-232", 10000L, "카카오뱅크", "1234", sender);
        Account receiverAcc = 계좌_생성("791-5878-247", 12000L, "카카오뱅크", "4567", receiver);

        KakaoBankTransferMoneyRequest request = new KakaoBankTransferMoneyRequest(receiver, receiverAcc.getAccountNumber(), senderAcc.getAccountNumber(), 2000L, senderAcc.getPassword());
        accountService.sendMoney(request, sender.getId());

        Account account = accountRepository.findById(senderAcc.getId()).orElseThrow();
        Assertions.assertThat(account.getBalance()).isEqualTo(8000L);
    }

    @Test
    @DisplayName("잔액 동시성 테스트")
    void 잔액_동시성_테스트() throws InterruptedException {
        Account senderAcc = 계좌_생성("092-1234-232", 10000L, "카카오뱅크", "1234", sender);
        Account receiverAcc = 계좌_생성("792-5878-247", 12000L, "카카오뱅크", "4567", receiver);


        KakaoBankTransferMoneyRequest request = new KakaoBankTransferMoneyRequest(receiver, receiverAcc.getAccountNumber(), senderAcc.getAccountNumber(), 100L, senderAcc.getPassword());
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);

        for (int i=0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    lockAccountFacade.sendMoneyFacade(request, sender.getId());
                } finally {
                    countDownLatch.countDown();
                }

            });
        }
        countDownLatch.await();

        Account account = accountRepository.findById(senderAcc.getId()).orElseThrow();
        Assertions.assertThat(account.getBalance()).isEqualTo(0L);
    }

    // given : 계좌에 10,000원이 있고, 이체금액이 2,000원이며,
    // when : 잘못된 계좌 비밀번호를 입력해서 이체를 진행할 때
    // then : InvalidParameterException 발생하지만, transferHistory 에 데이터는 저장된다.
    @Test
    @DisplayName("잘못된 비밀번호 입력했을 때 이체이력 히스토리 저장확인 테스트")
    void 잘못된_비밀번호_입력했을때_이체이력_생성_테스트() {
        Account senderAcc = 계좌_생성("093-1234-232", 10000L, "카카오뱅크", "1234", sender);
        Account receiverAcc = 계좌_생성("793-5878-247", 12000L, "카카오뱅크", "4567", receiver);

        //given
        KakaoBankTransferMoneyRequest transferMoneyDTO = new KakaoBankTransferMoneyRequest(receiver, receiverAcc.getAccountNumber(), senderAcc.getAccountNumber(), 2000L, "4444");

        //when
        //then
        Assertions.assertThatThrownBy(() -> accountService.sendMoney(transferMoneyDTO, sender.getId()))
                .isInstanceOf(AccountException.class)
                .hasMessage(ErrorCode.INVALID_PASSWORD.getMessage());

        List<TransferHistoryResponse> transferHistoryByAcc = transferHistoryService.getTransferHistoryByAcc(senderAcc);
        Assertions.assertThat(transferHistoryByAcc.size()).isEqualTo(1);
    }

    // given : 계좌에 1,000원이 있고, 이체금액이 2,000원이며,
    // when : 게좌에 잔액이 부족할 때
    // then : InvalidParameterException 발생하지만, transferHistory 에 데이터는 저장된다.
    @Test
    @DisplayName("계좌 잔액이 부족할 때 이체이력 히스토리 저장확인 테스트")
    void 잔액부족시_이체이력_히스토리_생성_테스트() {
        //given
        Account senderAcc = 계좌_생성("094-1234-232", 10000L, "카카오뱅크", "1234", sender);
        Account receiverAcc = 계좌_생성("794-5878-247", 12000L, "카카오뱅크", "4567", receiver);

        Account anotherAcc = Account.builder()
                .accountNumber("095-1234-232")
                .balance(1000L)
                .bankName("신한은행")
                .status(AccountStatus.AVAILABLE)
                .password("1234")
                .user(sender)
                .build();
        accountRepository.saveAndFlush(anotherAcc);

        KakaoBankTransferMoneyRequest transferMoneyDTO = new KakaoBankTransferMoneyRequest(receiver, receiverAcc.getAccountNumber(), anotherAcc.getAccountNumber(), 2000L, "1234");

        Assertions.assertThatThrownBy(() -> accountService.sendMoney(transferMoneyDTO, sender.getId()))
                .isInstanceOf(AccountException.class)
                .hasMessage(ErrorCode.LACK_OF_BALANCE.getMessage());
    }

    private User 유저_생성(String username, String registrationNumber, String phoneNumber, String 서울특별시, String 관악구, String 당곡2가길, String zipCode) {
        User user = User.builder()
                .username(username)
                .registrationNumber(registrationNumber)
                .gender(Gender.MALE)
                .phoneNumber(phoneNumber)
                .address(new Address(서울특별시, 관악구, 당곡2가길, zipCode))
                .build();
        return userRepository.saveAndFlush(user);
    }

    private Account 계좌_생성(String accountNumber, long balance, String bankName, String password, User sender) {
        Account acc = Account.builder()
                .accountNumber(accountNumber)
                .balance(balance)
                .bankName(bankName)
                .status(AccountStatus.AVAILABLE)
                .password(password)
                .user(sender)
                .build();
        return accountRepository.saveAndFlush(acc);
    }
}