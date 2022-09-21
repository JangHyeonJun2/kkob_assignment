package com.kkob.assignment.domain;

import com.kkob.assignment.enums.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.security.InvalidParameterException;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Account extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String accountNumber;

    private String bankName;

    private Long balance;

    @Enumerated(EnumType.STRING)
    private AccountStatus status;

    private String password;

    @ManyToOne
    @JoinColumn(name = "USER_ID")
    private User user;

    @Builder
    public Account(String accountNumber, String bankName, Long balance, AccountStatus status, String password, User user) {
        this.accountNumber = accountNumber;
        this.bankName = bankName;
        this.balance = balance;
        this.status = status;
        this.password = password;
        this.user = user;
    }

    public boolean checkPassword(String password) {
        if ( !this.password.equals(password) ) {
            return false;
        }
        return true;
    }

    public void decreaseMoney(Long amount) {
        this.balance -= amount;
    }

    public boolean checkBalance(Long amount) {
        if (this.balance < amount || (this.balance - amount) < 0) {
            return false;
        }
        return true;
    }
    public void increaseMoney(Long amount) {
        this.balance += amount;
    }

    public boolean checkReceiver(User requestReceiver) {
        if ( !this.user.getUsername().equals(requestReceiver.getUsername()) ) {
            return false;
        }
        return true;
    }

    public void checkStatus() {
        if (this.status == AccountStatus.UNAVAILABLE) {
            throw new InvalidParameterException("계좌가 정지된 상태입니다.");
        }
    }
}
