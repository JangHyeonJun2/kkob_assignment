package com.kkob.assignment.domain;

import com.kkob.assignment.enums.AccountStatus;

import javax.persistence.*;

@Entity
public class Account extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String accountNumber;

    private String bankName;

    private Long balance;

    @Enumerated(EnumType.STRING)
    private AccountStatus status;

    private String password;

    @ManyToOne
    @JoinColumn(name = "USER_ID")
    private User user;
}
