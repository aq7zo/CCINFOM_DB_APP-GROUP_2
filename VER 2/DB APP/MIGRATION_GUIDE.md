# JavaFX Migration Guide

## Overview

The application has been successfully migrated from a console-based MVC architecture to a modern JavaFX application with the following improvements:

1. **JavaFX GUI** - Modern, responsive user interface using FXML and Scene Builder
2. **Layered Architecture** - Controller → Service → DAO pattern for better separation of concerns
3. **Role-Based Authentication** - User (Victim) and Admin login with session management
4. **CSS Styling** - Consistent theming across all views

## New Project Structure

```
src/main/java/app/
├── MainApp.java                    # JavaFX entry point
├── config/
│   ├── DatabaseConnection.java    # Database connection management
│   └── SessionManager.java        # Role-based session management
├── controllers/                    # FXML Controllers (UI logic only)
│   ├── LoginController.java
│   ├── UserDashboardController.java
│   └── AdminDashboardController.java
├── services/                       # Business logic layer
│   ├── AuthService.java
│   ├── VictimService.java
│   ├── IncidentService.java
│   ├── PerpetratorService.java
│   ├── AttackTypeService.java
│   ├── EvidenceService.java
│   └── AdminService.java
├── dao/                            # Data Access Object layer
│   ├── VictimDAO.java
│   ├── AdministratorDAO.java
│   ├── IncidentDAO.java
│   ├── PerpetratorDAO.java
│   ├── AttackTypeDAO.java
│   └── EvidenceDAO.java
├── models/                         # Data entities
│   ├── Victim.java
│   ├── Administrator.java
│   ├── Perpetrator.java
│   ├── IncidentReport.java
│   ├── Evidence.java
│   └── AttackType.java
└── utils/                          # Utility classes
    ├── DateUtils.java
    ├── ValidationUtils.java
    └── SecurityUtils.java

src/main/resources/
├── fxml/                           # FXML view files
│   ├── LoginView.fxml
│   ├── UserDashboard.fxml
│   └── AdminDashboard.fxml
└── css/
    └── theme.css                   # Global stylesheet
```

## Running the Application

### Prerequisites
- Java JDK 17+
- Maven 3.x
- MySQL Server running
- Database `CybersecurityDB` created (run `DB_SCHEMA.sql`)

### Build and Run

```bash
# Compile the project
mvn clean compile

# Run the application
mvn exec:java -Dexec.mainClass="app.MainApp"
```

Or use your IDE to run `app.MainApp` directly.

## Key Features

### 1. Role-Based Login
- **User (Victim)**: Login with email from Victims table
- **Admin**: Login with email from Administrators table
- Session persists across views

### 2. User Dashboard
- Submit Incident Report
- Upload Evidence
- View My Reports (only own incidents)

### 3. Admin Dashboard
- Manage Victims
- Manage Perpetrators
- Manage Attack Types
- View All Incidents
- Create Incident Report (on behalf of any victim)
- Validate Reports
- Generate Reports

## Architecture Layers

### UI Layer (FXML Controllers)
- Handle user interactions
- Display data from Services
- Navigate between views
- **No direct database access**

### Service Layer
- Business logic and validation
- Coordinate multiple DAOs
- Handle transactions
- Auto-flagging and auto-escalation logic

### DAO Layer
- Pure database operations (CRUD)
- SQL queries and ResultSet mapping
- **No business logic**

## Migration Notes

### Old Structure (Console)
- `controller/` - Mixed business logic and database access
- `view/` - Console menus
- `model/` - Data entities
- `Main.java` - Console entry point

### New Structure (JavaFX)
- `controllers/` - FXML controllers (UI only)
- `services/` - Business logic
- `dao/` - Database access
- `models/` - Data entities (moved to `app.models`)
- `MainApp.java` - JavaFX entry point

## Adding New Views

1. Create FXML file in `src/main/resources/fxml/`
2. Create Controller in `app.controllers` package
3. Link FXML to Controller using `fx:controller` attribute
4. Use Services (not DAOs) in Controllers
5. Apply CSS classes from `theme.css`

## Database Connection

Update credentials in `app.config.DatabaseConnection.java`:
```java
private static final String DB_URL = "jdbc:mysql://localhost:3306/CybersecurityDB";
private static final String DB_USER = "root";
private static final String DB_PASSWORD = "your_password";
```

## Session Management

Access current user session:
```java
SessionManager session = SessionManager.getInstance();
if (session.isAdmin()) {
    // Admin-specific logic
} else if (session.isUser()) {
    // User-specific logic
}
```

## Future Enhancements

- Add password authentication (currently email-only)
- Implement remaining FXML views (SubmitIncident, UploadEvidence, etc.)
- Add data validation in Controllers
- Implement report generation views
- Add file upload functionality for evidence
- Connection pooling for better performance

## Troubleshooting

### JavaFX Not Found
If you get "JavaFX runtime components are missing" error:
- Ensure JavaFX dependencies are in `pom.xml`
- Run `mvn clean install` to download dependencies

### FXML Not Found
If FXML files are not loading:
- Ensure files are in `src/main/resources/fxml/`
- Check file paths in `FXMLLoader.load()` calls
- Verify `fx:controller` attribute matches package name

### Database Connection Issues
- Verify MySQL server is running
- Check database credentials
- Ensure `CybersecurityDB` database exists
- Run `DB_SCHEMA.sql` to create tables

## Support

For issues or questions, refer to:
- `ARCHITECTURE.md` - System architecture documentation
- `PHASE 1 - JavaFX Migration Blueprint.md` - Migration blueprint
- `PHASE 2 - Role-based Dashboard.md` - Role-based features

