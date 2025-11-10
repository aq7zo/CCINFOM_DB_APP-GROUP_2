# Cybersecurity Incident Reporting System - Architecture Documentation

## Table of Contents
1. [System Overview](#system-overview)
2. [Architecture Pattern](#architecture-pattern)
3. [Technology Stack](#technology-stack)
4. [Project Structure](#project-structure)
5. [Component Architecture](#component-architecture)
6. [Database Architecture](#database-architecture)
7. [Data Flow](#data-flow)
8. [Key Features & Business Logic](#key-features--business-logic)
9. [Security Architecture](#security-architecture)
10. [Design Patterns](#design-patterns)
11. [Dependencies & Integration](#dependencies--integration)

---

## System Overview

The **Cybersecurity Incident Reporting System** is a Java-based console application designed to manage and track cybersecurity incidents. The system enables administrators to register victims, track perpetrators, create incident reports, manage evidence, and generate comprehensive analytics reports.

### Core Purpose
- Track and manage cybersecurity incidents
- Monitor victim accounts and perpetrator threat levels
- Automate risk assessment and escalation
- Generate compliance-ready reports with privacy protection
- Maintain audit trails for all system activities

---

## Architecture Pattern

The application follows the **Model-View-Controller (MVC)** architectural pattern, which provides clear separation of concerns:

```
┌─────────────┐
│    View     │  ← User Interface (Console)
│  (Presentation)
└──────┬──────┘
       │
       ↓
┌─────────────┐
│ Controller  │  ← Business Logic & Coordination
│  (Logic)
└──────┬──────┘
       │
       ↓
┌─────────────┐
│   Model     │  ← Data Models & Database Access
│   (Data)
└─────────────┘
```

### MVC Components

- **Model**: Data entities and database operations
- **View**: Console-based user interface (designed for future GUI upgrade)
- **Controller**: Business logic, validation, and coordination between Model and View

---

## Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| **Language** | Java | JDK 17+ |
| **Database** | MySQL | 8.0+ |
| **Build Tool** | Maven | 3.x |
| **Database Driver** | MySQL Connector/J | 8.0.33 |
| **Architecture** | MVC Pattern | - |
| **Interface** | Console-based | (Future: JavaFX/Swing) |

---

## Project Structure

```
DB APP/
├── src/
│   └── main/
│       └── java/
│           ├── Main.java                    # Application entry point
│           ├── controller/                  # Business logic layer
│           │   ├── AdminController.java
│           │   ├── AttackTypeController.java
│           │   ├── EvidenceController.java
│           │   ├── IncidentController.java
│           │   ├── PerpetratorController.java
│           │   ├── ReportController.java
│           │   └── VictimController.java
│           ├── model/                       # Data layer
│           │   ├── Administrator.java
│           │   ├── AttackType.java
│           │   ├── DatabaseConnection.java
│           │   ├── Evidence.java
│           │   ├── IncidentReport.java
│           │   ├── Perpetrator.java
│           │   ├── ThreatLevelLog.java
│           │   ├── Victim.java
│           │   └── VictimStatusLog.java
│           ├── view/                        # Presentation layer
│           │   ├── ConsoleMenu.java
│           │   ├── PerpetratorView.java
│           │   ├── ReportView.java
│           │   └── VictimView.java
│           └── utils/                       # Utility classes
│               ├── DateUtils.java
│               ├── ReportGenerator.java
│               ├── SecurityUtils.java
│               └── ValidationUtils.java
├── DB_SCHEMA.sql                            # Database schema definition
├── pom.xml                                  # Maven configuration
├── README.md                                # User documentation
└── ARCHITECTURE.md                          # This file
```

---

## Component Architecture

### 1. Model Layer (`model/`)

The Model layer contains data entities and database connection management.

#### Core Models

| Model | Purpose | Key Fields |
|-------|---------|-----------|
| **Victim** | Represents a victim entity | `victimID`, `name`, `contactEmail`, `accountStatus`, `dateCreated` |
| **Perpetrator** | Represents an offender | `perpetratorID`, `identifier`, `identifierType`, `threatLevel`, `lastIncidentDate` |
| **IncidentReport** | Links victims, perpetrators, and attacks | `incidentID`, `victimID`, `perpetratorID`, `attackTypeID`, `status`, `dateReported` |
| **Evidence** | Evidence files for incidents | `evidenceID`, `incidentID`, `evidenceType`, `filePath`, `verifiedStatus` |
| **AttackType** | Predefined attack categories | `attackTypeID`, `attackName`, `description`, `severityLevel` |
| **Administrator** | System users | `adminID`, `name`, `role`, `contactEmail` |
| **ThreatLevelLog** | Audit trail for threat level changes | `logID`, `perpetratorID`, `oldThreatLevel`, `newThreatLevel`, `changeDate` |
| **VictimStatusLog** | Audit trail for status changes | `logID`, `victimID`, `oldStatus`, `newStatus`, `changeDate` |

#### DatabaseConnection

Singleton-like connection manager:
- **Purpose**: Centralized database connection management
- **Pattern**: Static connection pool (single connection)
- **Methods**:
  - `getConnection()`: Returns active database connection
  - `closeConnection()`: Closes the connection
  - `setCredentials()`: Configuration method (extensible)

**Connection Details**:
- URL: `jdbc:mysql://localhost:3306/CybersecurityDB`
- Driver: MySQL Connector/J
- Connection pooling: Single connection (can be extended)

---

### 2. Controller Layer (`controller/`)

Controllers handle business logic, validation, and coordinate between Models and Views.

#### Controller Responsibilities

1. **Data Validation**: Input validation using `ValidationUtils`
2. **Database Operations**: CRUD operations via Prepared Statements
3. **Business Rules**: Auto-flagging, auto-escalation logic
4. **Error Handling**: SQL exception handling and user feedback

#### Controllers Overview

| Controller | Primary Responsibilities |
|------------|-------------------------|
| **VictimController** | Victim CRUD, auto-flagging (>5 incidents/month), status management |
| **PerpetratorController** | Perpetrator CRUD, auto-escalation (≥3 victims/7 days), threat level management |
| **IncidentController** | Incident creation, validation, retrieval, triggers auto-checks |
| **EvidenceController** | Evidence upload, verification, status updates |
| **AttackTypeController** | Attack type management (CRUD) |
| **AdminController** | Administrator registration, authentication, management |
| **ReportController** | Report generation coordination |

#### Key Business Logic

**Auto-Flagging (VictimController)**:
```java
// Triggers when victim reports >5 incidents within a month
// Status changes: Active → Flagged
// Logged in VictimStatusLog
```

**Auto-Escalation (PerpetratorController)**:
```java
// Triggers when perpetrator has ≥3 unique victims within 7 days
// Threat level changes: UnderReview/Suspected → Malicious
// Logged in ThreatLevelLog
```

---

### 3. View Layer (`view/`)

The View layer handles user interaction and presentation.

#### View Components

| View | Purpose |
|------|---------|
| **ConsoleMenu** | Main menu navigation and routing |
| **VictimView** | Victim management interface |
| **PerpetratorView** | Perpetrator management interface |
| **ReportView** | Report generation interface |

#### Design Philosophy

- **Console-based**: Current implementation uses console I/O
- **Future-ready**: Designed for easy migration to GUI (JavaFX/Swing)
- **Separation**: Views only handle presentation, no business logic

---

### 4. Utility Layer (`utils/`)

Utility classes provide cross-cutting functionality.

#### Utility Classes

| Utility | Purpose | Key Methods |
|---------|---------|-------------|
| **DateUtils** | Date/time formatting and conversion | `formatForDisplay()`, `fromDatabaseFormat()` |
| **ValidationUtils** | Input validation | `isValidEmail()`, `isNotEmpty()`, enum validation |
| **SecurityUtils** | Security operations | `encrypt()`, `decrypt()`, `hashPassword()`, `anonymizeName()`, `anonymizeEmail()` |
| **ReportGenerator** | Report generation | `generateMonthlyAttackTrends()`, `generateTopPerpetrators()`, `generateVictimActivity()`, `generateEvidenceSummary()` |

#### Security Utilities

- **Encryption**: Base64 encoding (extensible to AES)
- **Hashing**: SHA-256 for passwords
- **Anonymization**: RA 10173 compliance (Philippine Data Privacy Act)
  - Names: First letter + "***"
  - Emails: First 2 chars + "***@domain"

---

## Database Architecture

### Database Schema

**Database Name**: `CybersecurityDB`

#### Entity Tables

1. **Victims**
   - Primary Key: `VictimID` (AUTO_INCREMENT)
   - Unique: `ContactEmail`
   - Status: ENUM('Active', 'Flagged', 'Suspended')

2. **Perpetrators**
   - Primary Key: `PerpetratorID` (AUTO_INCREMENT)
   - Unique: `Identifier`
   - Threat Level: ENUM('UnderReview', 'Suspected', 'Malicious', 'Cleared')

3. **AttackTypes**
   - Primary Key: `AttackTypeID` (AUTO_INCREMENT)
   - Severity: ENUM('Low', 'Medium', 'High')

4. **Administrators**
   - Primary Key: `AdminID` (AUTO_INCREMENT)
   - Unique: `ContactEmail`
   - Role: ENUM('System Admin', 'Cybersecurity Staff')

#### Transaction Tables

5. **IncidentReports**
   - Primary Key: `IncidentID` (AUTO_INCREMENT)
   - Foreign Keys: `VictimID`, `PerpetratorID`, `AttackTypeID`, `AdminID`
   - Status: ENUM('Pending', 'Validated')
   - Cascade: ON DELETE CASCADE for Victims/Perpetrators

6. **EvidenceUpload**
   - Primary Key: `EvidenceID` (AUTO_INCREMENT)
   - Foreign Keys: `IncidentID`, `AdminID`
   - Evidence Type: ENUM('Screenshot', 'Email', 'File', 'Chat Log')
   - Verified Status: ENUM('Pending', 'Verified', 'Rejected')
   - Cascade: ON DELETE CASCADE for Incidents

#### Audit Log Tables

7. **ThreatLevelLog**
   - Primary Key: `LogID` (AUTO_INCREMENT)
   - Foreign Keys: `PerpetratorID`, `AdminID`
   - Tracks: Old/New threat levels, change date

8. **VictimStatusLog**
   - Primary Key: `LogID` (AUTO_INCREMENT)
   - Foreign Keys: `VictimID`, `AdminID`
   - Tracks: Old/New status, change date

### Relationships

```
Victims ──┐
          ├──→ IncidentReports ←── Perpetrators
AttackTypes ──┘                    │
                                   │
Administrators ────────────────────┴──→ EvidenceUpload
                                   │
                                   └──→ ThreatLevelLog
                                   └──→ VictimStatusLog
```

### Database Features

- **Referential Integrity**: Foreign key constraints with CASCADE deletes
- **Data Types**: Appropriate ENUMs for status fields
- **Timestamps**: Automatic `CURRENT_TIMESTAMP` for creation dates
- **Indexing**: Primary keys and unique constraints provide indexing

---

## Data Flow

### Typical User Flow

```
1. User Input (View)
   ↓
2. Validation (Controller)
   ↓
3. Business Logic (Controller)
   ↓
4. Database Operation (Model)
   ↓
5. Result Processing (Controller)
   ↓
6. Display (View)
```

### Example: Creating an Incident Report

```
ConsoleMenu.showMainMenu()
    ↓
User selects "Create Incident Report"
    ↓
ConsoleMenu.createIncidentReport()
    ↓
IncidentController.createIncidentReport()
    ├──→ Validates inputs
    ├──→ Creates IncidentReport record
    ├──→ Updates Perpetrator.lastIncidentDate
    ├──→ PerpetratorController.checkAndAutoEscalate()
    │   └──→ Checks if ≥3 victims in 7 days
    │   └──→ Escalates threat level if needed
    │   └──→ Logs to ThreatLevelLog
    └──→ VictimController.checkAndAutoFlagVictims()
        └──→ Checks if >5 incidents in month
        └──→ Flags victim if needed
        └──→ Logs to VictimStatusLog
    ↓
Success message displayed
```

### Report Generation Flow

```
ReportView.showReportMenu()
    ↓
User selects report type
    ↓
ReportController routes to ReportGenerator
    ↓
ReportGenerator queries database
    ├──→ Complex SQL joins
    ├──→ Aggregations (COUNT, GROUP BY)
    └──→ Privacy anonymization (SecurityUtils)
    ↓
Formatted report string returned
    ↓
Displayed in console
```

---

## Key Features & Business Logic

### 1. Automated Risk Assessment

#### Victim Auto-Flagging
- **Trigger**: Victim reports >5 incidents within a calendar month
- **Action**: Status changes from `Active` → `Flagged`
- **Logging**: Entry created in `VictimStatusLog`
- **Implementation**: `VictimController.checkAndAutoFlagVictims()`

#### Perpetrator Auto-Escalation
- **Trigger**: Perpetrator targets ≥3 unique victims within 7 days
- **Action**: Threat level escalates to `Malicious`
- **Logging**: Entry created in `ThreatLevelLog`
- **Implementation**: `PerpetratorController.checkAndAutoEscalate()`

### 2. Evidence Management

- **Upload**: Link evidence files to incidents
- **Verification**: Admins can verify/reject evidence
- **Status Tracking**: Pending → Verified/Rejected workflow
- **Audit Trail**: Admin ID tracked for verification actions

### 3. Report Generation

#### Available Reports

1. **Monthly Attack Trends**
   - Breakdown by attack type
   - Severity analysis
   - Time of day patterns
   - Evidence counts

2. **Top Perpetrators**
   - Ranked by incident count
   - Victim count per perpetrator
   - Associated attack types
   - Threat level display

3. **Victim Activity**
   - Incident counts per victim
   - Unique perpetrator counts
   - Privacy-compliant anonymization
   - Highlights frequently targeted victims (>5 incidents)

4. **Evidence Summary**
   - All evidence with verification status
   - Admin review tracking
   - Summary statistics

### 4. Privacy Compliance

- **RA 10173 Compliance**: Philippine Data Privacy Act
- **Anonymization**: Names and emails anonymized in reports
- **Data Protection**: Encryption utilities available (extensible)

---

## Security Architecture

### Security Measures

1. **SQL Injection Prevention**
   - All queries use Prepared Statements
   - Parameterized queries throughout

2. **Input Validation**
   - Email format validation
   - Non-empty checks
   - Enum value validation

3. **Data Privacy**
   - Anonymization for reports
   - Encryption utilities (Base64, extensible to AES)
   - Password hashing (SHA-256)

4. **Audit Trails**
   - All status changes logged
   - Admin actions tracked
   - Timestamp tracking

### Security Utilities

**SecurityUtils** provides:
- `encrypt()` / `decrypt()`: Data encryption (Base64, extensible)
- `hashPassword()`: SHA-256 password hashing
- `anonymizeName()`: Privacy-compliant name masking
- `anonymizeEmail()`: Privacy-compliant email masking

---

## Design Patterns

### 1. MVC Pattern
- **Separation of Concerns**: Clear boundaries between layers
- **Maintainability**: Easy to modify individual layers
- **Testability**: Controllers can be tested independently

### 2. Singleton Pattern (DatabaseConnection)
- **Single Connection**: One database connection instance
- **Resource Management**: Centralized connection handling

### 3. DAO Pattern (Data Access Object)
- **Model Classes**: Represent database entities
- **Controller Methods**: Act as DAOs for database operations

### 4. Utility Pattern
- **Static Methods**: Utility classes with static helper methods
- **Cross-cutting Concerns**: Reusable across all layers

---

## Dependencies & Integration

### Maven Dependencies

```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.0.33</version>
</dependency>
```

### External Dependencies

- **MySQL Server**: Required for database operations
- **JDK 17+**: Java runtime and compilation
- **Maven**: Build and dependency management (optional)

### Integration Points

1. **Database**: MySQL via JDBC
2. **File System**: Evidence file path storage (not file handling)
3. **Console I/O**: Standard input/output for user interaction

---

## Future Enhancements

### Planned Improvements

1. **GUI Migration**
   - Replace console views with JavaFX/Swing
   - Controllers remain unchanged
   - Models remain unchanged

2. **Enhanced Security**
   - AES encryption for sensitive data
   - JWT-based authentication
   - Role-based access control (RBAC)

3. **Connection Pooling**
   - HikariCP or similar connection pool
   - Better resource management

4. **Configuration Management**
   - External configuration file
   - Environment variable support
   - Database credentials management

5. **Logging Framework**
   - SLF4J + Logback
   - Structured logging
   - Log file rotation

6. **Testing**
   - Unit tests for controllers
   - Integration tests for database operations
   - Mock database for testing

---

## Conclusion

The Cybersecurity Incident Reporting System is built with a clean MVC architecture that promotes maintainability, scalability, and security. The separation of concerns allows for easy future enhancements, particularly the migration to a GUI interface without changing core business logic.

The system implements automated risk assessment, comprehensive reporting, and privacy compliance features, making it suitable for real-world cybersecurity incident management.

---

**Document Version**: 1.0  
**Last Updated**: 2024  
**Author**: System Architecture Documentation

