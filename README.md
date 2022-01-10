# CrossSync

번지코드 연습 플러그인입니다.

서버 간 이동 시에 인벤토리 동기화를 위해 사용됩니다.

## 사용하는 것들

1. [버킷 & 번지코드 메시지 채널](https://www.spigotmc.org/wiki/bukkit-bungee-plugin-messaging-channel/)
 - "cross-sync:save" -> 번지코드 서버에서 서버 이동 전 저장할 것을 알릴 때 사용
2. [Redis Pub/Sub](https://redis.io/topics/pubsub)
 - "cross-sync:inv" -> 저장 된 인벤토리를 다른 모든 서버에게 publish (특정 서버 지정할 좋은 방법이 없음...)
 - "cross-sync:invalidate" -> 한번 이상 사용된 인벤토리는 재사용되지 않도록 모든 서버에서 삭제합니다.
3. Redis [Get](https://redis.io/commands/get) / [Set](https://redis.io/commands/set)
 - "cross-sync:ready:[name]" -> 플레이어 인벤토리 저장 가능 여부 확인

## 절차 (첫 서버 접속)

 - 플레이어 번지코드로 접속 시도
 - "cross-sync:ready:[name]" 값을 false 로 설정
 - 플레이어 번지코드 -> A 서버
 - "cross-sync:ready:[name]" 값을 true 로 설정

## 절차 (서버 이동)

 - 플레이어 A 서버 -> B 서버 이동 시작
 - "cross-sync:save" 채널로 A 서버에 저장할 것을 명령
 - "cross-sync:inv" 채널로 저장된 인벤토리 publish
 - B 서버 로그인 이벤트 발생, "cross-sync:inv" 로 저장된 내용 있으면 통과, 없을 경우 저장될 때 까지 wait
 - 플레이어 A 서버 -> B 서버 이동 완료, 저장한 인벤토리를 플레이어에 덮어씌움
 - "cross-sync:invalidate" 채널로 모든 서버에 인벤토리 삭제 명령

## TODO

 - 저장된 인벤토리를 특정 서버에게만 보내는 방법...