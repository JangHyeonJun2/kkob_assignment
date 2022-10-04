package com.kkob.assignment.domain;

import com.kkob.assignment.enums.TransferHistoryResult;
import com.kkob.assignment.enums.TransferStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.Objects;

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

    @Enumerated(EnumType.STRING)
    private TransferHistoryResult memo;

    public TransferHistory(Long transferAmount, TransferStatus status, User sender, User receiver, Account senderAcc, Account receiverAcc, TransferHistoryResult result) {
        this.transferAmount = transferAmount;
        this.status = status;
        this.sender = sender;
        this.receiver = receiver;
        this.senderAccount = senderAcc;
        this.receiverAccount = receiverAcc;
        this.memo = result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        TransferHistory that = (TransferHistory) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
