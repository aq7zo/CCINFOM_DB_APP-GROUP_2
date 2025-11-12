# Architectural Design Document
## PhishNet - Cybersecurity Incident Reporting System

**Version:** 2.0 (JavaFX Integration)  
**Last Updated:** 2025  
**Architecture Pattern:** Layered Architecture with MVC

---

## Table of Contents

1. [System Overview](#system-overview)
2. [Architecture Patterns](#architecture-patterns)
3. [Layer Architecture](#layer-architecture)
4. [Component Details](#component-details)
5. [Data Flow](#data-flow)
6. [Design Patterns](#design-patterns)
7. [Security Architecture](#security-architecture)
8. [Database Architecture](#database-architecture)
9. [UI Architecture](#ui-architecture)
10. [Dependencies & Technologies](#dependencies--technologies)
11. [Deployment Architecture](#deployment-architecture)

---

## System Overview

### Purpose
PhishNet is a desktop application for reporting and managing cybersecurity incidents, specifically phishing attacks. It provides a secure platform for victims to report incidents and administrators to manage and analyze threat data.

### Key Features
- **User Authentication**: Secure login system for victims and administrators
- **User Registration**: Public sign-up for victims
- **Incident Reporting**: (Planned) Report cybersecurity incidents
- **Administrative Dashboard**: (Planned) Manage incidents and perpetrators
- **Data Privacy Compliance**: RA 10173 (Philippine Data Privacy Act) compliance

### Technology Stack
- **Frontend**: JavaFX 21+ with FXML
- **Backend**: Java 17+
- **Database**: MySQL 8.0+
- **Build Tool**: Apache Maven
- **Architecture**: Layered Architecture with MVC

---

## Architecture Patterns

### Primary Pattern: Layered Architecture

The application follows a **strict layered architecture** where each layer has specific responsibilities and can only communicate with adjacent layers:

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                        │
│  ┌──────────────┐  ┌────────────────────────────────────┐  │
│  │   JavaFX    │  │      Controllers (MVC)             │  │
│  │   FXML      │  │  - LoginController                  │  │
│  │   Views     │  │  - SignUpController                 │  │
│  │             │  │  - AdminLoginController             │  │
│  └──────────────┘  └────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                            ↓ ↑
┌─────────────────────────────────────────────────────────────┐
│                    SERVICE LAYER                            │
│  ┌──────────────────────┐  ┌──────────────────────────┐    │
│  │ Authentication       │  │  Business Logic         │    │
│  │ Services            │  │  - Validation           │    │
│  │ - VictimAuth        │  │  - Error Handling       │    │
│  │ - AdminAuth         │  │  - Transaction Mgmt    │    │
│  └──────────────────────┘  └──────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
                            ↓ ↑
┌─────────────────────────────────────────────────────────────┐
│                    DAO LAYER                                │
│  ┌──────────────────────┐  ┌──────────────────────────┐    │
│  │  DAO Interfaces      │  │  DAO Implementations     │    │
│  │  - AdministratorDAO   │  │  - AdministratorDAOImpl│    │
│  │  - VictimDAO         │  │  - VictimDAOImpl        │    │
│  └──────────────────────┘  └──────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
                            ↓ ↑
┌─────────────────────────────────────────────────────────────┐
│                    UTILITY LAYER                            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  Database    │  │  Security    │  │  Validation  │      │
│  │  Connection  │  │  Utils       │  │  Utils       │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                            ↓ ↑
┌─────────────────────────────────────────────────────────────┐
│                    DATABASE LAYER                           │
│              MySQL Database (CybersecurityDB)               │
└─────────────────────────────────────────────────────────────┘
```

### Secondary Pattern: MVC (Model-View-Controller)

Within the Presentation Layer, the application uses MVC:

- **Model**: Domain objects (`Administrator.java`, `Victim.java`)
- **View**: FXML files (`LogIn.fxml`, `SignUp.fxml`, etc.)
- **Controller**: JavaFX controllers (`LoginController.java`, etc.)

---

## Layer Architecture

### 1. Presentation Layer

**Location**: `src/main/java/com/group2/dbapp/controller/`  
**Resources**: `src/main/resources/SceneBuilder/`

#### Responsibilities
- Handle user interface interactions
- Process user input
- Navigate between views
- Display data to users
- Show alerts and error messages
- Basic input validation (format checks)

#### Components

| Component | Purpose | Key Methods |
|-----------|---------|--------------|
| `LoginController` | Victim login | `handleLogin()`, `handleSignUp()`, `handleAdminLogin()` |
| `SignUpController` | User registration | `handleSignUp()`, `handleBackToLogin()`, `handleAdminLogin()` |
| `AdminLoginController` | Admin authentication | `handleLogin()`, `handleSignUp()`, `handleAdminLogin()` |

#### FXML Views

| View File | Controller | Purpose |
|-----------|------------|---------|
| `login uis/LogIn.fxml` | `LoginController` | Victim login interface |
| `login uis/SignUp.fxml` | `SignUpController` | User registration interface |
| `login uis/AdminLogin.fxml` | `AdminLoginController` | Administrator login interface |

#### Communication Flow
```
User Action → FXML View → Controller Method → Service Layer
```

---

### 2. Service Layer

**Location**: `src/main/java/com/group2/dbapp/service/`

#### Responsibilities
- Implement business logic and rules
- Coordinate between controllers and DAOs
- Data validation (business rules)
- Error handling and logging
- Transaction management (future)
- Security operations (authentication)

#### Components

| Service Class | Purpose | Key Methods |
|---------------|---------|-------------|
| `VictimAuthenticationService` | Victim authentication | `register()`, `login()`, `logout()`, `isLoggedIn()` |
| `AdminAuthenticationService` | Admin authentication | `authenticate()` |
| `AdministratorService` | Admin management | (Planned) CRUD operations |

#### Service Responsibilities

**VictimAuthenticationService**
- Validates user input (email format, password strength)
- Checks for duplicate email addresses
- Hashes passwords before storage
- Verifies credentials during login
- Manages current user session

**AdminAuthenticationService**
- Authenticates administrator credentials
- Verifies admin password hashes
- Returns administrator object on success

#### Communication Flow
```
Controller → Service Method → Validation → DAO → Database
```

---

### 3. DAO Layer (Data Access Object)

**Location**: `src/main/java/com/group2/dbapp/dao/`

#### Responsibilities
- Abstract database operations
- Execute SQL queries
- Map ResultSets to domain objects
- Handle database connections
- Prevent SQL injection (PreparedStatements)

#### Design Pattern: DAO Pattern

**Interface-Implementation Separation**
- **Interface**: Defines contract (`AdministratorDAO`, `VictimDAO`)
- **Implementation**: Concrete database operations (`AdministratorDAOImpl`, `VictimDAOImpl`)

#### Components

| DAO Interface | Implementation | Key Operations |
|---------------|----------------|----------------|
| `AdministratorDAO` | `AdministratorDAOImpl` | `findByEmail()`, `findById()`, `findAll()`, `create()`, `update()`, `delete()` |
| `VictimDAO` | `VictimDAOImpl` | `findByEmail()`, `create()`, `update()`, etc. |

#### DAO Operations

**CRUD Operations**
- **Create**: Insert new records
- **Read**: Query records (by ID, email, or all)
- **Update**: Modify existing records
- **Delete**: Remove records

**Security Features**
- All queries use `PreparedStatement` to prevent SQL injection
- Parameterized queries with `?` placeholders
- Proper exception handling

#### Communication Flow
```
Service → DAO Interface → DAO Implementation → DatabaseConnection → MySQL
```

---

### 4. Model Layer

**Location**: `src/main/java/com/group2/dbapp/model/`

#### Responsibilities
- Represent domain entities
- Encapsulate data
- Provide data transfer objects (DTOs)
- Map database tables to Java objects

#### Components

| Model Class | Database Table | Key Fields |
|-------------|----------------|------------|
| `Administrator` | `Administrators` | `adminID`, `name`, `role`, `contactEmail`, `passwordHash`, `dateAssigned` |
| `Victim` | `Victims` | `victimID`, `name`, `contactEmail`, `passwordHash`, `accountStatus`, `dateCreated` |

#### Model Structure

**Administrator Model**
```java
- adminID: int (Primary Key)
- name: String
- role: String (ENUM: 'System Admin', 'Cybersecurity Staff')
- contactEmail: String (Unique)
- passwordHash: String (SHA-256 hash)
- dateAssigned: LocalDateTime
```

**Victim Model**
```java
- victimID: int (Primary Key)
- name: String
- contactEmail: String (Unique)
- passwordHash: String (SHA-256 hash)
- accountStatus: String (ENUM: 'Active', 'Flagged', 'Suspended')
- dateCreated: LocalDateTime
```

---

### 5. Utility Layer

**Location**: `src/main/java/com/group2/dbapp/util/`

#### Responsibilities
- Provide reusable utility functions
- Manage database connections
- Security operations (hashing, encryption)
- Input validation
- Date/time formatting

#### Components

| Utility Class | Purpose | Key Methods |
|---------------|---------|-------------|
| `DatabaseConnection` | Connection management | `getConnection()`, `closeConnection()`, `testConnection()` |
| `SecurityUtils` | Security operations | `hashPassword()`, `verifyPassword()`, `encrypt()`, `decrypt()`, `anonymizeEmail()` |
| `ValidationUtils` | Input validation | `isValidEmail()`, `isValidPassword()`, `isNotEmpty()` |
| `DateUtils` | Date operations | (Planned) Date formatting utilities |

#### DatabaseConnection (Singleton Pattern)

**Purpose**: Centralized database connection management

**Features**:
- Singleton pattern ensures single connection instance
- Lazy initialization
- Connection pooling (single connection)
- Automatic reconnection on failure

**Connection Lifecycle**:
```
Application Start → Test Connection → Get Connection → Use → Close on Exit
```

#### SecurityUtils

**Password Hashing**:
- Algorithm: SHA-256
- Process: String → UTF-8 bytes → SHA-256 hash → Hexadecimal string
- Output: 64-character hexadecimal string

**Security Features**:
- Password hashing (one-way)
- Password verification
- Data anonymization (RA 10173 compliance)
- Base64 encoding (for sensitive data)

#### ValidationUtils

**Validation Rules**:
- Email: Regex pattern validation
- Password: Minimum 6 characters
- String: Non-empty checks
- Enum: Value validation

---

### 6. Database Layer

**Database**: MySQL 8.0+  
**Database Name**: `CybersecurityDB`

#### Schema Overview

**Core Tables**:
- `Victims`: User accounts for incident reporters
- `Administrators`: System administrators and staff
- `Perpetrators`: (Planned) Threat actors and identifiers
- `IncidentReports`: (Planned) Reported cybersecurity incidents
- `AttackTypes`: (Planned) Types of cyber attacks

#### Key Tables

**Victims Table**
```sql
CREATE TABLE Victims (
    VictimID INT AUTO_INCREMENT PRIMARY KEY,
    Name VARCHAR(100) NOT NULL,
    ContactEmail VARCHAR(100) UNIQUE NOT NULL,
    PasswordHash VARCHAR(255) NOT NULL,
    AccountStatus ENUM('Active', 'Flagged', 'Suspended') DEFAULT 'Active',
    DateCreated DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

**Administrators Table**
```sql
CREATE TABLE Administrators (
    AdminID INT AUTO_INCREMENT PRIMARY KEY,
    Name VARCHAR(100) NOT NULL,
    Role ENUM('System Admin', 'Cybersecurity Staff') NOT NULL,
    ContactEmail VARCHAR(100) UNIQUE NOT NULL,
    PasswordHash VARCHAR(255) NOT NULL,
    DateAssigned DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

#### Database Connection

**Configuration**:
- URL: `jdbc:mysql://localhost:3306/CybersecurityDB`
- Driver: `com.mysql.cj.jdbc.Driver`
- Connection: Singleton pattern

---

## Component Details

### Application Entry Point

**Class**: `Main.java`  
**Package**: `com.group2.dbapp`

#### Responsibilities
- Initialize JavaFX application
- Test database connection
- Load initial FXML view
- Handle application lifecycle
- Clean up resources on exit

#### Application Lifecycle

```
1. Application Launch
   ↓
2. Database Connection Test
   ↓
3. Load Login FXML
   ↓
4. Display Login Window
   ↓
5. User Interaction
   ↓
6. Application Close
   ↓
7. Database Connection Cleanup
```

---

## Data Flow

### Authentication Flow (Victim Login)

```
┌─────────┐
│  User   │
└────┬────┘
     │ 1. Enter email & password
     ↓
┌─────────────────────┐
│ LoginController     │
│ handleLogin()       │
└────┬────────────────┘
     │ 2. Validate input (basic)
     │ 3. Call authService.login()
     ↓
┌──────────────────────────┐
│ VictimAuthentication     │
│ Service                  │
│ login(email, password)   │
└────┬─────────────────────┘
     │ 4. Validate email format
     │ 5. Validate password not empty
     │ 6. Call victimDAO.findByEmail()
     ↓
┌─────────────────────┐
│ VictimDAO           │
│ findByEmail()       │
└────┬────────────────┘
     │ 7. Execute SQL query
     │ 8. Map ResultSet to Victim
     ↓
┌─────────────────────┐
│ Database            │
│ SELECT * FROM       │
│ Victims WHERE...    │
└────┬────────────────┘
     │ 9. Return Victim object
     ↓
┌──────────────────────────┐
│ VictimAuthentication     │
│ Service (continued)       │
└────┬─────────────────────┘
     │ 10. Verify password hash
     │ 11. Set currentVictim
     │ 12. Return Victim object
     ↓
┌─────────────────────┐
│ LoginController     │
│ (continued)         │
└────┬────────────────┘
     │ 13. Show success message
     │ 14. Navigate to dashboard
     ↓
┌─────────┐
│  User   │
│ Logged  │
│  In     │
└─────────┘
```

### Registration Flow (Sign Up)

```
┌─────────┐
│  User   │
└────┬────┘
     │ 1. Enter name, email, password
     ↓
┌─────────────────────┐
│ SignUpController    │
│ handleSignUp()     │
└────┬────────────────┘
     │ 2. Validate all fields filled
     │ 3. Validate passwords match
     │ 4. Call authService.register()
     ↓
┌──────────────────────────┐
│ VictimAuthentication     │
│ Service                  │
│ register(name, email,    │
│          password)       │
└────┬─────────────────────┘
     │ 5. Validate name not empty
     │ 6. Validate email format
     │ 7. Validate password strength
     │ 8. Check email not exists
     │ 9. Hash password
     │ 10. Create Victim object
     │ 11. Call victimDAO.create()
     ↓
┌─────────────────────┐
│ VictimDAO           │
│ create()            │
└────┬────────────────┘
     │ 12. Execute INSERT query
     ↓
┌─────────────────────┐
│ Database            │
│ INSERT INTO Victims │
└────┬────────────────┘
     │ 13. Return success
     ↓
┌──────────────────────────┐
│ SignUpController        │
│ (continued)             │
└────┬─────────────────────┘
     │ 14. Show success message
     │ 15. Navigate to login
     ↓
┌─────────┐
│  User   │
│ Account │
│ Created │
└─────────┘
```

---

## Design Patterns

### 1. Singleton Pattern

**Used In**: `DatabaseConnection`

**Purpose**: Ensure only one database connection instance exists

**Implementation**:
```java
private static Connection connection = null;

public static Connection getConnection() {
    if (connection == null || connection.isClosed()) {
        connection = DriverManager.getConnection(...);
    }
    return connection;
}
```

**Benefits**:
- Resource efficiency
- Centralized connection management
- Prevents connection leaks

---

### 2. DAO Pattern (Data Access Object)

**Used In**: All DAO classes

**Purpose**: Separate data access logic from business logic

**Structure**:
```
Interface (Contract)
    ↓
Implementation (SQL Queries)
    ↓
Database
```

**Benefits**:
- Database abstraction
- Easy to test (mock interfaces)
- Easy to swap database implementations
- Separation of concerns

**Example**:
```java
// Interface
public interface AdministratorDAO {
    Administrator findByEmail(String email) throws SQLException;
}

// Implementation
public class AdministratorDAOImpl implements AdministratorDAO {
    public Administrator findByEmail(String email) {
        // SQL implementation
    }
}
```

---

### 3. Service Layer Pattern

**Used In**: All Service classes

**Purpose**: Encapsulate business logic

**Benefits**:
- Reusable business logic
- Centralized business rules
- Easy to test
- Transaction management (future)

**Example**:
```java
public class VictimAuthenticationService {
    private final VictimDAO victimDAO;
    
    public Victim login(String email, String password) {
        // Business logic: validation, hashing, verification
    }
}
```

---

### 4. MVC Pattern (Model-View-Controller)

**Used In**: JavaFX application structure

**Components**:
- **Model**: Domain objects (`Administrator`, `Victim`)
- **View**: FXML files (`LogIn.fxml`, `SignUp.fxml`)
- **Controller**: JavaFX controllers (`LoginController`, etc.)

**Flow**:
```
View (FXML) → Controller → Model → Service → DAO → Database
```

**Benefits**:
- Separation of UI and logic
- Easy to modify views without changing logic
- Testable controllers

---

### 5. Layered Architecture Pattern

**Used In**: Overall application structure

**Layers**:
1. Presentation Layer
2. Service Layer
3. DAO Layer
4. Utility Layer
5. Database Layer

**Rules**:
- Each layer can only communicate with adjacent layers
- No skipping layers
- Clear separation of concerns

**Benefits**:
- Maintainability
- Testability
- Scalability
- Clear responsibilities

---

## Security Architecture

### Password Security

#### Hashing Process

**Algorithm**: SHA-256  
**Process**:
```
Plain Password → UTF-8 Bytes → SHA-256 Hash → Hexadecimal String
```

**Example**:
```
Input:  "password123"
Output: "ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f"
```

#### Storage

- **Never store plain text passwords**
- Only hash values stored in database
- Hash stored in `PasswordHash VARCHAR(255)` column

#### Verification

```
User Input → Hash Input → Compare with Stored Hash → Match/No Match
```

### SQL Injection Prevention

**Method**: PreparedStatements

**Example**:
```java
String sql = "SELECT * FROM Administrators WHERE ContactEmail = ?";
PreparedStatement stmt = conn.prepareStatement(sql);
stmt.setString(1, email);  // Safe parameter binding
```

**Benefits**:
- Automatic escaping
- Type safety
- Prevents SQL injection attacks

### Input Validation

**Multi-Layer Validation**:

1. **UI Layer**: Basic format checks (not empty, format)
2. **Service Layer**: Business rule validation (email format, password strength)
3. **Database Layer**: Constraints (UNIQUE, NOT NULL, ENUM)

**Validation Rules**:
- Email: Must match regex pattern
- Password: Minimum 6 characters
- Name: Not empty
- All fields: Required validation

### Data Privacy Compliance

**RA 10173 (Philippine Data Privacy Act) Compliance**:

- **Anonymization**: `SecurityUtils.anonymizeEmail()`, `anonymizeName()`
- **Secure Storage**: Password hashing
- **Access Control**: Role-based access (future)

---

## Database Architecture

### Connection Management

**Pattern**: Singleton  
**Class**: `DatabaseConnection`

**Features**:
- Single connection instance
- Lazy initialization
- Automatic reconnection
- Connection cleanup on application exit

### Query Execution

**Method**: PreparedStatements

**Benefits**:
- SQL injection prevention
- Performance (pre-compiled)
- Type safety

### Transaction Management

**Current**: Not implemented  
**Future**: Service layer transaction management

### Database Schema

**Database**: `CybersecurityDB`

**Tables**:
- `Victims`: User accounts
- `Administrators`: Admin accounts
- `Perpetrators`: (Planned) Threat actors
- `IncidentReports`: (Planned) Incident records
- `AttackTypes`: (Planned) Attack classifications

**Relationships**:
- Foreign keys for referential integrity
- Cascade deletes where appropriate

---

## UI Architecture

### JavaFX Framework

**Technology**: JavaFX 21+  
**UI Definition**: FXML files  
**Styling**: Inline styles (CSS can be added)

### View Organization

**Directory Structure**:
```
SceneBuilder/
├── login uis/
│   ├── LogIn.fxml
│   ├── SignUp.fxml
│   └── AdminLogin.fxml
├── dashboards/
│   └── (Planned dashboards)
└── assets/
    └── ccinfom phishnet logo.png
```

### Controller-View Binding

**FXML Binding**:
```xml
<Pane fx:controller="com.group2.dbapp.controller.LoginController">
    <TextField fx:id="emailField" />
    <Button fx:id="loginButton" onAction="#handleLogin" />
</Pane>
```

**Controller Injection**:
```java
@FXML
private TextField emailField;

@FXML
private void handleLogin() {
    // Handle login
}
```

### Navigation Flow

```
Application Start
    ↓
Login Screen (LogIn.fxml)
    ├─→ Sign Up Screen (SignUp.fxml)
    ├─→ Admin Login (AdminLogin.fxml)
    └─→ (Future) Victim Dashboard
```

---

## Dependencies & Technologies

### Core Dependencies

| Dependency | Version | Purpose |
|------------|---------|---------|
| Java | 17+ | Programming language |
| JavaFX | 21+ | UI framework |
| MySQL Connector | 8.0+ | Database driver |
| Maven | 3.6+ | Build tool |

### Maven Dependencies

**JavaFX**:
- `javafx-controls`: UI components
- `javafx-fxml`: FXML support

**Database**:
- `mysql-connector-java`: MySQL JDBC driver

### Build Configuration

**Maven Structure**:
```
pom.xml
src/
├── main/
│   ├── java/
│   │   └── com/group2/dbapp/
│   └── resources/
│       └── SceneBuilder/
└── test/
```

---

## Deployment Architecture

### Application Type

**Type**: Desktop Application  
**Platform**: Cross-platform (Windows, macOS, Linux)  
**Distribution**: JAR file or native executable

### Runtime Requirements

- Java Runtime Environment (JRE) 17+
- MySQL Server 8.0+ (running)
- Network access to database (localhost or remote)

### Deployment Steps

1. **Database Setup**:
   - Run `DB_SCHEMA_V2.sql` to create database
   - Configure connection in `DatabaseConnection.java`

2. **Build Application**:
   ```bash
   mvn clean package
   ```

3. **Run Application**:
   ```bash
   java -jar target/phishnet.jar
   ```
   OR
   ```bash
   mvn javafx:run
   ```

### Configuration

**Database Configuration**:
- Location: `DatabaseConnection.java`
- Configurable: URL, username, password

**Future**: External configuration file (`application.properties`)

---

## Future Enhancements

### Planned Features

1. **Incident Reporting**:
   - Victim dashboard for reporting incidents
   - File uploads for evidence
   - Incident tracking

2. **Administrative Features**:
   - Admin dashboard
   - Incident management
   - Perpetrator tracking
   - Analytics and reporting

3. **Security Improvements**:
   - Password salt implementation
   - BCrypt or Argon2 for password hashing
   - Session management
   - Role-based access control (RBAC)

4. **Architecture Improvements**:
   - Transaction management
   - Connection pooling
   - Logging framework (Log4j/SLF4J)
   - Unit testing framework
   - Configuration externalization

---

## Conclusion

This architectural design document provides a comprehensive overview of the PhishNet application's structure, patterns, and implementation details. The application follows industry best practices with:

- **Clear separation of concerns** through layered architecture
- **Security-first approach** with password hashing and SQL injection prevention
- **Maintainable code structure** with design patterns
- **Scalable foundation** for future enhancements

The architecture supports the application's current functionality while providing a solid foundation for future feature additions.

---

**Document Version**: 1.0  
**Last Updated**: 2025  
**Maintained By**: Development Team

