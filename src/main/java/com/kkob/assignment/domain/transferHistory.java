package com.kkob.assignment.domain;

import com.kkob.assignment.enums.TransferStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@NoArgsConstructor
@Getter
@Entity
public class transferHistory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private Long transferAmount;

    @Enumerated(EnumType.STRING)
    private TransferStatus status;

    private String TransferMemo;

    @ManyToOne
    @JoinColumn(name = "SENDER_ID")
    private User sender;

    @ManyToOne
    @JoinColumn(name = "RECEIVER_ID")
    private User receiver;

    @ManyToOne
    @JoinColumn(name = "ACCOUNT_ID")
    private Account account;
}
