# Architecture Documentation

## System Architecture Overview

The application follows a **layered architecture** pattern with clear separation of concerns:

```
┌──────────────────────────────────────────────────────────────┐
│                   PRESENTATION LAYER                         │
│  ┌────────────────┐  ┌──────────────────────────────────┐   │
│  │  JavaFX FXML   │  │   JavaFX Controllers             │   │
│  │  - LogIn.fxml  │  │   - LoginController.java         │   │
│  └────────────────┘  └──────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────┘
                            ↓ ↑
┌──────────────────────────────────────────────────────────────┐
│                   SERVICE LAYER                              │
│  ┌───────────────────────────┐  ┌────────────────────────┐  │
│  │ AuthenticationService     │  │ AdministratorService   │  │
│  │ - login()                 │  │ - getAdministrator()   │  │
│  │ - register()              │  │ - updateAdministrator()│  │
│  │ - changePassword()        │  │ - deleteAdministrator()│  │
│  └───────────────────────────┘  └────────────────────────┘  │
└──────────────────────────────────────────────────────────────┘
                            ↓ ↑
┌──────────────────────────────────────────────────────────────┐
│                   DAO LAYER                                  │
│  ┌────────────────────┐  ┌──────────────────────────────┐   │
│  │ AdministratorDAO   │  │ AdministratorDAOImpl         │   │
│  │ (Interface)        │  │ - findByEmail()              │   │
│  │                    │  │ - findById()                 │   │
│  │                    │  │ - create()                   │   │
│  │                    │  │ - update()                   │   │
│  │                    │  │ - delete()                   │   │
│  └────────────────────┘  └──────────────────────────────┘   │
└──────────────────────────────────────────────────────────────┘
                            ↓ ↑
┌──────────────────────────────────────────────────────────────┐
│                   UTILITY LAYER                              │
│  ┌──────────────────┐  ┌──────────────┐  ┌──────────────┐   │
│  │DatabaseConnection│  │SecurityUtils │  │ValidationUtils│  │
│  │DateUtils         │  │              │  │               │  │
│  └──────────────────┘  └──────────────┘  └──────────────┘   │
└──────────────────────────────────────────────────────────────┘
                            ↓ ↑
┌──────────────────────────────────────────────────────────────┐
│                   DATABASE LAYER                             │
│              MySQL CybersecurityDB                           │
└──────────────────────────────────────────────────────────────┘
```

## Layer Responsibilities

### 1. Presentation Layer (UI)
**Location**: `controller/` package and `SceneBuilder/` directory

**Responsibilities**:
- Handle user interactions (button clicks, form submissions)
- Display data to the user
- Input validation (basic client-side)
- Navigate between views
- Show alerts and error messages

**Key Classes**:
- `LoginController.java`: Handles login form interactions

**Technologies**: JavaFX, FXML

### 2. Service Layer (Business Logic)
**Location**: `service/` package

**Responsibilities**:
- Implement business rules and logic
- Coordinate between controllers and DAOs
- Transaction management
- Data validation (business rules)
- Error handling and logging

**Key Classes**:
- `AuthenticationService.java`: Handles authentication logic
- `AdministratorService.java`: Manages administrator operations

**Benefits**:
- Reusable business logic
- Easy to test
- Centralized business rules

### 3. DAO Layer (Data Access)
**Location**: `dao/` package

**Responsibilities**:
- CRUD operations on database
- Database query execution
- Result set mapping to objects
- SQL statement management
- Connection handling

**Key Classes**:
- `AdministratorDAO.java`: Interface defining data operations
- `AdministratorDAOImpl.java`: Implementation with actual SQL queries

**Design Pattern**: DAO (Data Access Object) Pattern

### 4. Model Layer (Domain Objects)
**Location**: `model/` package

**Responsibilities**:
- Represent database entities
- Encapsulate data
- Provide getters and setters
- Data transfer between layers

**Key Classes**:
- `Administrator.java`: Represents administrator entity

### 5. Utility Layer
**Location**: `util/` package

**Responsibilities**:
- Common helper functions
- Database connection management
- Security utilities (hashing, encryption)
- Validation utilities
- Date/time utilities

**Key Classes**:
- `DatabaseConnection.java`: Manages database connections
- `SecurityUtils.java`: Password hashing and encryption
- `ValidationUtils.java`: Input validation
- `DateUtils.java`: Date/time formatting

## Authentication Flow

```
┌─────────┐
│  User   │
└────┬────┘
     │ 1. Enter credentials
     ↓
┌─────────────────────┐
│ LoginController     │
│ @FXML handleLogin() │
└────┬────────────────┘
     │ 2. Call login()
     ↓
┌──────────────────────────┐
│ AuthenticationService    │
│ login(email, password)   │
└────┬─────────────────────┘
     │ 3. Validate input
     ↓
┌──────────────────────────┐
│ ValidationUtils          │
│ isValidEmail()           │
│ isNotEmpty()             │
└────┬─────────────────────┘
     │ 4. Find by email
     ↓
┌──────────────────────────┐
│ AdministratorDAO         │
│ findByEmail(email)       │
└────┬─────────────────────┘
     │ 5. Execute SQL query
     ↓
┌──────────────────────────┐
│ DatabaseConnection       │
│ getConnection()          │
└────┬─────────────────────┘
     │ 6. Return admin data
     ↓
┌──────────────────────────┐
│ SecurityUtils            │
│ verifyPassword()         │
└────┬─────────────────────┘
     │ 7. Return result
     ↓
┌──────────────────────────┐
│ LoginController          │
│ Show success/error       │
└──────────────────────────┘
```

