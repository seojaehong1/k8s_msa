# 문제 해결 가이드

이 문서는 프로젝트 개발 및 배포 과정에서 발생한 문제들과 해결 방법을 정리한 것입니다.

---

## 📋 목차

1. [빌드 관련 문제](#빌드-관련-문제)
2. [RabbitMQ 연결 문제](#rabbitmq-연결-문제)
3. [JPA 엔티티 관계 문제](#jpa-엔티티-관계-문제)
4. [JSON 직렬화 문제](#json-직렬화-문제)
5. [Kubernetes 배포 문제](#kubernetes-배포-문제)
6. [Docker 빌드 문제](#docker-빌드-문제)

---

## 🔨 빌드 관련 문제

### 문제 1: Java가 설치되지 않음

**증상**:
```
ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
```

**원인**:
- Java가 설치되지 않았거나
- JAVA_HOME 환경 변수가 설정되지 않음

**해결 방법**:

1. **Java 설치 확인**
```bash
java -version
```

2. **JAVA_HOME 설정 (Windows PowerShell)**
```powershell
$env:JAVA_HOME = "C:\util\jdk-17.0.12_windows-x64_bin\jdk-17.0.12"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
```

3. **영구적으로 설정하려면**
- 시스템 환경 변수에 JAVA_HOME 추가
- PATH에 `%JAVA_HOME%\bin` 추가

**교훈**:
- 프로젝트 시작 전 Java 설치 및 환경 변수 설정 확인
- Gradle Wrapper를 사용하면 일부 문제 해결 가능

---

### 문제 2: Gradle 파일 잠금 오류

**증상**:
```
java.io.FileNotFoundException: .gradle\8.8\fileHashes\fileHashes.lock (파일이 삭제되었습니다)
```

**원인**:
- Gradle Daemon이 비정상 종료
- 파일 잠금 문제
- Windows 파일 시스템 이슈

**해결 방법**:

1. **Gradle Daemon 중지**
```bash
./gradlew --stop
```

2. **잠금 파일 삭제**
```powershell
Remove-Item -Path ".gradle\8.8\fileHashes\fileHashes.lock" -Force
```

3. **전체 .gradle 디렉토리 삭제 (최후의 수단)**
```powershell
Remove-Item -Path ".gradle" -Recurse -Force
```

4. **다시 빌드**
```bash
./gradlew build
```

**교훈**:
- 빌드 실패 시 Gradle Daemon 상태 확인
- Windows에서는 파일 잠금 문제가 자주 발생하므로 주의

---

## 🐰 RabbitMQ 연결 문제

### 문제 1: Connection Refused

**증상**:
```
Connection refused: localhost:5672
```

**원인**:
- RabbitMQ가 실행되지 않음
- 호스트 주소가 잘못됨 (Kubernetes 환경)

**해결 방법**:

1. **로컬 환경**
```bash
# RabbitMQ 실행 확인
docker ps | grep rabbitmq

# RabbitMQ 실행
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

2. **Kubernetes 환경**
```properties
# application.properties 수정
spring.rabbitmq.host=rabbitmq  # localhost가 아닌 서비스 이름 사용
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
```

**교훈**:
- 로컬과 Kubernetes 환경의 네트워크 차이 이해
- Kubernetes에서는 서비스 이름으로 통신

---

### 문제 2: ACCESS_REFUSED

**증상**:
```
ACCESS_REFUSED - Login was refused using authentication mechanism PLAIN
```

**원인**:
- 사용자명/비밀번호가 잘못됨
- RabbitMQ 설정이 누락됨

**해결 방법**:

1. **application.properties 확인**
```properties
spring.rabbitmq.host=rabbitmq
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
```

2. **RabbitMQ Deployment 확인**
```yaml
env:
  - name: RABBITMQ_DEFAULT_USER
    value: "guest"
  - name: RABBITMQ_DEFAULT_PASS
    value: "guest"
```

**교훈**:
- 환경 변수와 애플리케이션 설정 일치 확인
- Kubernetes Secret 사용 고려 (프로덕션 환경)

---

### 문제 3: Exchange와 Queue가 Binding되지 않음

**증상**:
- 메시지가 발행되지만 수신되지 않음
- RabbitMQ Management에서 메시지가 쌓임

**원인**:
- Exchange와 Queue가 Binding되지 않음
- Routing Key가 일치하지 않음

**해결 방법**:

1. **RabbitMQConfig에 Binding 추가**
```java
@Bean
public DirectExchange exchange() {
    return new DirectExchange("coffee-shop-exchange");
}

@Bean
public Queue orderCreatedQueue() {
    return new Queue("order.created");
}

@Bean
public Binding bindingOrderCreated(Queue orderCreatedQueue, DirectExchange exchange) {
    return BindingBuilder.bind(orderCreatedQueue)
        .to(exchange)
        .with("order.created");
}
```

2. **RabbitMQ Management에서 확인**
- http://localhost:15672 접속
- Exchanges 탭에서 `coffee-shop-exchange` 확인
- Queues 탭에서 `order.created` 확인
- Bindings 탭에서 연결 확인

**교훈**:
- Producer와 Consumer 모두 Exchange, Queue, Binding 설정 필요
- RabbitMQ Management UI 활용

---

## 💾 JPA 엔티티 관계 문제

### 문제 1: 순환 참조로 인한 500 에러

**증상**:
```
POST /api/orders 요청 시 500 Internal Server Error
```

**원인**:
- Order와 OrderItem 간 양방향 관계
- JSON 직렬화 시 순환 참조 발생

**해결 방법**:

1. **@JsonManagedReference / @JsonBackReference 사용**
```java
// Order.java
@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
@JsonManagedReference
private List<OrderItem> items;

// OrderItem.java
@ManyToOne
@JoinColumn(name = "order_id")
@JsonBackReference
private Order order;
```

2. **또는 @JsonIgnore 사용**
```java
@ManyToOne
@JoinColumn(name = "order_id")
@JsonIgnore
private Order order;
```

**교훈**:
- 양방향 관계 시 JSON 직렬화 주의
- @JsonManagedReference/@JsonBackReference 또는 @JsonIgnore 사용

---

### 문제 2: orphanRemoval 누락

**증상**:
- Order 삭제 시 OrderItem이 삭제되지 않음
- 데이터베이스에 고아 레코드 남음

**원인**:
- `orphanRemoval = true` 설정 누락

**해결 방법**:
```java
@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
private List<OrderItem> items;
```

**교훈**:
- 부모-자식 관계에서 orphanRemoval 설정 중요
- 데이터 정합성 유지

---

## 📦 JSON 직렬화 문제

### 문제 1: LocalDateTime 역직렬화 오류

**증상**:
```
Cannot deserialize value of type `java.time.LocalDateTime` from String
```

**원인**:
- JavaTimeModule이 등록되지 않음
- RabbitMQ MessageConverter에 설정 누락

**해결 방법**:

1. **RabbitMQConfig 수정**
```java
@Bean
public MessageConverter messageConverter() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    return new Jackson2JsonMessageConverter(objectMapper);
}
```

2. **필요한 Import 추가**
```java
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
```

**교훈**:
- LocalDateTime 사용 시 JavaTimeModule 필수
- Producer와 Consumer 모두 동일한 설정 필요

---

## ☸️ Kubernetes 배포 문제

### 문제 1: 서비스 간 통신 실패

**증상**:
- Pod는 정상 실행되지만 서비스 간 통신 실패
- Connection refused 오류

**원인**:
- 서비스 이름으로 통신하지 않음
- Service 리소스가 없음

**해결 방법**:

1. **Service 리소스 확인**
```bash
kubectl get services
```

2. **application.properties 수정**
```properties
# localhost 대신 서비스 이름 사용
eureka.client.service-url.defaultZone=http://eureka-service:8761/eureka/
spring.rabbitmq.host=rabbitmq
```

3. **Deployment에 환경 변수 추가**
```yaml
env:
  - name: EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
    value: "http://eureka-service:8761/eureka/"
```

**교훈**:
- Kubernetes에서는 서비스 이름으로 통신
- 로컬과 Kubernetes 환경 분리

---

### 문제 2: 이미지 Pull 실패

**증상**:
```
Failed to pull image "product-service:1.0": rpc error: code = Unknown desc = ...
```

**원인**:
- 이미지가 로컬에만 존재
- imagePullPolicy 설정 문제

**해결 방법**:

1. **imagePullPolicy 설정**
```yaml
spec:
  containers:
    - name: product-service
      image: product-service:1.0
      imagePullPolicy: Never  # 로컬 이미지 사용
```

2. **또는 이미지를 Registry에 Push**
```bash
docker tag product-service:1.0 registry.example.com/product-service:1.0
docker push registry.example.com/product-service:1.0
```

**교훈**:
- 로컬 개발 시 imagePullPolicy: Never 사용
- 프로덕션에서는 이미지 Registry 사용

---

## 🐳 Docker 빌드 문제

### 문제 1: Multi-stage Build 실패

**증상**:
```
COPY failed: file not found in build context
```

**원인**:
- 빌드 컨텍스트 문제
- 파일 경로 오류

**해결 방법**:

1. **Dockerfile 위치 확인**
- Dockerfile은 프로젝트 루트에 있어야 함

2. **빌드 명령어**
```bash
# 프로젝트 루트에서 실행
docker build -f product-service/Dockerfile -t product-service:1.0 .
```

3. **COPY 경로 확인**
```dockerfile
# 루트의 build.gradle 복사
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle
COPY product-service ./product-service
```

**교훈**:
- Docker 빌드 컨텍스트 이해
- 멀티 프로젝트 구조에서 경로 주의

---

### 문제 2: JAR 파일을 찾을 수 없음

**증상**:
```
COPY failed: stat /build/product-service/build/libs/*.jar: no such file or directory
```

**원인**:
- 빌드가 실패했거나
- JAR 파일이 생성되지 않음

**해결 방법**:

1. **빌드 단계 확인**
```dockerfile
RUN ./gradlew :product-service:build -x test --no-daemon
```

2. **빌드 로그 확인**
```bash
docker build --progress=plain -f product-service/Dockerfile -t product-service:1.0 .
```

3. **로컬에서 먼저 빌드 테스트**
```bash
./gradlew :product-service:build
ls product-service/build/libs/
```

**교훈**:
- Docker 빌드 전 로컬 빌드 확인
- 빌드 로그를 자세히 확인

---

## 📝 일반적인 문제 해결 절차

### 1. 로그 확인
```bash
# Kubernetes Pod 로그
kubectl logs <pod-name>

# Docker 컨테이너 로그
docker logs <container-name>

# 애플리케이션 로그
tail -f logs/application.log
```

### 2. 상태 확인
```bash
# Kubernetes 리소스 상태
kubectl get pods
kubectl get services
kubectl describe pod <pod-name>

# Docker 컨테이너 상태
docker ps
docker inspect <container-name>
```

### 3. 네트워크 확인
```bash
# Kubernetes 서비스 확인
kubectl get svc
kubectl describe svc <service-name>

# 포트 포워딩 테스트
kubectl port-forward <pod-name> 8080:8080
```

### 4. 설정 확인
- application.properties
- Kubernetes Deployment YAML
- Dockerfile
- RabbitMQ 설정

---

## 🎓 교훈 정리

1. **환경 차이 이해**: 로컬과 Kubernetes 환경의 차이
2. **로그 확인**: 문제 발생 시 로그를 먼저 확인
3. **단계별 테스트**: 작은 단위로 나누어 테스트
4. **설정 일관성**: 모든 환경에서 설정 일치 확인
5. **문서화**: 문제 해결 과정을 문서화

---

이 문서는 지속적으로 업데이트됩니다. 새로운 문제를 발견하면 추가해주세요.

