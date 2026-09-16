# Banking Application

A production-oriented **Banking Application REST API** built using **Java 26 and Spring Boot**. The application provides secure banking operations for administrators, employees, and customers, including customer and employee management, bank accounts, transactions, loans, loan repayments, investments, investment performance, notifications, file management, auditing, and reporting.

The project was developed with an emphasis on **clean architecture, security, transactional integrity, database versioning, validation, observability, maintainability, and enterprise-oriented Spring Boot practices**.

---

## Table of Contents

* [Overview](#overview)
* [Key Features](#key-features)
* [Technology Stack](#technology-stack)
* [Architecture](#architecture)
* [Application Modules](#application-modules)
* [User Roles](#user-roles)
* [Authorization Matrix](#authorization-matrix)
* [Core Business Flow](#core-business-flow)
* [Security](#security)
* [JWT Authentication Flow](#jwt-authentication-flow)
* [Idempotency](#idempotency)
* [Caching](#caching)
* [Database and Flyway Migration](#database-and-flyway-migration)
* [Auditing](#auditing)
* [AOP and Cross-Cutting Concerns](#aop-and-cross-cutting-concerns)
* [Validation and Exception Handling](#validation-and-exception-handling)
* [File Management](#file-management)
* [Notifications and Events](#notifications-and-events)
* [Schedulers](#schedulers)
* [Reporting](#reporting)
* [API Documentation](#api-documentation)
* [Actuator and Monitoring](#actuator-and-monitoring)
* [Project Structure](#project-structure)
* [Database Schema](#database-schema)
* [Prerequisites](#prerequisites)
* [Configuration](#configuration)
* [JWT Key Generation](#jwt-key-generation)
* [Running the Application](#running-the-application)
* [Testing](#testing)
* [Build and Package](#build-and-package)
* [API Endpoint Reference](#api-endpoint-reference)
* [Development Profiles](#development-profiles)
* [Production Considerations](#production-considerations)
* [Repository Guidelines](#repository-guidelines)
* [Project Documentation](#project-documentation)
* [License](#license)

---

# Overview

The Banking Application is a modular Spring Boot REST API designed to demonstrate how a banking domain can be implemented using modern Java and Spring technologies.

The application separates responsibilities across:

* REST controllers
* Request/response DTOs
* MapStruct mappers
* Service interfaces and implementations
* JPA repositories
* Domain entities
* Security and authorization services
* Cross-cutting AOP components
* Event publishing and listeners
* Scheduled maintenance processes
* Database migration scripts
* Centralized exception handling
* Validation components
* Auditing
* Caching
* File storage
* Reporting

The REST API is versioned under:

```text
/api/v1
```

The application is stateless for REST authentication and uses **JWT access tokens** with **refresh-token support**.

---

# Key Features

## Core Banking

* User management
* Customer management
* Employee management
* Administrator management
* Bank account management
* Account status management
* Deposit
* Withdrawal
* Money transfer
* Transaction reversal
* Transaction history

## Loan Management

* Loan application
* Loan eligibility checking
* Loan approval
* Loan rejection
* Loan completion
* Loan disbursement
* Loan repayment
* Loan monitoring

## Investment Management

* Investment creation
* Investment retrieval
* Customer investment listing
* Investment maturity
* Investment settlement
* Investment performance recording
* Investment performance history
* Latest investment performance retrieval
* Investment maturity processing

## Security

* Spring Security
* Stateless REST authentication
* JWT authentication
* RSA-based JWT signing
* Role-based authorization
* Ownership-based authorization
* Method-level security
* BCrypt password hashing
* Password change functionality
* Refresh tokens
* Logout and refresh-token invalidation
* Custom authentication entry point
* Custom access-denied handler
* Security auditing

## Reliability

* Idempotency protection
* Transaction management
* Database constraints
* Global exception handling
* Business exceptions
* Resource-not-found handling
* Validation
* Scheduled cleanup processes
* Optimistic/transactional business processing where applicable

## Enterprise Features

* Flyway database migration
* JPA auditing
* AOP
* Application events
* Caffeine caching
* Spring scheduling
* Spring Actuator
* OpenAPI / Swagger
* Structured API responses
* Pagination support
* File metadata management
* Local file storage
* Apache Tika integration
* Performance monitoring
* Logging

---

# Technology Stack

| Technology                     | Usage                                |
| ------------------------------ | ------------------------------------ |
| Java 26                        | Application development              |
| Spring Boot 4.1.1              | Application framework                |
| Spring Web MVC                 | REST API                             |
| Spring Data JPA                | Persistence                          |
| Hibernate                      | ORM                                  |
| MySQL                          | Relational database                  |
| Flyway                         | Database migration/versioning        |
| Spring Security                | Authentication and authorization     |
| JWT / OAuth2 Resource Server   | Stateless API authentication         |
| RSA                            | JWT signing                          |
| BCrypt                         | Password hashing                     |
| Spring Validation              | Request validation                   |
| MapStruct                      | DTO/entity mapping                   |
| Lombok                         | Boilerplate reduction                |
| Spring AOP / AspectJ           | Cross-cutting concerns               |
| Caffeine                       | Application caching                  |
| Spring Events                  | Domain/application events            |
| Spring Scheduling              | Background processing                |
| Spring Actuator                | Monitoring and operational endpoints |
| Springdoc OpenAPI              | API documentation                    |
| Apache Tika                    | File/document processing             |
| Spring AI Tika Document Reader | Document reading support             |
| Maven                          | Build and dependency management      |
| JUnit / Spring Boot Test       | Testing                              |
| Testcontainers                 | Integration-test database support    |

---

# Architecture

The application follows a layered and modular architecture.

```text
                         ┌─────────────────────────┐
                         │       API Client        │
                         │ Postman / External App  │
                         └────────────┬────────────┘
                                      │
                                      ▼
                         ┌─────────────────────────┐
                         │     REST Controllers    │
                         │       /api/v1/**        │
                         └────────────┬────────────┘
                                      │
                                      ▼
                         ┌─────────────────────────┐
                         │   Security / JWT Layer  │
                         │ Authentication + Roles   │
                         │ Ownership Authorization │
                         └────────────┬────────────┘
                                      │
                                      ▼
                         ┌─────────────────────────┐
                         │     Service Layer       │
                         │ Business Rules / Txn    │
                         └────────────┬────────────┘
                                      │
                         ┌────────────┴────────────┐
                         ▼                         ▼
                ┌──────────────────┐      ┌──────────────────┐
                │     MapStruct    │      │  Domain Events   │
                │ DTO <-> Entity   │      │    Listeners     │
                └────────┬─────────┘      └────────┬─────────┘
                         │                         │
                         ▼                         ▼
                ┌──────────────────┐      ┌──────────────────┐
                │  JPA Repository  │      │ Notifications /  │
                │                  │      │ Audit Processing │
                └────────┬─────────┘      └──────────────────┘
                         │
                         ▼
                ┌──────────────────┐
                │      MySQL       │
                │   + Flyway       │
                └──────────────────┘
```

Cross-cutting concerns are implemented using AOP and infrastructure components:

```text
                     ┌────────────────────┐
                     │    REST Request    │
                     └─────────┬──────────┘
                               │
                               ▼
                  ┌────────────────────────┐
                  │ Security / JWT Filter  │
                  └────────────┬───────────┘
                               │
                               ▼
                  ┌────────────────────────┐
                  │   Idempotency Filter   │
                  └────────────┬───────────┘
                               │
                               ▼
                  ┌────────────────────────┐
                  │       Controller       │
                  └────────────┬───────────┘
                               │
                               ▼
                  ┌────────────────────────┐
                  │        Service         │
                  └────────────┬───────────┘
                               │
               ┌───────────────┼────────────────┐
               ▼               ▼                ▼
          Audit Aspect    Logging Aspect   Performance Aspect
                               │
                               ▼
                         Repository / DB
```

---

# Application Modules

The project is organized by business domain.

## 1. Authentication

Package:

```text
com.example.bank.auth
```

Responsibilities:

* Login
* Access-token generation
* Refresh-token processing
* Logout
* Password change
* Authentication-related persistence

---

## 2. User

Package:

```text
com.example.bank.user
```

Responsibilities:

* User creation
* User update
* User retrieval
* Username-based lookup
* User deletion
* User lifecycle management

---

## 3. Customer

Package:

```text
com.example.bank.customer
```

Responsibilities:

* Customer creation
* Customer update
* Customer lookup
* Customer/user relationship
* Customer activation
* Customer deactivation
* Customer lifecycle validation

---

## 4. Employee

Package:

```text
com.example.bank.employee
```

Responsibilities:

* Employee creation
* Employee update
* Employee lookup
* Employee/user relationship
* Employee deletion

---

## 5. Administrator

Package:

```text
com.example.bank.admin
```

Responsibilities:

* Administrator management
* Administrator creation
* Administrator update
* Administrator retrieval
* Administrator deactivation
* Administrative account operations

---

## 6. Account

Package:

```text
com.example.bank.account
```

Responsibilities:

* Bank account creation
* Account lookup
* Customer account lookup
* Account activation
* Account deactivation
* Account blocking
* Account closure
* Account-number generation

---

## 7. Transaction

Package:

```text
com.example.bank.transaction
```

Responsibilities:

* Deposit
* Withdrawal
* Transfer
* Transaction history
* Transaction lookup
* Transaction reversal
* Transaction reference generation

Financial operations are implemented with transactional business processing.

---

## 8. Loan

Package:

```text
com.example.bank.loan
```

Responsibilities:

* Loan application
* Loan eligibility
* Loan approval
* Loan rejection
* Loan completion
* Loan disbursement
* Loan lifecycle management

---

## 9. Loan Repayment

Package:

```text
com.example.bank.loanrepayment
```

Responsibilities:

* Loan repayment
* Repayment lookup
* Loan repayment history
* Repayment status management

---

## 10. Investment

Package:

```text
com.example.bank.investment
```

Responsibilities:

* Investment creation
* Investment lookup
* Customer investments
* Investment maturity
* Investment settlement

---

## 11. Investment Performance

Package:

```text
com.example.bank.investmentperformance
```

Responsibilities:

* Performance recording
* Performance lookup
* Investment performance history
* Latest performance retrieval

---

## 12. Notification

Package:

```text
com.example.bank.notification
```

Responsibilities:

* User notifications
* Unread notification retrieval
* Mark notification as read
* Mark all notifications as read
* Notification maintenance

---

## 13. File Management

Package:

```text
com.example.bank.file
```

Responsibilities:

* File upload
* File metadata
* File download
* Customer file listing
* File deletion
* File-type validation
* Local file storage

Supported file extensions include:

```text
pdf
jpg
jpeg
png
xlsx
docx
```

---

## 14. Audit

Package:

```text
com.example.bank.audit
```

Responsibilities:

* Audit log persistence
* Audit log retrieval
* Audit event processing
* Security-related auditing

---

## 15. Reporting

Package:

```text
com.example.bank.reporting
```

Supported reports:

* Customer reports
* Account reports
* Transaction reports
* Loan reports
* Investment reports

Reports support pagination and filtering through request parameters where applicable.

---

## 16. Idempotency

Package:

```text
com.example.bank.idempotency
```

Responsibilities:

* Idempotency-key processing
* Duplicate request prevention
* Request state tracking
* Idempotency record cleanup

---

# User Roles

The application supports three primary roles:

```text
ADMIN
EMPLOYEE
CUSTOMER
```

## ADMIN

Administrative users can perform administrative operations such as:

* Manage administrators
* Manage employees
* Manage customers
* Manage accounts
* Approve/reject loans
* Access administrative reports
* Access audit information according to authorization rules

## EMPLOYEE

Employees support banking operations such as:

* Customer management
* Employee-related operations permitted by authorization rules
* Account operations
* Loan processing
* Banking operations permitted by role and ownership rules

## CUSTOMER

Customers can perform customer-owned banking operations such as:

* View their account information
* View balances
* Transfer money
* Deposit/withdraw where permitted
* View transactions
* Apply for loans
* Check loan eligibility
* View loans
* Make loan repayments
* Manage investments
* View notifications
* Manage their permitted files

---

# Authorization Matrix

The exact authorization is enforced by Spring Security together with domain-specific authorization services.

| Operation                |       ADMIN      |      EMPLOYEE      |     CUSTOMER    |
| ------------------------ | :--------------: | :----------------: | :-------------: |
| Administrator management |        Yes       |         No         |        No       |
| Employee management      |        Yes       |    As permitted    |        No       |
| Customer management      |        Yes       |         Yes        |        No       |
| Account administration   |        Yes       |         Yes        | Ownership/rules |
| Money transfer           |    Restricted    |     Restricted     |       Yes       |
| Loan application         |   As permitted   |    As permitted    |       Yes       |
| Loan eligibility         |        Yes       |         Yes        |       Own       |
| Loan approval            |        Yes       |         Yes        |        No       |
| Loan rejection           |        Yes       |         Yes        |        No       |
| Loan disbursement        |        Yes       |         Yes        |        No       |
| Loan repayment           |   As permitted   |    As permitted    |       Own       |
| Investments              |   As permitted   |    As permitted    |       Own       |
| Audit logs               | Authorized users |     Restricted     |        No       |
| Reports                  | Authorized users | Authorized reports |    Restricted   |
| Notifications            |  Own/authorized  |   Own/authorized   |       Own       |

> Authorization is not based only on role. Domain authorization services are also used to enforce resource ownership and business-specific access rules.

---

# Core Business Flow

## Customer Creation Flow

```text
Admin / Employee
       │
       ▼
Create User
       │
       ▼
Create Customer
       │
       ▼
Customer Lifecycle Validation
       │
       ▼
Persist Customer
       │
       ▼
Publish CustomerCreatedEvent
       │
       ├──────────────► Audit Processing
       │
       └──────────────► Notification Processing
```

## Account Flow

```text
Customer
   │
   ▼
Account Creation
   │
   ▼
Account Number Generation
   │
   ▼
Account Validation
   │
   ▼
Account Persistence
   │
   ▼
Active Account
```

## Money Transfer Flow

```text
Customer
   │
   ▼
POST /api/v1/transactions/transfer
   │
   ▼
JWT Authentication
   │
   ▼
Role / Ownership Authorization
   │
   ▼
Idempotency Check
   │
   ▼
Validate Source Account
   │
   ▼
Validate Destination Account
   │
   ▼
Validate Balance
   │
   ▼
Transactional Debit + Credit
   │
   ▼
Create Transaction Records
   │
   ▼
Publish TransactionCompletedEvent
   │
   ├──────────────► Audit
   │
   └──────────────► Notification
```

## Loan Flow

```text
Customer
   │
   ▼
Loan Application
   │
   ▼
Eligibility Validation
   │
   ▼
Pending
   │
   ├────► Approved ────► Disbursed ────► Completed
   │
   └────► Rejected
```

---

# Security

The REST API uses Spring Security with a stateless security model.

The REST security chain is applied to:

```text
/api/**
```

Authentication is performed using JWT bearer tokens.

The application uses:

* Spring Security
* OAuth2 Resource Server
* JWT
* RSA key pair
* BCrypt password encoding
* Method-level security
* Custom authentication entry point
* Custom access-denied handler
* Role-based authorization
* Resource ownership authorization

Passwords are never intended to be stored as plain text. Passwords are encoded using:

```text
BCryptPasswordEncoder
```

---

# JWT Authentication Flow

```text
                    ┌───────────────┐
                    │     Client    │
                    └───────┬───────┘
                            │
                            │ username + password
                            ▼
                    ┌───────────────┐
                    │ POST /login   │
                    └───────┬───────┘
                            │
                            ▼
                    ┌───────────────┐
                    │ Authentication│
                    │    Manager    │
                    └───────┬───────┘
                            │
                            ▼
                    ┌───────────────┐
                    │ UserDetails   │
                    │    Service    │
                    └───────┬───────┘
                            │
                            ▼
                    ┌───────────────┐
                    │ BCrypt Verify │
                    └───────┬───────┘
                            │
                            ▼
                    ┌───────────────┐
                    │ JWT Generator │
                    │ RSA Private   │
                    │     Key       │
                    └───────┬───────┘
                            │
                            ▼
                  Access Token + Refresh Token
                            │
                            ▼
                    ┌───────────────┐
                    │ Future API    │
                    │   Requests    │
                    └───────┬───────┘
                            │
                   Authorization: Bearer <JWT>
                            │
                            ▼
                    JWT Validation
                            │
                            ▼
                       API Access
```

Login endpoint:

```http
POST /api/v1/auth/login
```

Refresh endpoint:

```http
POST /api/v1/auth/refresh
```

Logout endpoint:

```http
POST /api/v1/auth/logout
```

Password change:

```http
POST /api/v1/auth/change-password
```

---

# Idempotency

Financial APIs can receive the same request more than once because of:

* Network retries
* Client retries
* Duplicate button clicks
* Timeout recovery
* Reverse proxies or API gateways retrying requests

The application therefore includes an idempotency mechanism.

Clients can send an idempotency key for supported operations.

Example:

```http
Idempotency-Key: 7c3f0e21-6c8e-4e58-a5f1-123456789abc
```

The request lifecycle is conceptually:

```text
Incoming Request
       │
       ▼
Read Idempotency-Key
       │
       ▼
Existing Request?
   ┌───┴───┐
   │       │
  Yes      No
   │       │
   ▼       ▼
Return    Process
Existing   Request
Result      │
            ▼
      Store Result/State
```

Idempotency data is persisted in the database and cleaned up by a scheduled process.

Default development configuration:

```yaml
bank:
  idempotency:
    ttl-minutes: 2
```

Production configuration can override this using:

```text
IDEMPOTENCY_TTL_MINUTES
```

---

# Caching

The application uses **Caffeine** for in-memory caching.

Configured cache areas include:

```text
userById
userByUsername
customerById
customerByUserId
employeeById
employeeByUserId
accountById
accountByNumber
loanById
investmentById
```

Default development cache configuration:

```text
Maximum entries: 1000
Expiration:      1 minute after write
```

Production configuration supports environment-based cache sizing and expiration.

Example:

```text
CACHE_MAX_SIZE
CACHE_EXPIRE_AFTER_WRITE
```

Caching is used to reduce unnecessary database access for frequently requested read operations.

---

# Database and Flyway Migration

The application uses:

```text
MySQL
```

Database schema changes are managed using:

```text
Flyway
```

Hibernate schema generation is configured as:

```text
ddl-auto: validate
```

This means Hibernate validates the entity/database structure rather than automatically creating or modifying the schema.

## Migration History

The project currently contains the following migrations:

| Version | Migration                           |
| ------- | ----------------------------------- |
| V1      | Create users                        |
| V2      | Create customers                    |
| V3      | Create employees                    |
| V4      | Create accounts                     |
| V5      | Create transactions                 |
| V6      | Create loans                        |
| V7      | Create loan repayments              |
| V8      | Create investments                  |
| V9      | Create investment performance       |
| V10     | Create file metadata                |
| V11     | Create refresh tokens               |
| V12     | Add loan disbursement support       |
| V13     | Add customer lifecycle              |
| V14     | Create notifications                |
| V15     | Create audit logs                   |
| V16     | Create idempotency keys             |
| V17     | Add investment settlement support   |
| V18     | Correct transaction type constraint |

Migration directory:

```text
src/main/resources/db/migration/
```

When the application starts, Flyway applies pending migrations automatically.

---

# Auditing

JPA auditing is enabled to track entity lifecycle information.

The common auditable infrastructure includes:

```text
AuditableEntity
AuditorAwareImpl
AuditAction
```

The application supports auditing information such as:

* Created date
* Created by
* Last modified date
* Last modified by

Business/security operations can also generate audit events.

Audit records are persisted in the audit-log infrastructure.

---

# AOP and Cross-Cutting Concerns

The project uses AOP to separate cross-cutting concerns from business logic.

Implemented aspects include:

## Audit Aspect

```text
AuditAspect
```

Responsible for audit-related cross-cutting processing.

## Idempotency Aspect

```text
IdempotencyAspect
```

Supports idempotent request processing.

## Logging Aspect

```text
LoggingAspect
```

Provides centralized logging around application operations.

## Performance Aspect

```text
PerformanceAspect
```

Measures execution time and identifies slow operations.

The configured development threshold is:

```text
500 ms
```


# Validation and Exception Handling

The application uses Bean Validation and custom validation components.

Examples include:

```text
StrongPassword
StrongPasswordValidator

ValidDateRange
DateRangeValidator

ValidationUtil
```

The application also defines domain-specific exceptions such as:

```text
ApplicationException
BusinessException
ForbiddenException
FileStorageException
IdempotencyException
InsufficientBalanceException
InvalidTransactionException
LoanEligibilityException
ResourceNotFoundException
UnauthorizedException
ValidationException
```

A centralized:

```text
GlobalExceptionHandler
```

converts exceptions into consistent REST API error responses.

Common response structures include:

```text
ApiResponse
ErrorResponse
FieldErrorResponse
PageResponse
```

---

# File Management

The application supports controlled local file storage.

Configured root directory:

```text
storage/files
```

Maximum development file size:

```text
2 MB
```

Supported extensions:

```text
pdf
jpg
jpeg
png
xlsx
docx
```

The file module provides:

```text
Upload
Metadata retrieval
Customer file listing
Download
Delete
```

File metadata is stored in the database while the actual file content is stored through the configured storage service.

The storage implementation is based on:

```text
FileStorageService
LocalFileStorageService
```


# Notifications and Events

The application uses application/domain events to decouple business operations from secondary processing.

Important events include:

```text
CustomerCreatedEvent
LoanApprovedEvent
TransactionCompletedEvent
InvestmentMaturityEvent
AuditEvent
DomainEvent
```

Event listeners include:

```text
AuditEventListener
NotificationEventListener
```

Example:

```text
Business Operation
       │
       ▼
Publish Domain Event
       │
       ├──────────────► Audit Listener
       │
       └──────────────► Notification Listener
```

This keeps secondary responsibilities separate from the primary business transaction flow.

---

# Schedulers

The application contains scheduled/background processing components.

Implemented schedulers include:

```text
InvestmentScheduler
LoanScheduler
NotificationScheduler
RefreshTokenCleanupScheduler
IdempotencyCleanupScheduler
```

Responsibilities include:

### Investment Scheduler

Processes investment maturity-related operations.

### Loan Scheduler

Performs loan monitoring/maintenance tasks.

### Notification Scheduler

Performs notification maintenance.

### Refresh Token Cleanup Scheduler

Removes expired/obsolete refresh-token records.

### Idempotency Cleanup Scheduler

Removes expired idempotency records.

Scheduler intervals are configurable.

---

# Reporting

Reporting APIs are available under:

```text
/api/v1/reports
```

Supported reports:

```text
Customer Report
Account Report
Transaction Report
Loan Report
Investment Report
```

Reporting uses a dedicated repository abstraction:

```text
ReportingRepository
ReportingRepositoryImpl
```

and query abstraction:

```text
ReportQuery
```

This keeps reporting queries separate from normal transactional repositories.

---

# API Documentation

OpenAPI documentation is generated using:

```text
springdoc-openapi
```

Development API documentation:

```text
http://localhost:8080/v3/api-docs
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

Swagger UI is enabled in the development profile.

API documentation can be disabled in production through configuration.

---

# Actuator and Monitoring

Spring Boot Actuator is included for operational monitoring.

Configured endpoints include:

```text
/actuator/health
/actuator/info
/actuator/metrics
```

Development:

```text
http://localhost:8080/actuator/health
http://localhost:8080/actuator/info
http://localhost:8080/actuator/metrics
```

Production management endpoints are configured separately and can use a dedicated management port.

Production defaults:

```text
Management Port: 8081
Management Address: 127.0.0.1
```

---

# Project Structure

The current implementation follows a feature-oriented package structure.

```text
BankApplication/
│
├── bank/
│   ├── pom.xml
│   ├── mvnw
│   ├── mvnw.cmd
│   │
│   └── src/
│       ├── main/
│       │   ├── java/
│       │   │   └── com/example/bank/
│       │   │       │
│       │   │       ├── account/
│       │   │       ├── admin/
│       │   │       ├── aspect/
│       │   │       ├── audit/
│       │   │       ├── auth/
│       │   │       ├── common/
│       │   │       ├── config/
│       │   │       ├── customer/
│       │   │       ├── employee/
│       │   │       ├── event/
│       │   │       ├── file/
│       │   │       ├── idempotency/
│       │   │       ├── investment/
│       │   │       ├── investmentperformance/
│       │   │       ├── loan/
│       │   │       ├── loanrepayment/
│       │   │       ├── notification/
│       │   │       ├── reporting/
│       │   │       ├── scheduler/
│       │   │       ├── security/
│       │   │       ├── transaction/
│       │   │       ├── user/
│       │   │       │
│       │   │       ├── BankApplication.java
│       │   │       ├── JwtKeyGenerator.java
│       │   │       └── PasswordHashGenerator.java
│       │   │
│       │   └── resources/
│       │       ├── application.yml
│       │       ├── application.properties
│       │       ├── application-dev.yml
│       │       ├── application-test.yml
│       │       ├── application-prod.yml
│       │       │
│       │       └── db/
│       │           └── migration/
│       │               ├── V1__create_users.sql
│       │               ├── V2__create_customers.sql
│       │               ├── ...
│       │               └── V18__correct_transaction_type_constraint.sql
│       │
│       └── test/
│           └── java/
│               └── com/example/bank/
│
├── swagger/
│   ├── swagger main page.png
│   ├── all endpoints.png
│   ├── customer endpoints.png
│   ├── employee endpoints.png
│   └── transactions endpoints.png
│
├── storage/
│   └── files/
│
└── BankApplication_Professional_Project_Documentation.pdf
```

> The repository may also contain generated build output such as `target/`. Build artifacts should not be treated as source code.

---

# Database Schema

The database is organized around the major banking domains.

Conceptually:

```text
                         ┌─────────────┐
                         │    Users    │
                         └──────┬──────┘
                                │
                    ┌───────────┴───────────┐
                    ▼                       ▼
             ┌─────────────┐         ┌─────────────┐
             │ Customers   │         │ Employees   │
             └──────┬──────┘         └─────────────┘
                    │
                    ▼
             ┌─────────────┐
             │  Accounts   │
             └──────┬──────┘
                    │
                    ▼
             ┌─────────────┐
             │Transactions │
             └─────────────┘

                    Customer
                       │
             ┌─────────┴─────────┐
             ▼                   ▼
        ┌──────────┐        ┌─────────────┐
        │  Loans   │        │ Investments │
        └────┬─────┘        └──────┬──────┘
             │                     │
             ▼                     ▼
      Loan Repayments       Investment Performance
```

Additional infrastructure tables support:

```text
Refresh Tokens
Notifications
Audit Logs
Idempotency Keys
File Metadata
```

---

# Prerequisites

Before running the project, install:

* Java 26
* MySQL 8.x or compatible MySQL version
* Git
* Maven, or use the included Maven Wrapper
* IDE such as IntelliJ IDEA, Eclipse, or VS Code

Verify Java:

```bash
java -version
```

Verify Git:

```bash
git --version
```

Verify MySQL:

```bash
mysql --version
```

---

# Database Setup

Create the database:

```sql
CREATE DATABASE bank;
```

The development profile uses:

```text
Database: bank
Host: localhost
Port: 3306
Username: root
```

The application is configured to use Flyway, so the schema should be created through the migration scripts rather than manually creating individual tables.

For local development, database credentials can be overridden through environment variables.

---

# Configuration

The main configuration is located at:

```text
src/main/resources/application.yml
```

Environment-specific configuration:

```text
application-dev.yml
application-test.yml
application-prod.yml
```

The default profile is:

```text
dev
```

---

## Development Database Variables

```text
DB_URL
DB_USERNAME
DB_PASSWORD
```

Example:

```text
DB_URL=jdbc:mysql://localhost:3306/bank?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
DB_USERNAME=root
DB_PASSWORD=your_password
```

---

# JWT Configuration

JWT uses an RSA key pair.

Required environment variables:

```text
JWT_PRIVATE_KEY_BASE64
JWT_PUBLIC_KEY_BASE64
```

Development configuration:

```yaml
bank:
  security:
    jwt:
      issuer: banking-application
      access-token-expiration: 60000
      refresh-token-expiration: 60
```

Production values should be supplied through environment variables.

Do **not** commit private JWT keys to source control.

---

# JWT Key Generation

The project includes:

```text
com.example.bank.JwtKeyGenerator
```

This utility generates an RSA 2048-bit key pair and prints Base64 encoded private/public keys.

Run it from the IDE as a Java application.

The output provides:

```text
JWT_PRIVATE_KEY_BASE64=
JWT_PUBLIC_KEY_BASE64=
```

Important:

* Do not add quotation marks.
* Do not add PEM headers or footers.
* Keep each value as one continuous Base64 string.
* Never expose or commit the private key.
* Use environment variables or a proper secret-management solution.

Example environment configuration:

```text
JWT_PRIVATE_KEY_BASE64=<private-key>
JWT_PUBLIC_KEY_BASE64=<public-key>
```

---

# Password Hash Generation

The project also contains:

```text
com.example.bank.PasswordHashGenerator
```

This utility demonstrates BCrypt password hashing.

Passwords should be stored only as BCrypt hashes.

Example:

```text
Plain Password
      │
      ▼
BCryptPasswordEncoder
      │
      ▼
BCrypt Hash
      │
      ▼
Database
```

Never store production passwords in plain text.

---

# Running the Application

## Option 1 — Maven Wrapper

From the `bank` directory:

### Windows

```cmd
mvnw.cmd spring-boot:run
```

### Linux/macOS

```bash
./mvnw spring-boot:run
```

---

## Option 2 — Maven

```bash
mvn spring-boot:run
```

---

## Option 3 — Build JAR

```bash
mvn clean package
```

Then run:

```bash
java -jar target/bank-0.0.1-SNAPSHOT.jar
```

---

# Running with a Specific Profile

Development:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Test:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=test
```

Production:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

For production deployments, environment variables should be supplied through the deployment environment rather than committed configuration files.

---

# Application URLs

Default development server:

```text
http://localhost:8080
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

Health:

```text
http://localhost:8080/actuator/health
```

Info:

```text
http://localhost:8080/actuator/info
```

Metrics:

```text
http://localhost:8080/actuator/metrics
```

---

# Testing

The project contains tests for:

* Application context
* User REST controller
* User service implementation

Test infrastructure also includes:

```text
Spring Boot Test
JUnit
Spring Security Test
Testcontainers
MySQL Testcontainer
```

Run all tests:

```bash
mvn test
```

Run a clean test build:

```bash
mvn clean test
```

Test profile configuration is available in:

```text
application-test.yml
```

The test configuration uses a separate database:

```text
banking_application_test
```

and enables Flyway migrations.

---

# Build and Package

Clean the project:

```bash
mvn clean
```

Compile:

```bash
mvn compile
```

Run tests:

```bash
mvn test
```

Package:

```bash
mvn package
```

Full verification:

```bash
mvn clean verify
```

Build without tests:

```bash
mvn clean package -DskipTests
```

---

# API Endpoint Reference

All versioned REST endpoints use:

```text
/api/v1
```

## Authentication

Base path:

```text
/api/v1/auth
```

| Method | Endpoint           | Purpose                         |
| ------ | ------------------ | ------------------------------- |
| POST   | `/login`           | Authenticate user               |
| POST   | `/change-password` | Change password                 |
| POST   | `/refresh`         | Refresh access token            |
| POST   | `/logout`          | Logout/invalidate refresh token |

---

## Users

Base path:

```text
/api/v1/users
```

| Method | Endpoint               | Purpose              |
| ------ | ---------------------- | -------------------- |
| POST   | `/`                    | Create user          |
| PUT    | `/{id}`                | Update user          |
| GET    | `/{id}`                | Get user             |
| GET    | `/username/{username}` | Get user by username |
| GET    | `/`                    | Get all users        |
| DELETE | `/{id}`                | Delete user          |

---

## Customers

Base path:

```text
/api/v1/customers
```

| Method | Endpoint                   | Purpose              |
| ------ | -------------------------- | -------------------- |
| POST   | `/`                        | Create customer      |
| PUT    | `/{id}`                    | Update customer      |
| GET    | `/{id}`                    | Get customer         |
| GET    | `/user/{userId}`           | Get customer by user |
| GET    | `/`                        | Get all customers    |
| DELETE | `/{id}`                    | Delete customer      |
| PATCH  | `/{customerId}/deactivate` | Deactivate customer  |
| PATCH  | `/{customerId}/activate`   | Activate customer    |

---

## Employees

Base path:

```text
/api/v1/employees
```

| Method | Endpoint         | Purpose              |
| ------ | ---------------- | -------------------- |
| POST   | `/`              | Create employee      |
| PUT    | `/{id}`          | Update employee      |
| GET    | `/{id}`          | Get employee         |
| GET    | `/user/{userId}` | Get employee by user |
| GET    | `/`              | Get all employees    |
| DELETE | `/{id}`          | Delete employee      |

---

## Administrators

Base path:

```text
/api/v1/admins
```

| Method | Endpoint           | Purpose                  |
| ------ | ------------------ | ------------------------ |
| POST   | `/`                | Create administrator     |
| PUT    | `/{id}`            | Update administrator     |
| GET    | `/{id}`            | Get administrator        |
| GET    | `/`                | Get all administrators   |
| PATCH  | `/{id}/deactivate` | Deactivate administrator |

Administrative account operation:

```text
/api/v1/admin/accounts/{userId}/deactivate
```

---

## Accounts

Base path:

```text
/api/v1/accounts
```

| Method | Endpoint                 | Purpose               |
| ------ | ------------------------ | --------------------- |
| POST   | `/`                      | Create account        |
| GET    | `/{id}`                  | Get account           |
| GET    | `/customer/{customerId}` | Get customer accounts |
| GET    | `/`                      | Get accounts          |
| PATCH  | `/{id}/block`            | Block account         |
| PATCH  | `/{id}/activate`         | Activate account      |
| PATCH  | `/{id}/deactivate`       | Deactivate account    |
| PATCH  | `/{id}/close`            | Close account         |

---

## Transactions

Base path:

```text
/api/v1/transactions
```

| Method | Endpoint               | Purpose                  |
| ------ | ---------------------- | ------------------------ |
| POST   | `/deposit`             | Deposit money            |
| POST   | `/withdraw`            | Withdraw money           |
| POST   | `/transfer`            | Transfer money           |
| GET    | `/{id}`                | Get transaction          |
| GET    | `/account/{accountId}` | Get account transactions |
| POST   | `/{id}/reverse`        | Reverse transaction      |

Financial operations should be treated as sensitive operations and require appropriate authentication, authorization, validation, and transactional processing.

---

## Loans

Base path:

```text
/api/v1/loans
```

| Method | Endpoint                    | Purpose                |
| ------ | --------------------------- | ---------------------- |
| POST   | `/`                         | Apply for loan         |
| GET    | `/{loanId}`                 | Get loan               |
| GET    | `/customer/{customerId}`    | Get customer loans     |
| GET    | `/`                         | Get loans              |
| GET    | `/eligibility/{customerId}` | Check loan eligibility |
| PATCH  | `/{loanId}/approve`         | Approve loan           |
| PATCH  | `/{loanId}/reject`          | Reject loan            |
| PATCH  | `/{loanId}/complete`        | Complete loan          |
| POST   | `/{loanId}/disbursements`   | Disburse loan          |

---

## Loan Repayments

Base path:

```text
/api/v1/loan-repayments
```

| Method | Endpoint         | Purpose             |
| ------ | ---------------- | ------------------- |
| POST   | `/loan/{loanId}` | Make repayment      |
| GET    | `/{repaymentId}` | Get repayment       |
| GET    | `/loan/{loanId}` | Get loan repayments |
| GET    | `/`              | Get repayments      |

---

## Investments

Base path:

```text
/api/v1/investments
```

| Method | Endpoint                 | Purpose                  |
| ------ | ------------------------ | ------------------------ |
| POST   | `/`                      | Create investment        |
| GET    | `/{investmentId}`        | Get investment           |
| GET    | `/customer/{customerId}` | Get customer investments |
| GET    | `/`                      | Get investments          |
| PATCH  | `/{investmentId}/mature` | Mature investment        |
| PATCH  | `/{investmentId}/settle` | Settle investment        |

---

## Investment Performance

Base path:

```text
/api/v1/investment-performance
```

| Method | Endpoint                            | Purpose                    |
| ------ | ----------------------------------- | -------------------------- |
| POST   | `/`                                 | Record performance         |
| GET    | `/{performanceId}`                  | Get performance            |
| GET    | `/investment/{investmentId}`        | Get investment performance |
| GET    | `/investment/{investmentId}/latest` | Get latest performance     |

---

## Notifications

Base path:

```text
/api/v1/notifications
```

| Method | Endpoint                 | Purpose                        |
| ------ | ------------------------ | ------------------------------ |
| GET    | `/`                      | Get notifications              |
| GET    | `/unread`                | Get unread notifications       |
| GET    | `/{notificationId}`      | Get notification               |
| PATCH  | `/{notificationId}/read` | Mark notification as read      |
| PATCH  | `/read-all`              | Mark all notifications as read |

---

## Files

Base path:

```text
/api/v1/files
```

| Method | Endpoint                 | Purpose            |
| ------ | ------------------------ | ------------------ |
| POST   | `/`                      | Upload file        |
| GET    | `/{fileId}`              | Get file metadata  |
| GET    | `/customer/{customerId}` | Get customer files |
| GET    | `/{fileId}/download`     | Download file      |
| DELETE | `/{fileId}`              | Delete file        |

File upload uses:

```text
multipart/form-data
```

---

## Audit Logs

Base path:

```text
/api/v1/audit-logs
```

| Method | Endpoint | Purpose           |
| ------ | -------- | ----------------- |
| GET    | `/`      | Search audit logs |
| GET    | `/{id}`  | Get audit log     |

---

## Reports

Base path:

```text
/api/v1/reports
```

| Method | Endpoint        | Purpose            |
| ------ | --------------- | ------------------ |
| POST   | `/customers`    | Customer report    |
| POST   | `/accounts`     | Account report     |
| GET    | `/transactions` | Transaction report |
| GET    | `/loans`        | Loan report        |
| GET    | `/investments`  | Investment report  |

For exact request fields, query parameters, response models, security requirements, and schemas, use the generated OpenAPI documentation.

---

# API Request Authentication

Protected endpoints require a bearer token.

Example:

```http
Authorization: Bearer <access-token>
```

Typical workflow:

```text
1. Login
2. Receive access token
3. Store access token securely on the client
4. Send Authorization header
5. Refresh when access token expires
6. Logout when session ends
```

---

# Standard API Response Handling

The project provides reusable response classes including:

```text
ApiResponse
ErrorResponse
FieldErrorResponse
PageResponse
```

This provides a consistent structure for:

* Successful responses
* Error responses
* Validation errors
* Paginated responses

---

# Pagination

The application contains reusable pagination support through:

```text
PageResponse
PageableUtil
```

Reporting and collection APIs can expose paginated data where supported.

Typical pagination concepts include:

```text
page
size
sort
```

The exact accepted parameters depend on the individual controller.

---

# Common HTTP Status Codes

The API follows standard HTTP semantics where applicable.

| Status | Meaning                                    |
| -----: | ------------------------------------------ |
|    200 | Successful request                         |
|    201 | Resource created                           |
|    204 | Successful operation with no response body |
|    400 | Invalid request / validation failure       |
|    401 | Authentication required/failed             |
|    403 | Access denied                              |
|    404 | Resource not found                         |
|    409 | Business conflict / duplicate operation    |
|    500 | Unexpected server-side failure             |

---

# Development Profiles

## `dev`

Used for local development.

Characteristics:

* Server port: `8080`
* Swagger enabled
* OpenAPI enabled
* Debug logging for application package
* SQL logging enabled
* Health details available
* Local MySQL configuration
* Caffeine cache enabled

---

## `test`

Used for automated testing.

Characteristics:

* Random server port
* Separate test database
* Flyway enabled
* Reduced logging
* Swagger disabled
* Health information restricted
* Test-specific file storage

---

## `prod`

Used for production deployment.

Characteristics:

* Environment-based database configuration
* Environment-based JWT configuration
* Larger configurable cache
* Production-oriented logging
* Swagger/OpenAPI disabled by default
* Separate management port
* Restricted management binding
* Sensitive error details hidden
* Environment-based file storage
* Longer configurable idempotency retention

---

# Production Configuration

Production configuration should be supplied through environment variables or a secure configuration/secret-management system.

Important variables include:

```text
DB_URL
DB_USERNAME
DB_PASSWORD

JWT_ISSUER
JWT_PRIVATE_KEY_BASE64
JWT_PUBLIC_KEY_BASE64
JWT_ACCESS_TOKEN_EXPIRATION
JWT_REFRESH_TOKEN_EXPIRATION

SERVER_PORT

CACHE_MAX_SIZE
CACHE_EXPIRE_AFTER_WRITE

FILE_STORAGE_ROOT
FILE_MAX_SIZE
FILE_MAX_REQUEST_SIZE
FILE_MAX_SIZE_BYTES
MAX_FILES_PER_OWNER

IDEMPOTENCY_TTL_MINUTES

PERFORMANCE_SLOW_METHOD_THRESHOLD_MS

SPRINGDOC_API_DOCS_ENABLED
SPRINGDOC_SWAGGER_UI_ENABLED

MANAGEMENT_SERVER_PORT
MANAGEMENT_SERVER_ADDRESS
```

Secrets must never be committed to Git.

---

# Production Considerations

This project contains several production-oriented patterns, but production deployment still requires environment-specific infrastructure and operational controls.

Before deploying to a real banking environment, consider:

* HTTPS/TLS termination
* Secure secret management
* Database backups
* Database replication/high availability
* Connection-pool sizing
* Distributed cache if multiple application instances are deployed
* Centralized logging
* Centralized monitoring
* Alerting
* Rate limiting
* API gateway
* WAF
* Network segmentation
* Key rotation
* JWT key lifecycle management
* Secure file storage
* Malware scanning for uploaded files
* Disaster recovery
* Audit retention policies
* Data retention requirements
* Regulatory compliance
* PII/data protection
* Encryption at rest
* Encryption in transit
* Database access controls
* Operational runbooks

The application should therefore be considered a **production-oriented reference implementation**, not a certification of regulatory or banking production compliance.

---

# Important Security Practices

Never commit:

```text
JWT_PRIVATE_KEY_BASE64
Database passwords
Production credentials
API keys
Private certificates
Secret tokens
```

Use environment variables or an enterprise secret manager.

Do not use development credentials in production.

Do not expose:

```text
/actuator/*
```

unnecessarily to the public internet.

Swagger/OpenAPI should normally remain disabled or access-controlled in production.

---

Do not ignore migration scripts, source code, tests, configuration templates, or documentation that belongs in the repository.

---

# Project Documentation

The repository includes professional project documentation:

```text
BankApplication_Professional_Project_Documentation.pdf
```

The documentation covers the broader application design, API flow, technology stack, functionalities, and project details.

Swagger reference screenshots are also included under:

```text
swagger/
```

Available references include:

```text
swagger main page.png
all endpoints.png
customer endpoints.png
employee endpoints.png
transactions endpoints.png
```

---

# Package Responsibilities

The application follows a feature-oriented structure rather than placing every controller, service, repository, and entity into global technical packages.

For example:

```text
customer/
├── controller/
│   └── rest/
├── dto/
│   ├── request/
│   └── response/
├── entity/
├── mapper/
├── repository/
└── service/
```

This approach keeps each business capability together and makes the codebase easier to navigate and extend.

---

# Design Principles Used

The implementation follows several important software engineering principles:

### Separation of Concerns

Controllers, services, repositories, security, mapping, validation, and infrastructure have separate responsibilities.

### Interface-Based Service Design

Business services are generally separated into:

```text
Service
ServiceImpl
```

### DTO-Based API Design

REST APIs expose request/response DTOs rather than directly exposing persistence entities.

### Mapping Layer

MapStruct is used for DTO/entity conversion.

### Centralized Exception Handling

Application errors are converted into consistent REST responses.

### Transactional Business Operations

Critical financial operations are handled with transactional processing.

### Database Version Control

Flyway manages schema evolution.

### Cross-Cutting Concerns

AOP handles concerns such as:

```text
Logging
Auditing
Performance monitoring
Idempotency
Security auditing
```

### Domain Events

Application events decouple secondary processing from core business operations.

---

# Important Domain Constraints

The application treats financial operations differently from ordinary CRUD operations.

Examples include:

## Money Transfer

A transfer must consider:

* Source account validity
* Destination account validity
* Account status
* Ownership/authorization
* Available balance
* Transaction validity
* Duplicate request protection
* Transactional consistency

## Withdrawal

A withdrawal must consider:

* Account validity
* Account status
* Ownership/authorization
* Available balance
* Transaction rules

## Loan Approval

Loan approval must consider:

* Loan existence
* Current loan status
* Authorization
* Business eligibility/state
* Valid state transition

## Investment Maturity

Investment maturity is processed through business rules and scheduled processing.

---

# Error Handling Philosophy

The application avoids exposing internal implementation details to API consumers.

Production configuration explicitly prevents exposing:

```text
Stack traces
Exception class names
Binding internals
Sensitive error information
```

Errors are returned through application-specific response models.

---

# Observability

The project provides multiple levels of observability:

```text
Application Logging
       │
       ├── Logging Aspect
       │
       ├── Performance Aspect
       │
       └── Security Audit
               │
               ▼
        Audit Infrastructure

Spring Actuator
       │
       ├── Health
       ├── Info
       └── Metrics
```

This allows application behavior and operational health to be monitored separately from business responses.

---

# Extensibility

The modular design allows additional banking capabilities to be introduced without restructuring the entire application.

Potential future modules could include:

```text
Cards
Beneficiaries
Standing Instructions
Fixed Deposits
Recurring Deposits
Credit Scoring
KYC Management
Fraud Detection
Payment Gateway Integration
External Banking Integration
Email/SMS Providers
Multi-factor Authentication
```

These are not part of the current documented implementation unless explicitly added to the source code.

---

# Project Status

The current repository represents the completed **Banking Application REST API project** with the implemented modules and infrastructure described in this README.

The implementation includes:

* REST API
* JWT authentication
* Role-based authorization
* Ownership authorization
* BCrypt password hashing
* Flyway migrations
* MySQL persistence
* JPA auditing
* DTO mapping
* Validation
* Global exception handling
* AOP
* Idempotency
* Caffeine caching
* Domain/application events
* Notifications
* File management
* Scheduled processing
* Reporting
* OpenAPI / Swagger
* Actuator
* Automated tests
* Testcontainers support

---

# Quick Start

```bash
# 1. Clone repository
git clone <repository-url>

# 2. Enter project
cd BankApplication/bank

# 3. Configure MySQL

# 4. Configure JWT RSA keys
#    JWT_PRIVATE_KEY_BASE64
#    JWT_PUBLIC_KEY_BASE64

# 5. Run application
mvnw.cmd spring-boot:run
```

Then open:

```text
Swagger:
http://localhost:8080/swagger-ui.html

OpenAPI:
http://localhost:8080/v3/api-docs

Health:
http://localhost:8080/actuator/health
```

---

# License

This project is intended for educational, demonstration, and professional project-development purposes.

If this repository is distributed or extended for commercial use, the applicable licensing, security, compliance, and operational requirements should be established separately.

---

# Author

**Banking Application — Java Final Project**

Technology focus:

```text
Java 26
Spring Boot
Spring Security
JWT
MySQL
JPA / Hibernate
Flyway
MapStruct
Lombok
AOP
Caffeine
OpenAPI
Actuator
Testcontainers
```
