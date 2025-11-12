# PhishNet - Cybersecurity Incident Reporting System (JavaFX Version)

## Overview
This is a refactored version of the console-based Cybersecurity Incident Reporting System, now featuring a modern JavaFX graphical user interface with proper architectural layering.

## Architecture

### Layered Architecture
The application follows a clean **3-tier architecture**:

```
┌─────────────────────────────────────┐
│     Presentation Layer (UI)         │
│  - JavaFX Controllers               │
│  - FXML Views                       │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│      Business Logic Layer           │
│  - Services (AuthenticationService, │
│    AdministratorService)            │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│      Data Access Layer              │
│  - DAO Interfaces & Implementations │
│  - Database Connection              │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│         MySQL Database              │
└─────────────────────────────────────┘
```

### Project Structure

```
VERSION_2-JAVAFX_INTEGRATION/
├── pom.xml                          # Maven configuration with JavaFX dependencies
├── DB_SCHEMA_V2.sql                 # Updated database schema with password support
├── README.md                        # This file
└── src/
    ├── main/
    │   ├── java/com/group2/dbapp/   # Java source files
    │   └── resources/
    │       ├── SceneBuilder/        # FXML files and assets
    │       │   ├── LogIn.fxml       # Login page design
    │       │   └── assets/
    │       │       └── ccinfom phishnet logo.png
    │       └── application.properties.template
    ├── Main.java                    # JavaFX Application entry point
    ├── controller/                  # JavaFX Controllers (Presentation Layer)
    │   └── LoginController.java     # Login page controller
    ├── service/                     # Business Logic Layer
    │   ├── AuthenticationService.java
    │   └── AdministratorService.java
    ├── dao/                         # Data Access Layer
    │   ├── AdministratorDAO.java    # DAO interface
    │   └── AdministratorDAOImpl.java # DAO implementation
    ├── model/                       # Domain Models
    │   └── Administrator.java       # Administrator entity
    └── util/                        # Utility classes
        ├── DatabaseConnection.java  # Database connection manager
        ├── SecurityUtils.java       # Password hashing & encryption
        ├── ValidationUtils.java     # Input validation
        └── DateUtils.java           # Date/time utilities
```

## Features Implemented

### Phase 1: Login System ✓
- [x] JavaFX login interface
- [x] Email and password authentication
- [x] SHA-256 password hashing
- [x] Database integration
- [x] Input validation
- [x] Error handling

### Architecture Components ✓
- [x] DAO Layer (Data Access Objects)
- [x] Service Layer (Business Logic)
- [x] Controller Layer (UI Controllers)
- [x] Model Layer (Domain Entities)
- [x] Utility Layer (Helpers)

## Prerequisites

1. **Java Development Kit (JDK) 17 or higher**
2. **Apache Maven 3.6+**
3. **MySQL Server 8.0+**
4. **JavaFX SDK 21+** (will be downloaded automatically by Maven)

## Setup Instructions

### 1. Database Setup

```sql
-- Run the database schema
mysql -u root -p < DB_SCHEMA_V2.sql
```

The schema includes:
- Updated `Administrators` table with `PasswordHash` field
- Sample administrator account:
  - Email: `admin@phishnet.com`
  - Password: `admin123`

### 2. Configure Database Connection

Edit `src/main/java/com/group2/dbapp/util/DatabaseConnection.java`:

```java
private static final String DB_URL = "jdbc:mysql://localhost:3306/CybersecurityDB";
private static final String DB_USER = "root";
private static final String DB_PASSWORD = "your_password_here";
```

### 3. Build the Project

```bash
cd VERSION_2-JAVAFX_INTEGRATION
mvn clean install
```

### 4. Run the Application

```bash
mvn javafx:run
```

## Testing the Login System

### Test Credentials
- **Email**: `admin@phishnet.com`
- **Password**: `admin123`

### Creating New Administrators

You can use the `AuthenticationService.register()` method programmatically or run this SQL:

```sql
-- Password: newpassword123
-- SHA-256 hash: ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f
INSERT INTO Administrators (Name, Role, ContactEmail, PasswordHash) 
VALUES ('Jane Doe', 'Cybersecurity Staff', 'jane@phishnet.com', 
'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f');
```

## Key Technologies

- **JavaFX 21**: Modern UI framework
- **MySQL 8.0**: Relational database
- **Maven**: Build and dependency management
- **JDBC**: Database connectivity
- **SHA-256**: Password hashing

## Security Features

1. **Password Hashing**: All passwords are hashed using SHA-256
2. **Input Validation**: Email and password validation before processing
3. **SQL Injection Prevention**: PreparedStatements for all database queries
4. **Data Privacy Compliance**: RA 10173 (Philippine Data Privacy Act)

## Design Patterns Used

1. **Singleton Pattern**: DatabaseConnection class
2. **DAO Pattern**: Data access abstraction
3. **Service Layer Pattern**: Business logic separation
4. **MVC Pattern**: Model-View-Controller for JavaFX

## Next Steps (Future Phases)

- [ ] Main dashboard after login
- [ ] Incident reporting interface
- [ ] Victim management
- [ ] Perpetrator tracking
- [ ] Evidence upload
- [ ] Reports generation
- [ ] User registration interface
- [ ] Password recovery functionality

## Troubleshooting

### Common Issues

1. **Database Connection Failed**
   - Ensure MySQL is running
   - Check database credentials in `DatabaseConnection.java`
   - Verify database exists: `SHOW DATABASES LIKE 'CybersecurityDB';`

2. **FXML Load Error**
   - Ensure FXML path is correct: `/SceneBuilder/LogIn.fxml`
   - Check that FXML file is in the resources directory

3. **JavaFX Runtime Error**
   - Ensure you're running with Maven: `mvn javafx:run`
   - JavaFX must be on the module path

4. **Location is not set / FXML not found**
   - Ensure FXML files are in `src/main/resources/` directory
   - Check the path in FXMLLoader matches the resource location
   - FXML files must be copied to `target/classes/` during build

## Contributing

This project follows a layered architecture. When adding new features:

1. Create model classes in `model/` package
2. Create DAO interfaces and implementations in `dao/` package
3. Create service classes in `service/` package
4. Create controllers in `controller/` package
5. Create FXML views in `SceneBuilder/` directory

## License

Educational project for CCINFOM Database Application Development.

---

**Group 2 - Database Application Project**

