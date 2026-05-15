# 🏎️ F1 Data Ecosystem: Real-Time Microservices Architecture

[![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.2-brightgreen?style=for-the-badge&logo=springboot)](https://spring.io/projects/spring-boot)
[![Kafka](https://img.shields.io/badge/Apache_Kafka-Event--Driven-black?style=for-the-badge&logo=apachekafka)](https://kafka.apache.org/)
[![Microservices](https://img.shields.io/badge/Architecture-Microservices-blue?style=for-the-badge)](https://microservices.io/)

A cutting-edge, **high-concurrency** distributed system built to ingest, process, and visualize Formula 1 racing data. By integrating the **OpenF1 API**, this ecosystem transforms raw telemetry into real-time streams and deep analytical insights using a modern event-driven approach.

---

## ⚡ The Mission
This project isn't just about data; it's about **speed and reliability**. It simulates the high-pressure environment of an F1 pit wall, handling thousands of telemetry data points per second, managing complex timing strategies, and providing a seamless "replay" experience for historical races.

## 🛠️ Tech Stack & Engineering Excellence
* **Core:** Java 21, Spring Boot 3.4.2, Spring WebFlux (Reactive Stack).
* **Messaging & Streams:** **Apache Kafka** for decoupled service communication and real-time data pipelines.
* **Gateway & Security:** Spring Cloud Gateway with **Redis-backed Rate Limiting** and stateless **JWT** authentication.
* **Resilience:** Advanced fault tolerance using **Resilience4j** (Circuit Breakers & Retries) to handle external API latencies.
* **Real-Time Engagement:** **STOMP Over WebSockets** for push-based telemetry and live notifications.
* **Persistence:** PostgreSQL with optimized indexing for rapid query performance.

---

## 🏗️ Inside the Paddock (The Services)

### 🛡️ [Gateway Service] - The Digital Steward
* Acts as the unified entry point.
* Implements high-speed **Redis Rate Limiting** to protect internal services.
* Centralized **JWT Security Filter** ensures all race data is accessed securely.

### 📊 [Analytics Service] - The Strategist
* Crunching numbers to provide **Session Summaries** and **Driver Consistency** reports.
* Calculates moving averages and fastest lap trends using asynchronous processing.

### 🏎️ [Telemetry Service] - The Pulse
* The high-frequency heart of the system.
* Broadcasts **Live Speed, Gear, and DRS** data via WebSockets for real-time dashboard updates.

### ⏱️ [Timing Service] - The Clock
* Tracks every millisecond.
* Manages **Lap Times** and **Stint (Tyre) Strategies** (Soft/Medium/Hard) with automatic sync and fallback mechanisms.

### 🔄 [Replay Service] - The Time Machine
* A unique simulation engine that allows re-streaming of historical sessions.
* Provides frame-by-frame data playback as if the race were happening *right now*.

### 🔔 [Notification Service] - The Engineer's Radio
* Keeps users in the loop with event-triggered alerts (Yellow Flags, Weather changes, Race Starts).

---

## 🚀 Key Engineering Challenges Solved
* **Zero-Downtime Data Sync:** Using `CompletableFuture` and Reactive clients to sync massive race datasets without blocking threads.
* **Fault Tolerance:** When the external F1 API stutters, the **Circuit Breaker** kicks in, serving cached data or graceful fallbacks.
* **High-Frequency Streaming:** Optimized Kafka-to-WebSocket pipelines to ensure telemetry data reaches the client with sub-second latency.

## 🏁 Installation & Quick Start
1.  **Spin up Infrastructure:** Use Docker to launch PostgreSQL, Kafka, and Redis.
2.  **Clone & Build:** `./gradlew clean build`
3.  **Launch the Grid:** Fire up the Discovery Server/Gateway, then activate your desired services.
4.  **Explore the API:** Interactive Swagger documentation is available at the Gateway's UI endpoint.