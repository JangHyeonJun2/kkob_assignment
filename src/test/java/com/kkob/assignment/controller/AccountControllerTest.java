package com.kkob.assignment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkob.assignment.domain.Account;
import com.kkob.assignment.domain.Address;
import com.kkob.assignment.domain.User;
import com.kkob.assignment.dto.request.KakaoBankTransferMoneyRequest;
import com.kkob.assignment.enums.AccountStatus;
import com.kkob.assignment.enums.Gender;
import com.kkob.assignment.repository.AccountRepository;
import com.kkob.assignment.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class AccountControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

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

    // given : 계좌에 10,000원이 있고, 이체 금액이 2,000원이며, 이체 진행 할 떄 계좌 비밀번호를 입력하고
    // when : 입력한 계좌 비밀번호가 참일 때
    // then : 계좌에는 8,000원이 남게된다.
    @Test
    @DisplayName("이체 정상 테스트")
    void 계좌_비밀번호가_정상일때_이체_테스트() throws Exception {
        //given
        KakaoBankTransferMoneyRequest transferMoneyDTO = new KakaoBankTransferMoneyRequest(receiver, receiverAcc.getAccountNumber(), senderAcc.getAccountNumber(), 2000L, senderAcc.getPassword());

        mockMvc.perform(post("/kkob/send/{senderId}/money", sender.getId())
                        .content(objectMapper.writeValueAsString(transferMoneyDTO))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receiverName").value("라이언"))
                .andExpect(jsonPath("$.amount").value(2000L));

        assertThat(accountRepository.findById(senderAcc.getId()).orElseThrow().getBalance()).isEqualTo(8000);
    }

}