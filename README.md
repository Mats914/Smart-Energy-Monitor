# ⚡ Smart Energy Monitoring System v2

A production-ready backend system built with **Java 17**, **Spring Boot 3**, **Apache Kafka**, **WebSocket**, **PostgreSQL**, and **React** — simulating real-time energy grid monitoring similar to systems at **Svenska kraftnät**.

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    React Frontend                        │
│          WebSocket (STOMP) ←──── Live updates           │
└──────────────────────┬──────────────────────────────────┘
                       │ HTTP / REST
┌──────────────────────▼──────────────────────────────────┐
│                 Spring Boot Backend                      │
│                                                         │
│  AuthController    EnergyController                     │
│       │                  │                              │
│  AuthService        EnergyService                       │
│       │            ┌─────┴──────┐                       │
│      JWT      KafkaProducer  Repository                 │
│                    │                                    │
└────────────────────┼────────────────────────────────────┘
                     │ Kafka topic: energy-readings
┌────────────────────▼────────────────────────────────────┐
│              Kafka Consumer                             │
│    Persist → Check threshold → WebSocket push          │
└─────────────┬───────────────────────┬───────────────────┘
              │                       │
         PostgreSQL            WebSocket broker
         (readings,            /topic/readings/{user}
          alerts)              /topic/alerts/{user}
```

---

## 🚀 Features

| Feature | Details |
|---|---|
| 🔐 JWT Auth | Register/Login with role-based access (ADMIN/USER) |
| ⚡ Kafka Pipeline | Event-driven: Producer → Topic → Consumer |
| 📡 WebSocket | Live dashboard updates via STOMP/SockJS |
| 📊 Stats API | Daily/monthly totals, average, peak, active alerts |
| 🔄 Scheduler | Auto-generates sensor data every 30 seconds |
| 🚨 Smart Alerts | Threshold-based alerts with 4 severity levels |
| 🧪 Full Test Suite | Unit (Mockito) + Integration (MockMvc + EmbeddedKafka) |
| 🐳 Docker | One-command startup with Kafka + Zookeeper + PostgreSQL |

---

## 🔧 Tech Stack

**Backend**
- Java 17 + Spring Boot 3.2
- Spring Security + JWT (JJWT 0.11.5)
- Spring Data JPA + PostgreSQL
- Apache Kafka + Spring Kafka
- Spring WebSocket (STOMP over SockJS)
- Spring Scheduling
- Lombok + Bean Validation

**Frontend**
- React 18 + Vite
- Recharts (area chart)
- @stomp/stompjs + sockjs-client
- Axios with JWT interceptor
- Lucide React icons

**Testing**
- JUnit 5 + Mockito
- Spring MockMvc (integration)
- EmbeddedKafka (Kafka tests)
- Awaitility (async assertions)
- H2 in-memory database

**DevOps**
- Docker + Docker Compose
- Multi-stage Dockerfile
- Zookeeper + Kafka + PostgreSQL

---

## 📦 Quick Start

### One command (Docker)

```bash
git clone https://github.com/yourusername/smart-energy-monitor.git
cd smart-energy-monitor
docker-compose up --build
```

Services started:
- Backend API: `http://localhost:8080`
- Kafka: `localhost:9092`
- PostgreSQL: `localhost:5432`

### Frontend

```bash
cd frontend
npm install
npm run dev     # http://localhost:5173
```

### Run tests

```bash
mvn test        # Uses H2 + EmbeddedKafka — no external services needed
```

---

## 🌐 API Reference

### Authentication

```http
POST /api/auth/register
{ "username": "alice", "email": "alice@ex.com", "password": "secret123" }

POST /api/auth/login
{ "username": "alice", "password": "secret123" }
→ { "token": "eyJ...", "username": "alice", "role": "USER" }
```

### Energy readings

```http
# Submit (published via Kafka, processed async)
POST /api/energy
Authorization: Bearer <token>
{ "consumptionKwh": 7.5, "location": "Main Meter" }
→ 202 Accepted

# Get all readings
GET /api/energy
GET /api/energy/range?start=2024-01-01T00:00:00&end=2024-01-31T23:59:59

# Statistics
GET /api/energy/stats
→ { totalToday, totalThisMonth, averageDaily, peakConsumption, totalReadings, activeAlerts }

# Alerts
GET /api/energy/alerts
PUT /api/energy/alerts/{id}/acknowledge

# Delete
DELETE /api/energy/{id}
```

### WebSocket (STOMP)

Connect to `ws://localhost:8080/ws` then subscribe:

```javascript
client.subscribe(`/topic/readings/${username}`, msg => { /* live reading */ });
client.subscribe(`/topic/alerts/${username}`,   msg => { /* new alert */   });
client.subscribe(`/topic/stats/${username}`,    msg => { /* stats update */ });
```

---

## 🔄 Data Flow

```
User submits reading
       ↓
EnergyController.submit()
       ↓
EnergyService.submitReading()
       ↓
KafkaProducer → "energy-readings" topic
       ↓
KafkaConsumer.consume()
    ├── Save to PostgreSQL
    ├── Push to WebSocket /topic/readings/{user}
    └── If consumption > 10 kWh:
            ├── Save Alert to PostgreSQL
            └── Push to WebSocket /topic/alerts/{user}
```

---

## 🧪 Test Coverage

| Layer | Test type | Framework |
|---|---|---|
| Service | Unit | JUnit 5 + Mockito |
| Controller | Integration | MockMvc |
| Kafka | Integration | EmbeddedKafka + Awaitility |
| Exception handler | Slice test | @WebMvcTest |

---

## 📈 CV Description

```
Smart Energy Monitoring System v2 — Java 17, Spring Boot, Kafka, WebSocket, React

• Built event-driven backend using Apache Kafka (Producer/Consumer pattern)
• Implemented real-time dashboard updates via WebSocket (STOMP/SockJS)
• Designed REST API with JWT authentication and role-based authorization
• Automated energy data simulation using Spring Scheduler
• Wrote unit tests (Mockito) and integration tests (MockMvc + EmbeddedKafka)
• Containerized full stack with Docker Compose (Kafka + Zookeeper + PostgreSQL)
```
