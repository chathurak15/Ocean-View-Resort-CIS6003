# Ocean View Resort – Distributed Online Reservation System

## Module  
CIS6003 – Advanced Programming  
BSc (Hons) Software Engineering  

---

## Project Overview

Ocean View Resort is a distributed online room reservation system developed using Java Servlet API and JDBC.

The system replaces manual booking management with a secure, scalable, and layered architecture aligned with enterprise Java EE fundamentals.

The project strictly follows:

- 3 Tier Architecture
- Object Oriented Design
- SOLID Principles
- Design Pattern Implementation
- Service Level Unit Testing (TDD-aligned approach)

---

# System Architecture

The system follows a strict 3-Tier Architecture:

## 1️ Presentation Layer
- Java Servlets
- REST style JSON endpoints
- Centralized exception handling (BaseServlet)
- HTTP status code standardization
- Date-range query support
- Session-ready architecture

---

## 2️ Business Layer
- Service implementations
- Business rule validation
- Constructor-based Dependency Injection
- Strategy Pattern (Billing logic)
- Validation Strategy Pattern
- Exception-driven flow control

This layer contains the core domain logic and is fully unit-tested.

---

## 3️ Data Access Layer
- DAO Pattern
- JDBC (MySQL)
- Connection utility (Singleton pattern)
- Custom exception abstraction (DataAccessException)

Strict separation of concerns is maintained between layers.

---

#  Implemented Modules

---

## User Management Module

### Features
- Secure authentication (SHA-256 password hashing)
- Role-based deletion restriction (Administrator protection)
- User activation/deactivation
- Duplicate username prevention
- Service-layer validation
- Full unit test coverage

### Design Concepts Applied
- DAO Pattern
- DTO Mapping Layer
- Constructor Injection
- Exception abstraction
- Business rule enforcement in the Service layer

---

## Guest Management Module
### Features
- Create, retrieve, update, and delete guests
- Search by ID, NIC, and phone number
- Duplicate validation checks
- Service-level isolation testing
---

## Room Management Module
### Features
- Create room
- Retrieve all rooms
- Retrieve room by ID
- Update room details
- Update room availability (idempotent logic)
- Delete room
- Duplicate room name prevention
- Available rooms by date range
- Maintenance/availability logic

### Design Concepts Applied
- DAO Pattern
- Validation Strategy Pattern
- Service-layer isolation
- Constructor Injection for testability
- BusinessRuleException enforcement
---

##  Reservation Management Module
### Features
- Create reservation
- Reservation conflict detection
- Cancel reservation
- Complete reservation
- Retrieve by reservation number
- Retrieve all reservations
- Retrieve reservations by date range (reporting)
- Reservation status lifecycle management

### Conflict Handling
- Overlapping booking prevention
- Boundary condition handling (check-in equals check-out allowed)
- Confirmed reservations block availability
- Cancelled reservations do not block availability

---

## Billing Module (Strategy Pattern)

### Implemented Strategies

- `FlatRateBillingStrategy`  
- `RoomTypeSurchargeBillingStrategy`  

### Features
- Dynamic price calculation
- Night-based billing
- Room type multiplier logic
- Reservation line item total calculation
- Business rule validation for invalid rates

### Design Pattern Applied
- Strategy Pattern (Runtime billing selection)
---

# Testing Strategy
Testing focuses on the **Service Layer**, where business logic resides.
## Approach
- JUnit 4.13.2
- FakeDAO implementations for isolation
- Stub services for facade testing
- Constructor based dependency injection
- Edge-case validation
- Exception expectation testing
- Date boundary testing

This approach ensures deterministic, fast, and isolated tests aligned with TDD principles.

---

# Git Workflow
## Branching Strategy

- **Dev** → Active development  
- **Test** → Verified and stable builds  
- **Master** → Production-ready release  

All merges are performed using Pull Requests.

## Version Tags
- `v0.2.0-user-module`
- `v0.2.1-user-tests`
- `v0.3.0-room-module`
- `v1.0.0-stable-release` (planned)

---

# Design Patterns Used
- Strategy Pattern (Billing)
- Validation Strategy Pattern
- Facade Pattern
- Singleton Pattern (DB Connection)
- Constructor Based Dependency Injection

---

# Technologies Used
- Java 21+
- Servlet API
- JDBC
- MySQL
- Maven
- JUnit 4.13.2
- Postman (API testing)

---

# System Strengths
- Clean layered architecture
- SOLID compliant service layer
- Exception-driven validation
- Conflict detection algorithm
- Deterministic unit testing
- Scalable module design
- Enterprise-ready structure

---

# Author

**Chathura Kavindu Bandara**  
BSc (Hons) Software Engineering  
CIS6003 – Advanced Programming  
