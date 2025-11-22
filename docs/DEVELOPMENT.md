# 개발 환경 설정 가이드

이 문서는 프로젝트를 로컬에서 개발하고 배포하는 방법을 설명합니다.

---

## 📋 목차

1. [사전 요구사항](#사전-요구사항)
2. [로컬 개발 환경 설정](#로컬-개발-환경-설정)
3. [Docker 이미지 빌드](#docker-이미지-빌드)
4. [Kubernetes 배포](#kubernetes-배포)
5. [테스트 방법](#테스트-방법)

---

## 🔧 사전 요구사항

### 필수 소프트웨어

1. **JDK 17 이상**
```bash
java -version
# java version "17.0.12" 이상
```

2. **Gradle 8.8 이상** (또는 Gradle Wrapper 사용)
```bash
./gradlew --version
```

3. **Docker** (컨테이너 빌드 및 RabbitMQ 실행용)
```bash
docker --version
docker-compose --version
```

4. **Kubernetes** (배포용, 선택사항)
```bash
kubectl version
minikube version  # 또는 다른 Kubernetes 클러스터
```

5. **Git**
```bash
git --version
```

### 선택 소프트웨어

- **IDE**: IntelliJ IDEA, Eclipse, VS Code
- **Postman** 또는 **curl**: API 테스트
- **RabbitMQ Management UI**: 메시지 큐 모니터링

---

## 💻 로컬 개발 환경 설정

### 1. 프로젝트 클론

```bash
git clone <repository-url>
cd k8s_msa
```

### 2. 환경 변수 설정

#### Windows (PowerShell)
```powershell
# Java 경로 설정
$env:JAVA_HOME = "C:\util\jdk-17.0.12_windows-x64_bin\jdk-17.0.12"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

# 확인
java -version
```

#### Linux/macOS
```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
export PATH=$JAVA_HOME/bin:$PATH

# 확인
java -version
```

### 3. RabbitMQ 실행

#### Docker 사용 (권장)
```bash
docker run -d \
  --name rabbitmq \
  -p 5672:5672 \
  -p 15672:15672 \
  -e RABBITMQ_DEFAULT_USER=guest \
  -e RABBITMQ_DEFAULT_PASS=guest \
  rabbitmq:3-management
```

#### 접속 확인
- Management UI: http://localhost:15672 (guest/guest)
- AMQP: localhost:5672

### 4. 서비스 실행 순서

#### 1단계: Eureka Server 실행
```bash
cd eureka-server
../gradlew bootRun
```

**확인**: http://localhost:8761

#### 2단계: Product Service 실행 (새 터미널)
```bash
cd product-service
../gradlew bootRun
```

**확인**: http://localhost:8001/api/products

#### 3단계: Order Service 실행 (새 터미널)
```bash
cd order-service
../gradlew bootRun
```

**확인**: http://localhost:8002/api/orders

#### 4단계: Gateway Service 실행 (새 터미널)
```bash
cd gateway-service
../gradlew bootRun
```

**확인**: http://localhost:8000

### 5. 서비스 등록 확인

1. Eureka Dashboard 접속: http://localhost:8761
2. Instances currently registered with Eureka에서 서비스 확인
3. 다음 서비스들이 등록되어야 함:
   - PRODUCT-SERVICE
   - ORDER-SERVICE
   - GATEWAY-SERVICE

### 6. 프론트엔드 실행 (선택사항)

```bash
cd gateway-ui
npm install
npm run dev
```

---

## 🐳 Docker 이미지 빌드

### 1. 멀티 스테이지 빌드 이해

각 서비스의 Dockerfile은 두 단계로 구성됩니다:

1. **Build Stage**: Gradle을 사용하여 소스 코드 빌드
2. **Runtime Stage**: 빌드된 JAR 파일만 포함하는 경량 이미지

### 2. 이미지 빌드 방법

#### Product Service 빌드
```bash
# 프로젝트 루트에서 실행
docker build -f product-service/Dockerfile -t product-service:1.0 .
```

#### Order Service 빌드
```bash
docker build -f order-service/Dockerfile -t order-service:1.0 .
```

#### Gateway Service 빌드
```bash
docker build -f gateway-service/Dockerfile -t gateway-service:1.0 .
```

#### Eureka Server 빌드
```bash
docker build -f eureka-server/Dockerfile -t eureka-server:1.0 .
```

### 3. 빌드 과정 설명

```dockerfile
# Stage 1: Build
FROM gradle:8.8-jdk17 AS builder
WORKDIR /build

# 루트 빌드 파일 복사
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# 서비스 소스 코드 복사
COPY product-service ./product-service

# 빌드 실행
RUN ./gradlew :product-service:build -x test --no-daemon

# Stage 2: Runtime
FROM amazoncorretto:17
WORKDIR /app

# 빌드된 JAR 파일만 복사
COPY --from=builder /build/product-service/build/libs/*.jar app.jar

EXPOSE 8001
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 4. 빌드 최적화

#### Docker BuildKit 사용
```bash
DOCKER_BUILDKIT=1 docker build -f product-service/Dockerfile -t product-service:1.0 .
```

#### 캐시 활용
- Docker는 각 레이어를 캐시
- 자주 변경되지 않는 파일을 먼저 COPY
- 의존성 파일을 별도로 COPY하여 캐시 효율성 향상

### 5. 이미지 확인

```bash
# 이미지 목록 확인
docker images

# 이미지 상세 정보
docker inspect product-service:1.0

# 이미지 크기 확인
docker images | grep product-service
```

### 6. 로컬에서 이미지 테스트

```bash
# 컨테이너 실행
docker run -d \
  --name product-service \
  -p 8001:8001 \
  -e EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://host.docker.internal:8761/eureka/ \
  product-service:1.0

# 로그 확인
docker logs product-service

# 컨테이너 중지 및 삭제
docker stop product-service
docker rm product-service
```

---

## ☸️ Kubernetes 배포

### 1. Kubernetes 클러스터 준비

#### Minikube 사용 (로컬 개발)
```bash
# Minikube 시작
minikube start

# 클러스터 확인
kubectl cluster-info
```

#### 다른 Kubernetes 클러스터
- GKE, EKS, AKS 등
- 또는 로컬 Kubernetes 클러스터

### 2. Docker 이미지 준비

#### Minikube에서 로컬 이미지 사용
```bash
# Minikube Docker 환경 사용
eval $(minikube docker-env)

# 이미지 빌드
docker build -f product-service/Dockerfile -t product-service:1.0 .

# Minikube Docker 환경 해제
eval $(minikube docker-env -u)
```

#### 또는 이미지 Registry 사용
```bash
# 이미지 태그
docker tag product-service:1.0 registry.example.com/product-service:1.0

# 이미지 Push
docker push registry.example.com/product-service:1.0

# Deployment YAML에서 imagePullPolicy 제거 또는 IfNotPresent로 변경
```

### 3. 배포 순서

#### 1단계: RabbitMQ 배포
```bash
kubectl apply -f k8s/rabbitmq-deployment.yaml
```

**확인**:
```bash
kubectl get pods -l app=rabbitmq
kubectl get svc rabbitmq
```

#### 2단계: Eureka Server 배포
```bash
kubectl apply -f k8s/eureka-deployment.yaml
```

**확인**:
```bash
kubectl get pods -l app=eureka-server
kubectl get svc eureka-service

# 포트 포워딩으로 접속
kubectl port-forward svc/eureka-service 8761:8761
# http://localhost:8761 접속
```

#### 3단계: Product Service 배포
```bash
kubectl apply -f k8s/product-deployment.yaml
```

**확인**:
```bash
kubectl get pods -l app=product-service
kubectl get svc product-service
```

#### 4단계: Order Service 배포
```bash
kubectl apply -f k8s/order-deployment.yaml
```

**확인**:
```bash
kubectl get pods -l app=order-service
kubectl get svc order-service
kubectl get hpa order-service-hpa
```

#### 5단계: Gateway Service 배포
```bash
kubectl apply -f k8s/gateway-deployment.yaml
```

**확인**:
```bash
kubectl get pods -l app=gateway-service
kubectl get svc gateway-service
```

### 4. 배포 상태 확인

```bash
# 모든 Pod 상태 확인
kubectl get pods

# 특정 Pod 상세 정보
kubectl describe pod <pod-name>

# Pod 로그 확인
kubectl logs <pod-name>

# 서비스 확인
kubectl get svc

# HPA 상태 확인
kubectl get hpa
```

### 5. 접속 방법

#### Gateway Service 접속
```bash
# LoadBalancer 타입인 경우
kubectl get svc gateway-service

# NodePort 타입인 경우
kubectl port-forward svc/gateway-service 8000:8080
# http://localhost:8000 접속
```

#### Minikube에서 접속
```bash
# Minikube 서비스 URL 확인
minikube service gateway-service --url
```

### 6. 스케일링

#### 수동 스케일링
```bash
kubectl scale deployment order-service --replicas=5
```

#### 자동 스케일링 (HPA)
- Order Service는 HPA가 설정되어 있음
- CPU 사용률 70% 기준으로 2-10개 Pod 자동 조정

```bash
# HPA 상태 확인
kubectl get hpa order-service-hpa

# HPA 상세 정보
kubectl describe hpa order-service-hpa
```

### 7. 롤링 업데이트

```bash
# 새 이미지로 업데이트
kubectl set image deployment/product-service product-service=product-service:1.1

# 업데이트 상태 확인
kubectl rollout status deployment/product-service

# 롤백
kubectl rollout undo deployment/product-service
```

---

## 🧪 테스트 방법

### 1. 단위 테스트

```bash
# 모든 서비스 테스트
./gradlew test

# 특정 서비스 테스트
./gradlew :product-service:test
```

### 2. 통합 테스트

#### API 테스트 (Postman 또는 curl)

**Product Service 테스트**:
```bash
# 상품 목록 조회
curl http://localhost:8001/api/products

# 상품 생성
curl -X POST http://localhost:8001/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "아메리카노",
    "description": "진한 에스프레소",
    "price": 4000.0,
    "stock": 100,
    "preparationTime": 5
  }'
```

**Order Service 테스트**:
```bash
# 주문 생성
curl -X POST http://localhost:8002/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "홍길동",
    "customerEmail": "hong@example.com",
    "items": [
      {
        "productId": 1,
        "productName": "아메리카노",
        "quantity": 2,
        "price": 4000.0,
        "preparationTime": 5
      }
    ]
  }'
```

### 3. RabbitMQ 메시지 테스트

#### RabbitMQ Management UI 사용
1. http://localhost:15672 접속 (guest/guest)
2. Queues 탭에서 `order.created` 확인
3. 주문 생성 후 메시지 확인
4. Product Service에서 재고 감소 확인

#### 로그 확인
```bash
# Order Service 로그
# "주문 생성 이벤트 발행" 메시지 확인

# Product Service 로그
# "주문 생성 이벤트 수신" 및 "재고 감소" 메시지 확인
```

### 4. E2E 테스트

#### 시나리오: 주문 생성 → 재고 감소

1. **상품 생성**
```bash
curl -X POST http://localhost:8000/api/products \
  -H "Content-Type: application/json" \
  -d '{"name": "아메리카노", "price": 4000.0, "stock": 100}'
```

2. **재고 확인**
```bash
curl http://localhost:8000/api/products/1
# stock: 100 확인
```

3. **주문 생성**
```bash
curl -X POST http://localhost:8000/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "홍길동",
    "items": [{"productId": 1, "quantity": 2, "price": 4000.0}]
  }'
```

4. **재고 확인 (감소 확인)**
```bash
curl http://localhost:8000/api/products/1
# stock: 98 확인 (100 - 2)
```

### 5. 부하 테스트

#### Apache Bench 사용
```bash
# 1000 요청, 동시 10개
ab -n 1000 -c 10 http://localhost:8000/api/products
```

#### JMeter 사용
- JMeter 스크립트 작성
- 다양한 시나리오 테스트

---

## 🔍 디버깅 팁

### 1. 로그 확인

#### 애플리케이션 로그
```bash
# 로컬
tail -f logs/application.log

# Kubernetes
kubectl logs -f <pod-name>
```

#### 여러 Pod 로그 동시 확인
```bash
kubectl logs -f -l app=order-service
```

### 2. Pod 내부 접속

```bash
# Pod 내부 쉘 접속
kubectl exec -it <pod-name> -- /bin/sh

# 명령어 실행
kubectl exec <pod-name> -- java -version
```

### 3. 네트워크 디버깅

```bash
# 서비스 엔드포인트 확인
kubectl get endpoints

# 포트 포워딩
kubectl port-forward svc/product-service 8001:8080
```

### 4. 리소스 사용량 확인

```bash
# Pod 리소스 사용량
kubectl top pods

# 노드 리소스 사용량
kubectl top nodes
```

---

## 📚 추가 리소스

- [Spring Boot 공식 문서](https://spring.io/projects/spring-boot)
- [Kubernetes 공식 문서](https://kubernetes.io/docs/)
- [RabbitMQ 공식 문서](https://www.rabbitmq.com/documentation.html)
- [Docker 공식 문서](https://docs.docker.com/)

---

이 문서는 지속적으로 업데이트됩니다. 새로운 방법이나 개선 사항을 발견하면 추가해주세요.

