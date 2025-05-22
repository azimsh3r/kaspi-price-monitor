# Kaspi Price Monitor & Auto-Updater

**⚠️ This project is deprecated. It is no longer maintained, and future updates or support are not planned.**

A high-performance, production-grade backend system built for the **KaspiKz Marketplace**. This commercial project helps manage product prices based on real-time competitor data, optimizing pricing strategies and tracking order statistics. Built with Spring Boot, it features robust security with JWT and role-based access control.

---

## Features

- **Automated Price Updates**  
  Adjusts merchant prices based on real-time competitor analysis using smart strategies.
  
- **Product Sync from Remote API**  
  Automatically fetches and syncs product listings from the Kaspi merchant account.

- **Smart Proxy Management**  
  Uses a rotating pool of proxies with cooldown enforcement to bypass IP restrictions and scraping limits.

- **Concurrent & Safe**  
  Manages thousands of price updates using a thread-safe executor and semaphores for maximum performance.

- **Dynamic Pricing Engine**  
  Applies undercutting or competitive price raises with merchant-defined behavior and cooldown enforcement.

- **Full Security Stack**  
  - JWT-based authentication  
  - Role-based authorization (`ADMIN`, `MERCHANT`, `USER`)  
  - Secure access to endpoints and services
  
- **Order Statistics**  
  Collects and exposes detailed statistics per merchant: total orders, revenue, product-level breakdowns.

- **Price Raise Throttling**  
  Prevents over-frequent price increases using a 5-hour cooldown system.

- **RESTful API**  
  Simple, JSON-based API for price updates, product queries, order stats, and secure login.

---

## Tech Stack

| Layer             | Tech                          |
|------------------|-------------------------------|
| Backend Framework | Spring Boot                   |
| Authentication    | JWT (Spring Security)         |
| Authorization     | Role-based access control     |
| Database          | PostgreSQL / JPA              |
| Concurrency       | Java Executors, Semaphore     |
| Caching/Queue     | Redis                         |
| Proxy Management  | Custom `ProxyService`         |
| Communication     | REST API                      |

---

## Architecture Overview

![Image](https://github.com/user-attachments/assets/1d67222f-a6ca-4c8e-8167-834df8d80910)
---

## Authentication & Roles

| Role     | Access                                                                 |
|----------|------------------------------------------------------------------------|
| `ADMIN`  | All endpoints (price updates, stats, product CRUD, proxy management)   |
| `MERCHANT` | View/update own products and pricing                                 |
| `USER`   | Read-only product and order access                                     |

### Example JWT-secured endpoint
```
POST /api/update-prices
Authorization: Bearer <your_token>
```

---

## Order Statistics

**GET** `/api/orders/stats?merchantId=123`

Returns:
```json
{
  "merchantId": 123,
  "totalOrders": 98,
  "totalRevenue": 185000,
  "topProducts": [
    { "sku": "XYZ123", "orders": 40, "revenue": 90000 },
    ...
  ]
}
```

---

## API Summary

| Method | Endpoint                   | Description                          |
|--------|----------------------------|--------------------------------------|
| POST   | `/api/login`               | Authenticate and get JWT             |
| POST   | `/api/update-prices`       | Start scheduled price updates        |
| GET    | `/api/products`            | List all products                    |
| GET    | `/api/orders/stats`        | Get aggregated order stats           |
| POST   | `/api/products`            | Add a product (admin/merchant only)  |

---

## Setup & Run

```bash
# Clone the repo
git clone https://github.com/yourusername/kaspi-price-monitor.git
cd kaspi-price-monitor

# Configure DB, Redis, and JWT secret, and your Kaspi Token in application.properties

# Build and run
./mvnw clean install
./mvnw spring-boot:run
```

---

## Optimizations & Engineering Decisions

- Used `ScheduledExecutorService` + `Semaphore` for concurrency control.
- Redis handles session storage for proxies and merchants.
- JWT & Spring Security used for lightweight, scalable access control.

---

## Author

**Azimjon Akhmadjonov**  
Software Engineer | Distributed Systems Builder  
Reach out on [GitHub](https://github.com/azimsh3r) or [LinkedIn](https://www.linkedin.com/in/azimjon-akhmadjonov/)
