package com.kkob.assignment.facade;

import com.kkob.assignment.dto.request.KakaoBankTransferMoneyRequest;
import com.kkob.assignment.dto.response.KakaoBankTransferMoneyResponse;
import com.kkob.assignment.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedissonLockAccountFacade {
    private final long WAIT_TIME = 5L;
    private final long LEASE_TIME = 1L;
    private final RedissonClient redissonClient;
    private final  AccountService accountService;

    public KakaoBankTransferMoneyResponse sendMoneyFacade(Long key, KakaoBankTransferMoneyRequest request, Long senderId) {
        RLock lock = redissonClient.getLock(key.toString());

        try {
            boolean available = lock.tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS);
            if (!available) {
                log.info("락 획득 실패");
                return new KakaoBankTransferMoneyResponse(request.getReceiver().getUsername(), 0L);
            }
            return accountService.sendMoney(request, senderId);
        } catch (InterruptedException IE) {
            throw new RuntimeException(IE);
        } finally {
            lock.unlock();
        }
    }
}
