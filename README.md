# Ocean View Resort – Distributed Online Reservation System

## Module  
CIS6003 – Advanced Programming  
BSc (Hons) Software Engineering  

---

## Project Overview

Ocean View Resort is a distributed online room reservation system developed using Java Servlet API and JDBC.

The system replaces manual booking management with a secure, scalable, and layered architecture aligned with enterprise Java EE fundamentals.

The project strictly follows:

- 3-Tier Architecture
- Object Oriented Design
- SOLID Principles
- Design Pattern Implementation
- Service Level Unit Testing (TDD-aligned approach)

---

# System Architecture

The system follows a strict 3-Tier Architecture:

## 1️⃣ Presentation Layer
- Java Servlets
- JSON-based RESTful endpoints
- Centralized error handling
- Session management

## 2️⃣ Business Layer
- Service implementations
- Business rule enforcement
- Constructor-based Dependency Injection
- Strategy Pattern (Validation implemented, Billing planned)

## 3️⃣ Data Access Layer
- DAO Pattern
- JDBC (MySQL)
- Connection utility
- Custom exception translation (DataAccessException)

Separation of concerns is strictly maintained.

---

# Implemented Modules

## ✅ User Management Module

### Features
- Secure authentication (SHA-256 password hashing)
- Role-based deletion restriction (Administrator protection)
- User activation/deactivation
- Exception-based validation
- Service-layer unit testing

### Design Concepts Applied
- DAO Pattern
- DTO Mapping Layer
- Dependency Injection (for testability)
- Exception handling abstraction
- Service-level business rule enforcement

---

## ✅ Room Management Module

### Features
- Create room
- Retrieve all rooms
- Retrieve room by ID
- Update room details
- Update room availability (idempotent logic)
- Delete room
- Duplicate name prevention
- Centralized exception handling

### Design Concepts Applied
- DAO Pattern
- Validation Strategy Pattern
- Service-layer business logic isolation
- Constructor Injection for testability

---
# Testing Strategy

Testing focuses on the Service Layer, where business logic resides.

## Approach
- JUnit 4.13.2
- FakeDAO implementations for isolation
- Constructor injection for dependency control
- Service-layer isolation testing
- Edge case validation
- Exception testing

## Coverage
- 94%+ Line Coverage
- 80%+ Branch Coverage
- 100% Class Coverage

This approach ensures deterministic, fast, and isolated unit tests aligned with TDD principles.

---

# Git Workflow

## Branching Strategy

- Dev → Active development
- Test → Verified builds
- Master → Stable releases

Pull Requests are used for controlled merges.

## Versioning

- v0.2.0-user-module
- v0.2.1-user-test

---

# Technologies Used

- Java 21+
- Servlet API
- JUnit 4.13.2
- MySQL
- JDBC
- Maven

---

# Future Modules
- Geuset Mangement
- Reservation Management (core distributed booking logic)
- Billing Calculation (Strategy Pattern)
- Reporting
- Session-based authentication improvements

---

## Author

Chathura Kavindu Bandara  
BSc (Hons) Software Engineering  
CIS6003 – Advanced Programming
