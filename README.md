## 📌 개요

이전 진행했던 빈티지 가구 쇼핑몰 프로젝트의 백엔드 리팩토링 프로젝트입니다.

## 📌 프로젝트 일정

- **프로젝트 진행 기간**: 2024/08/27 ~ 2024/10/31
    - **2024/08/27 ~ 2024/08/30**: 리팩토링 기획, 환경설정, 테이블 설계
    - **2024/08/31 ~ 2024/10/22**: 구현
    - **2024/10/22 ~ 2024/10/31**: 오류 수정, 테스트

## 📌 전제 조건

- 서버 성능 : AWS t3 micro (AWS free tier)
- 상품 데이터 : 800,000 건
- 최대 동시 주문 : 10초 간 2,500 건
- 최대 동시 조회 : 10초간 10,000 건

## 📌 주요 기능

✔ 회원 관리
<details> <summary><b>Spring Security + JWT 를 통한 인증 절차 관리</b></summary>
  - 로그인 시 JWT 토큰이 발급되어, 이후 요청에서 인증 헤더를 통해 사용자를 식별하고 권한을 부여합니다. </details>
✔ 상품 검색
<details> <summary><b>Full Text Search 를 통한 키워드 검색 최적화</b></summary>
  - 키워드 조회 시 Full-Text-Search 방식을 사용하여 like문을 사용한 쿼리보다 <b>약 634% 조회 성능을 개선</b>했습니다. </details>
  <details> <summary><b>Cache Aside 전략을 통한 상품 페이지 캐싱</b></summary> - Redis 캐시 정책을 통해 <b>DB의 부하를 최소화</b>했습니다. </details>
  <details> <summary><b>Cache Write Back 전략을 통한 조회수 관리</b></summary> - 캐시 Write Back 전략을 통해 DB 부하를 최소화했습니다. </details>
✔ 주문 처리
<details> <summary><b>Pessimistic Lock 을 통한 동시성 제어</b></summary>
  - 트랜잭션이 시작될 때 MySQL DB에 Exclusive Lock을 걸어 Race Condition을 해결했습니다. </details>
✔ 장바구니 관리
<details> <summary><b>Redis 를 활용한 장바구니</b></summary>
  - Redis를 독립적인 데이터 저장소로 사용하여 Redis의 빠른 응답 속도를 통해 실시간 데이터를 처리할 수 있습니다.<p></p>
  - <b>Amazon ElastiCache for Redis</b>를 통해 장바구니 데이터의 안정성과 가용성을 유지했습니다. </details>


## 📌 프로젝트 관리

### **✔ 애플리케이션 모니터링**

- Cloud Watch를 사용하여 **로그 + 성능 지표를 모니터링** 하고 있습니다.
- CPU가 70%를 초과하면 알림이 울리는 **경보 프로세스**를 구축했습니다. ( 경보 시 오토 스케일링 동작 )
- 추후 PLG stack 을 추가하여 APM을 강화하고자 합니다.

### **✔ 테스트 커버리지 95% 이상 유지**

- 발생할 수 있는 주요 시나리오에 대해 최대한 대처하고자 했습니다.

## **🎯 트러블 슈팅**

<details> <summary><b>📌 높은 트래픽 상황에도 상품이 클릭될 때마다 조회수 Update 쿼리가 동작했습니다.</b></summary> <p>
<b>❗ 문제상황</b></p>
 <p>- 높은 트래픽이 발생할 때 조회와 함께 발생하는 Update 쿼리는 서버에 큰 무리가 있었습니다.</p>
 <p>- <b>10초간 상품 상세 조회가 1만 회 동작할 때 에러율이 62.31% 발생했습니다.</b></p>
 <p><img src="https://user-images.githubusercontent.com/112923814/207050945-515b7aec-1999-4547-bbba-53dc37670325.png" width="50%" alt="Error rate graph"></p> <p><img src="https://user-images.githubusercontent.com/112923814/207050910-be5d0354-3d3a-4312-9077-b8db909638d2.png" width="50%" alt="High traffic error chart"></p>
 <p><b>💡 Solution : Cache Write Back</b></p> <p>- 조회수를 캐시에 모아 일정 주기 DB에 배치하는 프로세스를 구현했습니다.</p>
 <p>- 조회 기능의 많은 I/O와 함께 발생하는 Update 쿼리를 컨트롤할 수 있었습니다.</p>
 <p>- 싱글쓰레드인 Redis의 특성상 Atomic하게 Increment를 처리할 수 있었습니다.</p>
 <p><b>✔ 결과</b></p> <p>- 클릭 시마다 발생했던 Update 쿼리를 1시간 주기로 일어나는 배치 작업으로 최적화가 이루어졌습니다.</p>
 <p>- <b>동일 상황에서 에러율 0%를 달성했습니다.</b></p>
 <p><img src="https://user-images.githubusercontent.com/112923814/207050998-1e314ddd-4fee-49f4-9b76-157514757c0c.png" width="50%" alt="Optimized error rate graph"></p> <p><img src="https://user-images.githubusercontent.com/112923814/207051036-38937920-808d-4bf0-9414-2a4f4504a93c.png" width="50%" alt="Improved performance chart"></p> </details>
    
<details> <summary><b>📌 조회 쿼리 동작 시 순환 참조(Circular Reference)가 발생하여 성능 이슈가 발생했습니다.</b></summary>
 <p><b>❗ 문제 상황</b></p> <p>- `Orders`와 `Customer` 엔티티 간의 양방향 관계로 인해 순환 참조가 발생했습니다.</p>
 <p>- 데이터 조회 시 순환 참조로 인해 Jackson 라이브러리가 무한 루프에 빠져, `HttpMessageNotWritableException` 예외가 발생하고 응답 시간이 지연되는 성능 문제가 발생했습니다.</p>
 <p><b>💡 Solution</b></p>
 <p>- `Orders` 엔티티에서 `Customer` 필드에 `@JsonIgnore` 어노테이션을 추가하여, 직렬화 시 순환 참조가 발생하지 않도록 설정했습니다.</p>
 <p>- 이를 통해 `Orders` 엔티티가 직렬화될 때 `Customer` 필드가 제외되어 무한 루프를 방지했습니다.</p> 
<p><b>✔ 결과</b></p>
 <p>- `HttpMessageNotWritableException` 예외가 해결되어 정상적으로 데이터가 반환되었습니다.</p>
 <p>- 순환 참조 문제 해결 후 평균 응답 시간이 <b>33%</b> 개선되었습니다.</p> </details>

## 🛠️ 기술 스택

- **백엔드**: Java 17, Spring Boot, Spring Data JPA
- **데이터베이스**: MySQL
- **캐시**: Redis
- **인증**: Spring Security, JWT
- **빌드**: Gradle
