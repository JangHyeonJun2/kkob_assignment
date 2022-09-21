package com.kkob.assignment.domain;

import com.kkob.assignment.enums.TransferStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
public class TransferHistory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long transferAmount;

    @Enumerated(EnumType.STRING)
    private TransferStatus status;

    @ManyToOne
    @JoinColumn(name = "SENDER_ID")
    private User sender;

    @ManyToOne
    @JoinColumn(name = "RECEIVER_ID")
    private User receiver;

    @ManyToOne
    @JoinColumn(name = "SENDERACC_ID")
    private Account senderAccount;

    @ManyToOne
    @JoinColumn(name = "RECEIVERACC_ID")
    private Account receiverAccount;

    public TransferHistory(Long transferAmount, TransferStatus status, User sender, User receiver, Account senderAcc, Account receiverAcc) {
        this.transferAmount = transferAmount;
        this.status = status;
        this.sender = sender;
        this.receiver = receiver;
        this.senderAccount = senderAcc;
        this.receiverAccount = receiverAcc;
    }
}
