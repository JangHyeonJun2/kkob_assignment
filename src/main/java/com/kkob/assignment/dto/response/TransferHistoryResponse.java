package com.kkob.assignment.dto.response;

import com.kkob.assignment.domain.Account;
import com.kkob.assignment.domain.User;
import com.kkob.assignment.enums.TransferHistoryResult;
import com.kkob.assignment.enums.TransferStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
public class TransferHistoryResponse {
    private Long transferAmount;
    private TransferStatus status;
    private User sender;
    private User receiver;
    private Account senderAccount;
    private Account receiverAccount;
    private TransferHistoryResult memo;
    private LocalDateTime createdAt;
}
