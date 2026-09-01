# SKALA Shop API

상품과 고객을 관리하고, 고객이 보유 포인트로 상품을 주문하거나 주문을 취소할 수 있는 Spring Boot REST API입니다.

## 주요 기능

- 상품 등록, 조회, 수정, 삭제
- 고객 회원가입, 로그인, 조회, 수정, 삭제
- 고객별 주문 상품 조회
- 보유 포인트를 사용한 상품 주문
- 주문 수량의 전체 또는 일부 취소와 포인트 환불
- 요청값 검증 및 공통 오류 응답
- Swagger UI를 통한 API 문서 확인

> 이 프로젝트는 학습용입니다. 비밀번호를 암호화하지 않고 저장하며, 로그인 성공 시 인증 토큰을 발급하지 않습니다. 운영 환경에 그대로 사용하지 마세요.

## 기술 스택

- Java 21
- Spring Boot 4.1.0
- Spring Web MVC
- Spring Data JPA
- Jakarta Validation
- H2 Database
- springdoc-openapi (Swagger UI)
- Gradle Wrapper
- Lombok

## 실행 방법

### 요구 사항

- JDK 21

Gradle은 Wrapper가 포함되어 있어 별도로 설치할 필요가 없습니다.

```bash
java -version
```

### 애플리케이션 실행

macOS/Linux:

```bash
./gradlew bootRun
```

Windows:

```bat
gradlew.bat bootRun
```

기본 포트는 `8080`입니다. 실행 후 상태를 확인합니다.

```bash
curl http://localhost:8080/api/health
```

```json
{
  "application": "skala-shop-api",
  "status": "UP"
}
```

종료하려면 실행 중인 터미널에서 `Ctrl+C`를 누릅니다.

### 테스트 및 빌드

```bash
./gradlew test
./gradlew clean build
```

빌드된 JAR 실행:

```bash
java -jar build/libs/skala-shop-api-0.0.1-SNAPSHOT.jar
```

## API 문서 및 데이터베이스

애플리케이션 실행 후 다음 페이지를 사용할 수 있습니다.

| 항목 | 주소 |
| --- | --- |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| H2 Console | http://localhost:8080/h2-console |

H2 Console 접속 정보:

| 항목 | 값 |
| --- | --- |
| JDBC URL | `jdbc:h2:mem:shopdb` |
| User Name | `sa` |
| Password | 빈 값 |

데이터베이스는 인메모리 방식이며 애플리케이션을 재시작하면 초기화됩니다. 시작할 때 다음 상품이 자동 등록됩니다.

| ID | 상품명 | 가격 |
| ---: | --- | ---: |
| 1 | 무선 마우스 | 15,000 |
| 2 | 블루투스 키보드 | 29,000 |
| 3 | USB 허브 | 39,000 |

## API 목록

### 상태 확인

| Method | Endpoint | 설명 |
| --- | --- | --- |
| `GET` | `/api/health` | 서버 상태 확인 |

### 상품

| Method | Endpoint | 설명 |
| --- | --- | --- |
| `GET` | `/api/products` | 전체 상품 조회 |
| `GET` | `/api/products/{id}` | 상품 단건 조회 |
| `POST` | `/api/products` | 상품 등록 |
| `PUT` | `/api/products/{id}` | 상품 수정 |
| `DELETE` | `/api/products/{id}` | 상품 삭제 |

상품 등록 및 수정 요청:

```json
{
  "name": "노트북 거치대",
  "price": 25000
}
```

- `name`: 필수, 최대 100자
- `price`: 1 이상

### 고객 및 주문

| Method | Endpoint | 설명 |
| --- | --- | --- |
| `GET` | `/api/customers?page=0&size=10` | 고객 목록 조회 |
| `GET` | `/api/customers/{customerId}` | 고객 ID로 조회 |
| `GET` | `/api/customers/name/{customerName}` | 고객 이름으로 조회 |
| `GET` | `/api/customers/{customerId}/products` | 고객의 주문 상품 조회 |
| `POST` | `/api/customers` | 회원가입 |
| `POST` | `/api/customers/login` | 로그인 |
| `PUT` | `/api/customers` | 고객 이름과 포인트 수정 |
| `DELETE` | `/api/customers/{customerId}` | 고객 및 해당 고객의 주문 삭제 |
| `POST` | `/api/customers/order` | 상품 주문 |
| `POST` | `/api/customers/cancel` | 주문 전체 또는 일부 취소 |

고객 목록의 기본 페이지는 `0`, 기본 크기는 `10`입니다. 목록 응답에서는 `orders`가 빈 배열이며, 주문 내역이 필요하면 고객 단건 조회 또는 주문 상품 조회 API를 사용합니다.

## 사용 예시

아래 순서대로 회원가입, 로그인, 주문, 주문 조회, 주문 취소를 테스트할 수 있습니다.

### 1. 회원가입

