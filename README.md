# kkob_assignment
# 카카오뱅크 과제
## 요구사항 
1. 카카오뱅크 모바일 앱의, “카카오톡 친구에게 이체” 프로세스에서 사용되는 백엔드 API 및 기능들을 설계 및 구현하여라.
2. API 설계서
3. API별 시퀀스 다이어그램 
4. ERD & 테이블 설명
5. 위의 ERD를 기반으로, 아래의 데이터를 추출할 수 있는 Query
6. Java & RDBMS로 구현한 소스코드

## 개발환경
- JAVA 11
- IDE: Intelij
- BuildTool : gradle
- Database : Docker / MySQL

### AccountController
```java
@PostMapping(value = "/send/{senderId}/money")
public KakaoBankTransferMoneyResponse sendMoney(@RequestBody @Valid KakaoBankTransferMoneyRequest request,
                                                @PathVariable Long senderId) {
        return redissonLockAccountFacade.sendMoneyFacade(1L, request, senderId);
}
```
- 송금자의 정보는 PathVariable로 받기 때문에 request에는 송금자의 계좌정보만 받고있습니다.

### 동시성 메서드(RedissonLockAccountFacade.java)
```java
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
```
### AccountService
```java
@Transactional
public KakaoBankTransferMoneyResponse sendMoney(KakaoBankTransferMoneyRequest request, Long senderId) {
    User sender = userService.getUser(senderId);
    Account senderAccount = accountRepository.findByAccountNumberAndUser(request.getSenderAccNumber(), sender).orElseThrow();
    Account receiverAccount = accountRepository.findByAccountNumberAndUser(request.getReceiverAccNumber(), request.getReceiver()).orElseThrow();

    senderAccount.checkStatus();

    if (BooleanUtils.isFalse(senderAccount.checkPassword(request.getPassword()))) {
        transferHistoryService.addTransferHistory(new TransferHistory(request.getAmount(), TransferStatus.FAIL, sender, request.getReceiver(), senderAccount, receiverAccount));
        throw new InvalidParameterException("계좌 비밀번호가 잘못되었습니다.");
    }

    if (BooleanUtils.isFalse(senderAccount.checkBalance(request.getAmount()))) {
        transferHistoryService.addTransferHistory(new TransferHistory(request.getAmount(), TransferStatus.FAIL, sender, request.getReceiver(), senderAccount, receiverAccount));
        throw new InvalidParameterException("잔액이 부족합니다.");
    }

    if (BooleanUtils.isFalse(receiverAccount.checkReceiver(request.getReceiver()))) {
        transferHistoryService.addTransferHistory(new TransferHistory(request.getAmount(), TransferStatus.FAIL, sender, request.getReceiver(), senderAccount, receiverAccount));
        throw new InvalidParameterException("받는분이 잘 못되었습니다.");
    }

    senderAccount.decreaseMoney(request.getAmount());
    receiverAccount.increaseMoney(request.getAmount());
    transferHistoryService.addTransferHistory(new TransferHistory(request.getAmount(), TransferStatus.SUCCESS, sender, request.getReceiver(), senderAccount, receiverAccount));
    return new KakaoBankTransferMoneyResponse(sender.getUsername(), request.getAmount());
}
```
총 4가지를 체크하고 이체를 하게 됩니다.
- 계좌 상태 체크
- 계좌 비밀번호 체크
- 잔액 체크
- 수금자 체크
