# Hotel Booking AI - Microservice Architecture

This repository contains the decomposed **Microservice Architecture** refactored from the monolithic Hotel Booking AI platform.

## Architecture Overview

```
                          +-----------------------------+
                          |       Client / Frontend     |
                          +--------------+--------------+
                                         | (HTTP / REST)
                                         v
                          +-----------------------------+
                          |      API Gateway (8080)     |
                          | (JWT Auth, Rate Limit, CORS)|
                          +-------+--------------+------+
                                  |              |
           +----------------------+              +----------------------+
           |                                                            |
           v                                                            v
 +--------------------+   +---------------------+   +-----------------------+   +-------------------+
 |  User / Auth Svc   |   | Hotel/Inventory Svc |   |    Booking Service    |   |  Payment Service  |
 |      (8081)        |   |       (8082)        |   |        (8084)         |   |      (8085)       |
 +---------+----------+   +----------+----------+   +-----------+-----------+   +---------+---------+
           |                         |                          |                         |
      [user_db]                 [hotel_db]                [booking_db]               [payment_db]
           |                         |                          |                         |
           +-------------------------+--------------------------+-------------------------+
                                     | (Async Events)
                                     v
                          +---------------------+
                          | Event Broker(Kafka) |
                          +----------+----------+
                                     |
           +-------------------------+--------------------------+
           |                                                    |
           v                                                    v
 +--------------------+                               +--------------------+
 |  Notification Svc  |                               |   Offer Service    |
 |      (8086)        |                               |       (8083)       |
 +--------------------+                               +---------+----------+
                                                                |
                                                           [offer_db]
```

---

## Service Registry & Ports

| Service | Port | Database | Primary Responsibility |
| :--- | :--- | :--- | :--- |
| **API Gateway** | `8080` | N/A | Edge ingress, JWT authentication, header enrichment (`X-User-*`), routing |
| **Discovery Service** | `8761` | N/A | Spring Cloud Netflix Eureka Service Registry |
| **User Service** | `8081` | `user_db` | Registration, login, password hashing, JWT token issuance |
| **Hotel Inventory Service** | `8082` | `hotel_db` | Hotels, rooms, inventory management, availability toggling |
| **Offer Service** | `8083` | `offer_db` | Discount coupons, promo code validation, discount calculation |
| **Booking Service** | `8084` | `booking_db` | Booking creation, check-in, check-out, cancellations, owner reports |
| **Payment Service** | `8085` | `payment_db` | Razorpay orders, HMAC SHA256 signature verification, Kafka payment events |
| **Notification Service** | `8086` | N/A | Kafka event listener for booking confirmations, cancellations & payments |
| **AI Service** | `8087` | `ai_db` | Spring AI Concierge, RAG Semantic Search with PGVector, and Tool Calling |
| **Common DTO** | Library | N/A | Shared DTOs, Kafka event contracts, and header constants |

---

## Getting Started

### 1. Start Infrastructure (Databases & Kafka)
Run the provided Docker Compose file to start PostgreSQL (with pre-configured `user_db`, `hotel_db`, `booking_db`, `payment_db`, `offer_db`) and Apache Kafka:

```bash
docker-compose up -d
```

### 2. Build the Project
From the repository root:

```bash
mvn clean package -DskipTests
```

### 3. Startup Order
Start the services in the following order:
1. `discovery-service` (Eureka Server at http://localhost:8761)
2. `api-gateway` (Port 8080)
3. Domain services:
   - `user-service` (Port 8081)
   - `hotel-inventory-service` (Port 8082)
   - `offer-service` (Port 8083)
   - `payment-service` (Port 8085)
   - `booking-service` (Port 8084)
   - `notification-service` (Port 8086)

---

## API & End-to-End Verification Flow

All requests should be routed through the **API Gateway** at `http://localhost:8080`.

### Step 1: Register and Login
```bash
# 1. Register Customer
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"John Doe","email":"john@example.com","password":"password123","role":"CUSTOMER"}'

# 2. Login & Copy JWT Token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","password":"password123"}'
```

### Step 2: Hotel & Room Inventory
```bash
# List Hotels (Public GET)
curl http://localhost:8080/api/hotels

# List Available Rooms with Discount (Public GET)
curl "http://localhost:8080/api/rooms?hotelId=1&offerCode=SAVE10"
```

### Step 3: Distributed Booking Creation
```bash
curl -X POST http://localhost:8080/api/bookings/create-order \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "roomId": 1,
    "checkInDate": "2026-10-01",
    "checkOutDate": "2026-10-05",
    "offerCode": "SAVE10"
  }'
```

### Step 4: Payment Verification & Event-Driven Confirmation
```bash
curl -X POST http://localhost:8080/api/payments/verify \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "bookingId": 1,
    "razorpayOrderId": "<ORDER_ID_FROM_STEP_3>",
    "razorpayPaymentId": "pay_mock_12345",
    "razorpaySignature": "mock_sig"
  }'
```
*When payment succeeds, `Payment Service` publishes `PaymentCompletedEvent` to Kafka. `Booking Service` consumes this event to update the booking status to `CONFIRMED`, and `Notification Service` automatically logs/sends a confirmation email!*

### Step 5: Spring AI Concierge & RAG Semantic Search
```bash
# 1. Ask the AI Concierge (Natural Language + Function Calling)
curl -X POST http://localhost:8080/api/ai/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Which hotels do you have in Mumbai, and what discounts are available?",
    "conversationId": "conv_123"
  }'

# 2. Semantic Similarity Search (PGVector)
curl -X POST http://localhost:8080/api/ai/search \
  -H "Content-Type: application/json" \
  -d '{
    "query": "luxury sea facing resort with king size bed",
    "topK": 3
  }'

# 3. Index Hotels & Rooms into Vector Store
curl -X POST http://localhost:8080/api/ai/index
```
