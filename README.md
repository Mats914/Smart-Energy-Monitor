# ⚡ Smart Energy Monitoring System v2

A production-ready full-stack system simulating real-time energy grid monitoring.
Built with **Java 17**, **Spring Boot 3**, **Apache Kafka**, **WebSocket**, **React**, and **Docker**.

## 🏗️ Architecture

```
React Frontend  ──REST + WebSocket──►  Spring Boot 3
                                           │
                                    EnergyKafkaProducer
                                           │
                                    Apache Kafka (3 partitions)
                                           │
                                    EnergyKafkaConsumer
                                     ├── Save to PostgreSQL
                                     ├── WebSocket push → live UI
                                     └── Alert if > 10 kWh
```

## 🚀 Features

| | Feature |
|---|---|
| 🔐 | JWT auth · BCrypt · Role-based (ADMIN/USER) · Rate limiting 60 req/min |
| ⚡ | CRUD readings · 30s simulation · Date range queries |
| 🟡 | Kafka Producer/Consumer pipeline · 3 partitions · EmbeddedKafka tests |
| 🟢 | WebSocket STOMP · Live readings · Live alerts · Live stats |
| 🚨 | 4 severity levels · Auto-generation · Acknowledge flow |
| 📋 | Async audit logging for every action with IP tracking |
| 🛡️ | Admin: user management + paginated audit log |
| 🐳 | Docker Compose: Kafka + Zookeeper + PostgreSQL |
| 🧪 | Unit + Integration + EmbeddedKafka consumer tests |

## 📦 Quick Start

```bash
# All-in-one Docker
docker-compose up --build

# Frontend (separate terminal)
cd frontend && npm install && npm run dev
```

## 🌐 API

```http
POST /api/auth/register        { username, email, password }
POST /api/auth/login           { username, password }
POST /api/energy               Submit reading via Kafka
GET  /api/energy/stats         Dashboard statistics
GET  /api/energy/alerts        My alerts
PUT  /api/energy/alerts/{id}/acknowledge
GET  /api/admin/audit          Full audit log (ADMIN only)
```

## 📁 Structure

```
src/main/java/com/energy/
├── config/       Security · Kafka · WebSocket · RateLimiting · AppProperties
├── controller/   Auth · Energy · Admin
├── dto/          All DTOs + Kafka event types
├── exception/    GlobalHandler + ResourceNotFound/Conflict/Unauthorized
├── kafka/        Producer · Consumer
├── model/        User · EnergyReading · Alert · AuditLog
├── repository/   5 JPA repos with custom JPQL
├── scheduler/    30s simulation scheduler
├── security/     JWT filter + utility
├── service/      Auth · Energy · Audit · EnergyMapper
└── websocket/    WebSocketNotificationService

frontend/src/
├── api/          axios + JWT interceptor
├── components/   Sidebar · shared UI (StatCard, Panel, etc.)
├── hooks/        useAuth · useEnergy · useWebSocket
└── pages/        Dashboard · Readings · Alerts · Login · Register
```

## 📝 CV Description

```
Smart Energy Monitoring System | Java · Spring Boot · Kafka · WebSocket · React · Docker

• Event-driven pipeline: Kafka Producer/Consumer (3 partitions) for real-time data ingestion
• WebSocket (STOMP/SockJS) for live dashboard updates — no polling
• JWT authentication with role-based access (ADMIN/USER)
• Async audit logging (AuditService) for all user actions with IP tracking
• Rate limiting filter (60 req/min/IP) and global exception handling
• 30+ unit and integration tests including EmbeddedKafka consumer tests
• Containerized with Docker Compose: Kafka + Zookeeper + PostgreSQL
• React frontend: live area chart, readings table, alert management
```