회원가입 시 `100,000` 포인트가 지급됩니다.

```bash
curl -X POST http://localhost:8080/api/customers \
  -H 'Content-Type: application/json' \
  -d '{
    "customerId": "skala01",
    "name": "홍길동",
    "password": "password123"
  }'
```

```json
{
  "customerId": "skala01",
  "initialPoint": 100000,
  "message": "회원가입이 완료되었습니다."
}
```

회원가입 검증 규칙:

- `customerId`: 필수, 4~20자, 중복 불가
- `name`: 필수, 최대 50자
- `password`: 필수, 8~30자

### 2. 로그인

```bash
curl -X POST http://localhost:8080/api/customers/login \
  -H 'Content-Type: application/json' \
  -d '{
    "customerId": "skala01",
    "password": "password123"
  }'
```

### 3. 상품 주문

`productId`가 `1`인 무선 마우스를 2개 주문하는 예시입니다. 주문 시 `상품 가격 × 수량`만큼 포인트가 차감되며, 주문 당시의 상품 가격이 주문 정보에 저장됩니다.

```bash
curl -X POST http://localhost:8080/api/customers/order \
  -H 'Content-Type: application/json' \
  -d '{
    "customerId": "skala01",
    "productId": 1,
    "quantity": 2
  }'
```

```json
{
  "customerId": "skala01",
  "remainingPoint": 70000,
  "order": {
    "orderId": 1,
    "productId": 1,
    "productName": "무선 마우스",
    "quantity": 2,
    "unitPrice": 15000,
    "totalPrice": 30000
  },
  "message": "상품 주문이 완료되었습니다."
}
```

주문 요청의 `productId`와 `quantity`는 모두 1 이상이어야 하며, 포인트가 부족하면 주문되지 않습니다.

### 4. 주문 내역 조회

```bash
curl http://localhost:8080/api/customers/skala01/products
```

### 5. 주문 일부 취소

동일 상품을 여러 번 주문한 경우 가장 최근 주문부터 취소합니다. 환불액은 현재 상품 가격이 아닌 주문 당시 단가를 기준으로 계산됩니다. 남은 수량이 0이면 주문 항목이 삭제됩니다.

```bash
curl -X POST http://localhost:8080/api/customers/cancel \
  -H 'Content-Type: application/json' \
  -d '{
    "customerId": "skala01",
    "productId": 1,
    "quantity": 1
  }'
```

### 6. 고객 정보 수정

이 API는 이름뿐 아니라 포인트도 요청한 값으로 변경합니다.

```bash
curl -X PUT http://localhost:8080/api/customers \
  -H 'Content-Type: application/json' \
  -d '{
    "customerId": "skala01",
    "name": "김스칼라",
    "point": 120000
  }'
```

## 오류 응답

업무 오류와 요청값 검증 오류는 공통 형식으로 반환됩니다.

```json
{
  "timestamp": "2026-09-01T10:30:00.123456",
  "status": 404,
  "code": "PRODUCT_NOT_FOUND",
  "message": "상품을 찾을 수 없습니다.",
  "path": "/api/products/999"
}
```

| HTTP 상태 | 오류 코드 | 발생 조건 |
| ---: | --- | --- |
| 400 | `INVALID_REQUEST` | 요청값 검증 실패, 잘못된 JSON, 잘못된 페이지 값 |
| 401 | `INVALID_CREDENTIALS` | 고객 ID 또는 비밀번호 불일치 |
| 404 | `PRODUCT_NOT_FOUND` | 존재하지 않는 상품 |
| 404 | `CUSTOMER_NOT_FOUND` | 존재하지 않는 고객 |
| 404 | `ORDER_NOT_FOUND` | 취소할 주문이 없음 |
| 409 | `DUPLICATE_CUSTOMER_ID` | 이미 존재하는 고객 ID |
| 409 | `INSUFFICIENT_FUNDS` | 주문에 필요한 포인트 부족 |
| 409 | `INSUFFICIENT_QUANTITY` | 주문 수량보다 많은 수량 취소 시도 |
| 500 | `INTERNAL_ERROR` | 처리되지 않은 서버 오류 |

## 프로젝트 구조

```text
src
├── main
│   ├── java/com/skala/skala_shop_api
│   │   ├── controller  # HTTP 요청과 응답
│   │   ├── domain      # Customer, Product, OrderItem 엔터티와 저장소
│   │   ├── dto         # API 요청·응답 모델
│   │   ├── exception   # 업무 예외와 공통 예외 처리
│   │   └── service     # 상품·고객·주문 업무 로직
│   └── resources
│       ├── application.yml  # 서버, H2, JPA, 로그 설정
│       └── data.sql         # 초기 상품 데이터
└── test
    └── java             # Spring Context 테스트
```

애플리케이션 로그는 콘솔과 `logs/skala-shop-api.log`에 함께 기록됩니다.