## Design Patterns Applied

### 1. Singleton Pattern
**Used in**: `DatabaseConnection`

**Purpose**: Ensure only one database connection exists

```java
private static Connection connection = null;

public static Connection getConnection() throws SQLException {
    if (connection == null || connection.isClosed()) {
        // Create new connection
    }
    return connection;
}
```

### 2. DAO Pattern
**Used in**: All DAO classes

**Purpose**: Separate data access logic from business logic

```java
public interface AdministratorDAO {
    Administrator findByEmail(String email);
    boolean create(Administrator admin);
    // ... other CRUD operations
}
```

### 3. Service Layer Pattern
**Used in**: All Service classes

**Purpose**: Encapsulate business logic

```java
public class AuthenticationService {
    private final AdministratorDAO dao;
    
    public Administrator login(String email, String password) {
        // Business logic here
    }
}
```

### 4. MVC Pattern
**Used in**: JavaFX application structure

- **Model**: `Administrator.java`
- **View**: `LogIn.fxml`
- **Controller**: `LoginController.java`

## Security Architecture

### Password Security
1. **Never store plain text passwords**
2. **SHA-256 hashing** for all passwords
3. **Salt could be added** for enhanced security (future improvement)

```java
// Password hashing
String hash = SecurityUtils.hashPassword(plainPassword);

// Password verification
boolean valid = SecurityUtils.verifyPassword(inputPassword, storedHash);
```

### SQL Injection Prevention
All database queries use **PreparedStatements**:

```java
String sql = "SELECT * FROM Administrators WHERE ContactEmail = ?";
PreparedStatement stmt = conn.prepareStatement(sql);
stmt.setString(1, email);  // Safe from SQL injection
```

### Input Validation
Multiple layers of validation:
1. **UI Layer**: Basic format checks
2. **Service Layer**: Business rule validation
3. **Database Layer**: Constraints and foreign keys

## Database Schema

### Administrators Table (Enhanced)
```sql
CREATE TABLE Administrators (
    AdminID INT AUTO_INCREMENT PRIMARY KEY,
    Name VARCHAR(100) NOT NULL,
    Role ENUM('System Admin', 'Cybersecurity Staff') NOT NULL,
    ContactEmail VARCHAR(100) UNIQUE NOT NULL,
    PasswordHash VARCHAR(255) NOT NULL,  -- NEW: Password field
    DateAssigned DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

## Benefits of This Architecture

### 1. Separation of Concerns
- Each layer has a specific responsibility
- Changes in one layer don't affect others
- Easy to understand and maintain

### 2. Testability
- Each layer can be tested independently
- Mock objects can be used for unit testing
- Business logic is isolated from UI and database

### 3. Maintainability
- Clear structure makes code easy to navigate
- New features can be added without affecting existing code
- Bug fixes are localized to specific layers

### 4. Scalability
- Easy to add new features (new DAOs, Services, Controllers)
- Can switch database without changing business logic
- Can change UI without affecting backend

### 5. Reusability
- Service layer can be used by multiple UI components
- DAO layer can be reused across different services
- Utility classes are shared across the application

## Extension Points

### Adding New Features

#### 1. Add New Entity (e.g., Incident)
```
Step 1: Create model/Incident.java
Step 2: Create dao/IncidentDAO.java (interface)
Step 3: Create dao/IncidentDAOImpl.java (implementation)
Step 4: Create service/IncidentService.java
Step 5: Create controller/IncidentController.java
Step 6: Create SceneBuilder/Incident.fxml
```

#### 2. Add New Service Method
```java
// In VictimAuthenticationService.java
public boolean resetPassword(String email) {
    // Implementation
}
```

#### 3. Add New Utility
```java
// In util/EmailUtils.java
public class EmailUtils {
    public static boolean sendEmail(String to, String subject, String body) {
        // Implementation
    }
}
```

## Best Practices Followed

1. **Interface-based design**: DAOs use interfaces
2. **Dependency injection**: Services receive DAOs
3. **Error handling**: Try-catch blocks at appropriate layers
4. **Resource management**: Try-with-resources for connections
5. **Logging**: Console logging for debugging
6. **Validation**: Multiple layers of input validation
7. **Security**: Password hashing, SQL injection prevention
8. **Documentation**: Comprehensive JavaDoc comments

## Future Enhancements

1. **Dependency Injection Framework**: Use Spring or CDI
2. **Connection Pooling**: Use HikariCP or C3P0
3. **ORM**: Consider using JPA/Hibernate
4. **Logging Framework**: Replace System.out with Log4j or SLF4J
5. **Configuration Management**: Externalize configuration
6. **Testing**: Add JUnit tests for all layers
7. **Password Salt**: Add salt to password hashing
8. **Session Management**: Implement proper session handling

---

This architecture provides a solid foundation for building a scalable, maintainable, and secure application.

