# Ocean View Resort – Distributed Online Reservation System

## Module
CIS6003 – Advanced Programming  
BSc (Hons) Software Engineering

## Project Overview
Ocean View Resort is a distributed online room reservation system developed using Java Servlet API.  
The system replaces manual booking management with a secure, scalable, and structured 3-tier architecture.

---

## Architecture

The system follows a strict 3-Tier Architecture:

1. Presentation Layer
   - Servlets
   - JSON-based REST endpoints
   - Session management

2. Business Layer
   - Service implementations
   - Business rule enforcement
   - Strategy pattern (planned for billing module)

3. Data Access Layer
   - DAO Pattern
   - JDBC (MySQL)
   - Connection utility

SOLID principles and separation of concerns are maintained throughout.

---

## Implemented Module: User Management

### Features
- User registration
- Secure authentication (SHA-256 hashing)
- Role-based deletion restrictions
- User status activation/deactivation
- Exception handling

---

## Design Principles Applied
- Dependency Injection
- DAO Pattern
- DTO Mapping Layer
- SOLID Principles
- 3-Tier Architecture

---

## Testing Strategy

- JUnit 4.13.2
- Service layer tested using FakeUserDAO
- Dependency injection used for isolation
- High test coverage achieved (90%+ line coverage)

---

## Git Workflow

Branching Strategy:
- Dev → Development
- Test → Verified builds
- Master → Stable release

Pull requests used for controlled merges.

Versioning:
- v0.2.0-user-module

---

## Technologies Used

- Java 21+
- Servlet API
- JUnit 4.13.2
- MySQL
- JDBC
- Maven

---

## Future Modules

- Reservation Management
- Billing Calculation (Strategy Pattern)
- Reporting
- Stored Procedures / Database triggers

---

Chathura Kavindu Bandara
