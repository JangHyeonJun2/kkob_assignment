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

### 왜 이체하는 메서드를 동기로 하였나?

- 사실 이 부분에서 정말 많은 고민을 했습니다. 처음 과제를 받았을 때는 비동기로 이체 메서드를 구현하자고 생각했습니다. 왜냐면 카카오뱅크만 해도 대한민국에서 정말 많은 이체, 송금 등등을 하기 때문에 이체를 동기로 했을 때는 트랙픽이 몰려 락이 걸려서 시스템이 다운된다거나 아니면 스레드가 부족한 현상이 나지 않을까? 라는 생각이었습니다.
- 하지만 현재 이체 메서드는 동기 메서드로 되어있는데요. 그 이유는 만약 비동기로 했을 때 다른 은행서버가 응답값을 주지 않는다거나 이체에 대한 정보가 유실되는 문제가 있을 수 도 있고, 금액에 대한 적합성도 동기가 훨씬 안정적이지 않을까 생각했기 때문에 최종적으로 동기로 구현하였습니다.

### 주요 쟁점으로 생각 한 부분

- 나의 계좌에서 정기예금 또는 자동이체와 같이 여러 이체들이 하나의 계좌에서 금액을 건드리기 때문에 동시성 문제를 해결 해야했습니다.
    - 처음에는 Spring Boot Data JPA 에서 제공하는 Pessimistic Lock을 사용하여 동시성 문제를 해결했지만 이체에서는 성능이 중요하다고 판단했기 때문에 Redis Lock을 사용하였습니다.
    - Redis Lock에서 Redisson 라이브러리를 사용하였습니다.
        - Redisson을 사용한 이유
            1. 락 획득 재시도를 기본으로 제공하기 때문에 사용이 다른 Redis Lock 보다는 편리하였습니다.
            2. Pub-Sub 방식으로 구현이 되어있기 때문에 redis에 부하가 덜 하다는 장점도 있었습니다.
            3. 만약 이체를 실패하게 되었을 때 재시도를 해야해서 다시 이체를 시도해야합니다. 그래서 위에서도 말씀드렸듯이 Redisson 은 기본으로 제공하고 있습니다.

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
