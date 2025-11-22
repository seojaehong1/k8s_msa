# API 문서

이 문서는 각 마이크로서비스의 API 엔드포인트를 설명합니다.

---

## 📋 목차

1. [공통 사항](#공통-사항)
2. [Gateway Service](#gateway-service)
3. [Product Service](#product-service)
4. [Order Service](#order-service)
5. [Board Service](#board-service)
6. [에러 코드](#에러-코드)

---

## 🔧 공통 사항

### Base URL

- **로컬 환경**: `http://localhost:8000` (Gateway를 통한 접근)
- **직접 접근**: 각 서비스의 포트 사용
  - Product Service: `http://localhost:8001`
  - Order Service: `http://localhost:8002`
  - Board Service: `http://localhost:8080`

### 인증

대부분의 API는 JWT 토큰이 필요합니다.

```http
Authorization: Bearer <token>
```

### Content-Type

모든 요청과 응답은 JSON 형식을 사용합니다.

```http
Content-Type: application/json
Accept: application/json
```

---

## 🚪 Gateway Service

### Base URL
```
http://localhost:8000
```

### 인증 API

#### 1. 로그인

**엔드포인트**: `POST /api/auth/login`

**요청**:
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**응답** (200 OK):
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "admin",
  "role": "ADMIN"
}
```

**에러 응답** (401 Unauthorized):
```json
{
  "error": "Invalid credentials"
}
```

**예시**:
```bash
curl -X POST http://localhost:8000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

---

## 📦 Product Service

### Base URL
```
http://localhost:8001/api/products
```

### 1. 상품 목록 조회

**엔드포인트**: `GET /api/products`

**요청**:
```http
GET /api/products HTTP/1.1
Host: localhost:8001
```

**응답** (200 OK):
```json
[
  {
    "id": 1,
    "name": "아메리카노",
    "description": "진한 에스프레소",
    "price": 4000.0,
    "stock": 100,
    "preparationTime": 5,
    "category": {
      "id": 1,
      "name": "커피",
      "type": "BEVERAGE"
    }
  },
  {
    "id": 2,
    "name": "카페라떼",
    "description": "부드러운 우유와 에스프레소",
    "price": 4500.0,
    "stock": 80,
    "preparationTime": 7,
    "category": {
      "id": 1,
      "name": "커피",
      "type": "BEVERAGE"
    }
  }
]
```

**예시**:
```bash
curl http://localhost:8001/api/products
```

---

### 2. 상품 상세 조회

**엔드포인트**: `GET /api/products/{id}`

**경로 변수**:
- `id` (Long): 상품 ID

**요청**:
```http
GET /api/products/1 HTTP/1.1
Host: localhost:8001
```

**응답** (200 OK):
```json
{
  "id": 1,
  "name": "아메리카노",
  "description": "진한 에스프레소",
  "price": 4000.0,
  "stock": 100,
  "preparationTime": 5,
  "category": {
    "id": 1,
    "name": "커피",
    "type": "BEVERAGE",
    "description": "커피 음료"
  }
}
```

**에러 응답** (404 Not Found):
```json
{
  "error": "Product not found"
}
```

**예시**:
```bash
curl http://localhost:8001/api/products/1
```

---

### 3. 상품 생성

**엔드포인트**: `POST /api/products`

**요청 본문**:
```json
{
  "name": "카푸치노",
  "description": "우유 거품이 올라간 커피",
  "price": 4500.0,
  "stock": 50,
  "preparationTime": 8,
  "category": {
    "id": 1
  }
}
```

**응답** (200 OK):
```json
{
  "id": 3,
  "name": "카푸치노",
  "description": "우유 거품이 올라간 커피",
  "price": 4500.0,
  "stock": 50,
  "preparationTime": 8,
  "category": {
    "id": 1,
    "name": "커피",
    "type": "BEVERAGE"
  }
}
```

**예시**:
```bash
curl -X POST http://localhost:8001/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "카푸치노",
    "description": "우유 거품이 올라간 커피",
    "price": 4500.0,
    "stock": 50,
    "preparationTime": 8
  }'
```

---

### 4. 상품 수정

**엔드포인트**: `PUT /api/products/{id}`

**경로 변수**:
- `id` (Long): 상품 ID

**요청 본문**:
```json
{
  "name": "아메리카노 (수정)",
  "description": "수정된 설명",
  "price": 4500.0,
  "stock": 90,
  "preparationTime": 6
}
```

**응답** (200 OK):
```json
{
  "id": 1,
  "name": "아메리카노 (수정)",
  "description": "수정된 설명",
  "price": 4500.0,
  "stock": 90,
  "preparationTime": 6
}
```

**에러 응답** (404 Not Found):
```json
{
  "error": "Product not found"
}
```

**예시**:
```bash
curl -X PUT http://localhost:8001/api/products/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "아메리카노 (수정)",
    "price": 4500.0,
    "stock": 90
  }'
```

---

### 5. 상품 삭제

**엔드포인트**: `DELETE /api/products/{id}`

**경로 변수**:
- `id` (Long): 상품 ID

**요청**:
```http
DELETE /api/products/1 HTTP/1.1
Host: localhost:8001
```

**응답** (200 OK):
```http
HTTP/1.1 200 OK
```

**에러 응답** (404 Not Found):
```json
{
  "error": "Product not found"
}
```

**예시**:
```bash
curl -X DELETE http://localhost:8001/api/products/1
```

---

### 6. 카테고리별 상품 조회

**엔드포인트**: `GET /api/products/category/{categoryId}`

**경로 변수**:
- `categoryId` (Long): 카테고리 ID

**요청**:
```http
GET /api/products/category/1 HTTP/1.1
Host: localhost:8001
```

**응답** (200 OK):
```json
[
  {
    "id": 1,
    "name": "아메리카노",
    "price": 4000.0,
    "stock": 100
  },
  {
    "id": 2,
    "name": "카페라떼",
    "price": 4500.0,
    "stock": 80
  }
]
```

**예시**:
```bash
curl http://localhost:8001/api/products/category/1
```

---

### 7. 재고 있는 상품 조회

**엔드포인트**: `GET /api/products/available`

**요청**:
```http
GET /api/products/available HTTP/1.1
Host: localhost:8001
```

**응답** (200 OK):
```json
[
  {
    "id": 1,
    "name": "아메리카노",
    "stock": 100
  },
  {
    "id": 2,
    "name": "카페라떼",
    "stock": 80
  }
]
```

**예시**:
```bash
curl http://localhost:8001/api/products/available
```

---

### 8. 재고 수정

**엔드포인트**: `PATCH /api/products/{id}/stock`

**경로 변수**:
- `id` (Long): 상품 ID

**요청 본문**:
```json
150
```

**응답** (200 OK):
```json
{
  "id": 1,
  "name": "아메리카노",
  "stock": 150
}
```

**예시**:
```bash
curl -X PATCH http://localhost:8001/api/products/1/stock \
  -H "Content-Type: application/json" \
  -d "150"
```

---

## 🛒 Order Service

### Base URL
```
http://localhost:8002/api/orders
```

### 1. 주문 목록 조회

**엔드포인트**: `GET /api/orders`

**요청**:
```http
GET /api/orders HTTP/1.1
Host: localhost:8002
```

**응답** (200 OK):
```json
[
  {
    "id": 1,
    "customerName": "홍길동",
    "customerEmail": "hong@example.com",
    "orderDate": "2024-01-15T10:30:00",
    "status": "PENDING",
    "totalPrice": 8000.0,
    "items": [
      {
        "id": 1,
        "productId": 1,
        "productName": "아메리카노",
        "quantity": 2,
        "price": 4000.0,
        "preparationTime": 5
      }
    ],
    "estimatedCompletionTime": "2024-01-15T10:35:00"
  }
]
```

**예시**:
```bash
curl http://localhost:8002/api/orders
```

---

### 2. 주문 상세 조회

**엔드포인트**: `GET /api/orders/{id}`

**경로 변수**:
- `id` (Long): 주문 ID

**요청**:
```http
GET /api/orders/1 HTTP/1.1
Host: localhost:8002
```

**응답** (200 OK):
```json
{
  "id": 1,
  "customerName": "홍길동",
  "customerEmail": "hong@example.com",
  "orderDate": "2024-01-15T10:30:00",
  "status": "PENDING",
  "totalPrice": 8000.0,
  "items": [
    {
      "id": 1,
      "productId": 1,
      "productName": "아메리카노",
      "quantity": 2,
      "price": 4000.0,
      "preparationTime": 5
    }
  ],
  "estimatedCompletionTime": "2024-01-15T10:35:00"
}
```

**에러 응답** (404 Not Found):
```json
{
  "error": "Order not found"
}
```

**예시**:
```bash
curl http://localhost:8002/api/orders/1
```

---

### 3. 주문 생성

**엔드포인트**: `POST /api/orders`

**요청 본문**:
```json
{
  "customerName": "홍길동",
  "customerEmail": "hong@example.com",
  "items": [
    {
      "productId": 1,
      "productName": "아메리카노",
      "quantity": 2,
      "price": 4000.0,
      "preparationTime": 5
    },
    {
      "productId": 2,
      "productName": "카페라떼",
      "quantity": 1,
      "price": 4500.0,
      "preparationTime": 7
    }
  ]
}
```

**응답** (200 OK):
```json
{
  "id": 1,
  "customerName": "홍길동",
  "customerEmail": "hong@example.com",
  "orderDate": "2024-01-15T10:30:00",
  "status": "PENDING",
  "totalPrice": 12500.0,
  "items": [
    {
      "id": 1,
      "productId": 1,
      "productName": "아메리카노",
      "quantity": 2,
      "price": 4000.0,
      "preparationTime": 5
    },
    {
      "id": 2,
      "productId": 2,
      "productName": "카페라떼",
      "quantity": 1,
      "price": 4500.0,
      "preparationTime": 7
    }
  ],
  "estimatedCompletionTime": "2024-01-15T10:37:00"
}
```

**주의사항**:
- 주문 생성 시 자동으로 `order.created` 이벤트가 RabbitMQ로 발행됨
- Product Service에서 재고가 자동으로 감소됨

**예시**:
```bash
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

---

### 4. 주문 수정

**엔드포인트**: `PUT /api/orders/{id}`

**경로 변수**:
- `id` (Long): 주문 ID

**요청 본문**:
```json
{
  "customerName": "홍길동 (수정)",
  "customerEmail": "hong2@example.com",
  "status": "PROCESSING"
}
```

**응답** (200 OK):
```json
{
  "id": 1,
  "customerName": "홍길동 (수정)",
  "customerEmail": "hong2@example.com",
  "status": "PROCESSING"
}
```

**에러 응답** (404 Not Found):
```json
{
  "error": "Order not found"
}
```

**예시**:
```bash
curl -X PUT http://localhost:8002/api/orders/1 \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "홍길동 (수정)",
    "status": "PROCESSING"
  }'
```

---

### 5. 주문 삭제

**엔드포인트**: `DELETE /api/orders/{id}`

**경로 변수**:
- `id` (Long): 주문 ID

**요청**:
```http
DELETE /api/orders/1 HTTP/1.1
Host: localhost:8002
```

**응답** (200 OK):
```http
HTTP/1.1 200 OK
```

**에러 응답** (404 Not Found):
```json
{
  "error": "Order not found"
}
```

**예시**:
```bash
curl -X DELETE http://localhost:8002/api/orders/1
```

---

### 6. 매장별 주문 조회

**엔드포인트**: `GET /api/orders/store/{storeId}`

**경로 변수**:
- `storeId` (Long): 매장 ID

**요청**:
```http
GET /api/orders/store/1 HTTP/1.1
Host: localhost:8002
```

**응답** (200 OK):
```json
[
  {
    "id": 1,
    "customerName": "홍길동",
    "storeId": 1,
    "status": "PENDING"
  },
  {
    "id": 2,
    "customerName": "김철수",
    "storeId": 1,
    "status": "COMPLETED"
  }
]
```

**예시**:
```bash
curl http://localhost:8002/api/orders/store/1
```

---

### 7. 주문 상태 변경

**엔드포인트**: `PATCH /api/orders/{id}/status`

**경로 변수**:
- `id` (Long): 주문 ID

**요청 본문**:
```json
"PROCESSING"
```

**가능한 상태 값**:
- `PENDING`: 대기 중
- `PROCESSING`: 처리 중
- `COMPLETED`: 완료
- `CANCELLED`: 취소

**응답** (200 OK):
```json
{
  "id": 1,
  "status": "PROCESSING",
  "customerName": "홍길동"
}
```

**주의사항**:
- 상태 변경 시 `order.status.changed` 이벤트가 RabbitMQ로 발행됨

**예시**:
```bash
curl -X PATCH http://localhost:8002/api/orders/1/status \
  -H "Content-Type: application/json" \
  -d '"PROCESSING"'
```

---

## 📝 Board Service

### Base URL
```
http://localhost:8080/api/boards
```

### 1. 게시글 목록 조회

**엔드포인트**: `GET /api/boards`

**요청**:
```http
GET /api/boards HTTP/1.1
Host: localhost:8080
```

**응답** (200 OK):
```json
[
  {
    "id": 1,
    "title": "게시글 제목",
    "content": "게시글 내용",
    "author": "작성자",
    "createdAt": "2024-01-15T10:30:00"
  }
]
```

---

## ❌ 에러 코드

### HTTP 상태 코드

| 코드 | 의미 | 설명 |
|------|------|------|
| 200 | OK | 요청 성공 |
| 201 | Created | 리소스 생성 성공 |
| 400 | Bad Request | 잘못된 요청 |
| 401 | Unauthorized | 인증 실패 |
| 403 | Forbidden | 권한 없음 |
| 404 | Not Found | 리소스를 찾을 수 없음 |
| 500 | Internal Server Error | 서버 내부 오류 |

### 에러 응답 형식

```json
{
  "error": "에러 메시지",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/products/999"
}
```

### 일반적인 에러

#### 1. 리소스를 찾을 수 없음 (404)

```json
{
  "error": "Product not found"
}
```

**원인**:
- 존재하지 않는 ID로 조회
- 삭제된 리소스 접근

**해결 방법**:
- 올바른 ID 사용
- 리소스 존재 여부 확인

---

#### 2. 잘못된 요청 (400)

```json
{
  "error": "Validation failed",
  "details": [
    {
      "field": "name",
      "message": "상품명은 필수입니다"
    }
  ]
}
```

**원인**:
- 필수 필드 누락
- 잘못된 데이터 형식
- 유효성 검증 실패

**해결 방법**:
- 요청 본문 확인
- 필수 필드 포함
- 데이터 형식 확인

---

#### 3. 인증 실패 (401)

```json
{
  "error": "Invalid credentials"
}
```

**원인**:
- 잘못된 사용자명/비밀번호
- 만료된 토큰
- 토큰 누락

**해결 방법**:
- 로그인하여 새 토큰 발급
- Authorization 헤더 확인

---

#### 4. 서버 오류 (500)

```json
{
  "error": "Internal server error",
  "message": "데이터베이스 연결 실패"
}
```

**원인**:
- 서버 내부 오류
- 데이터베이스 연결 실패
- 예상치 못한 예외

**해결 방법**:
- 서버 로그 확인
- 서비스 상태 확인
- 관리자에게 문의

---

## 📚 추가 리소스

- [Spring Boot REST API 가이드](https://spring.io/guides/tutorials/rest/)
- [HTTP 상태 코드](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status)
- [JSON 스키마](https://json-schema.org/)

---

이 문서는 지속적으로 업데이트됩니다. 새로운 API가 추가되면 문서에 반영해주세요.

