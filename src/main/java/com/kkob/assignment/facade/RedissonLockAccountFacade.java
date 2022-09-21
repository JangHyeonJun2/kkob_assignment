package com.kkob.assignment.facade;

import com.kkob.assignment.dto.request.KakaoBankTransferMoneyRequest;
import com.kkob.assignment.dto.response.KakaoBankTransferMoneyResponse;
import com.kkob.assignment.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedissonLockAccountFacade {
    private final RedissonClient redissonClient;
    private final  AccountService accountService;

    public KakaoBankTransferMoneyResponse sendMoneyFacade(Long key, KakaoBankTransferMoneyRequest request, Long senderId) {
        RLock lock = redissonClient.getLock(key.toString());

        try {
            boolean available = lock.tryLock(5, 1, TimeUnit.SECONDS);
            if (!available) {
                System.out.println("락 획득 실패");
                throw new RuntimeException("서버 문제");
            }
            return accountService.sendMoney(request, senderId);
        } catch (InterruptedException IE) {
            throw new RuntimeException(IE);
        } finally {
            lock.unlock();
        }
    }
}
