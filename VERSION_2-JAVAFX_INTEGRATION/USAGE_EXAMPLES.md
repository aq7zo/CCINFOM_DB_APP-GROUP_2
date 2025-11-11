# Usage Examples and Code Patterns

## Table of Contents
1. [Authentication Examples](#authentication-examples)
2. [DAO Layer Examples](#dao-layer-examples)
3. [Service Layer Examples](#service-layer-examples)
4. [Controller Examples](#controller-examples)
5. [Utility Examples](#utility-examples)

---

## Authentication Examples

### Example 1: Basic Login

```java
// In your controller or main application
AuthenticationService authService = new AuthenticationService();

// Attempt login
Administrator admin = authService.login("admin@phishnet.com", "admin123");

if (admin != null) {
    System.out.println("Login successful!");
    System.out.println("Welcome, " + admin.getName());
    System.out.println("Role: " + admin.getRole());
} else {
    System.out.println("Login failed - invalid credentials");
}
```

### Example 2: Register New Administrator

```java
AuthenticationService authService = new AuthenticationService();

boolean success = authService.register(
    "Jane Doe",                    // name
    "Cybersecurity Staff",         // role
    "jane@phishnet.com",           // email
    "securePassword123"            // password
);

if (success) {
    System.out.println("Administrator registered successfully!");
} else {
    System.out.println("Registration failed");
}
```

### Example 3: Change Password

```java
AuthenticationService authService = new AuthenticationService();

// First login
Administrator admin = authService.login("jane@phishnet.com", "securePassword123");

if (admin != null) {
    // Change password
    boolean changed = authService.changePassword("securePassword123", "newPassword456");
    
    if (changed) {
        System.out.println("Password changed successfully!");
    }
}
```

### Example 4: Check Login Status

```java
AuthenticationService authService = new AuthenticationService();

// Login
authService.login("admin@phishnet.com", "admin123");

// Check if logged in
if (authService.isLoggedIn()) {
    Administrator currentUser = authService.getCurrentUser();
    System.out.println("Current user: " + currentUser.getName());
}

// Logout
authService.logout();

// Check again
if (!authService.isLoggedIn()) {
    System.out.println("User logged out successfully");
}
```

---

## DAO Layer Examples

### Example 5: Find Administrator by Email

```java
AdministratorDAO adminDAO = new AdministratorDAOImpl();

try {
    Administrator admin = adminDAO.findByEmail("admin@phishnet.com");
    
    if (admin != null) {
        System.out.println("Found: " + admin.getName());
        System.out.println("Role: " + admin.getRole());
        System.out.println("ID: " + admin.getAdminID());
    } else {
        System.out.println("Administrator not found");
    }
} catch (SQLException e) {
    System.err.println("Database error: " + e.getMessage());
}
```

### Example 6: Get All Administrators

```java
AdministratorDAO adminDAO = new AdministratorDAOImpl();

try {
    List<Administrator> admins = adminDAO.findAll();
    
    System.out.println("Total administrators: " + admins.size());
    
    for (Administrator admin : admins) {
        System.out.println("- " + admin.getName() + " (" + admin.getRole() + ")");
    }
} catch (SQLException e) {
    System.err.println("Database error: " + e.getMessage());
}
```

### Example 7: Create New Administrator

```java
AdministratorDAO adminDAO = new AdministratorDAOImpl();

// Create administrator object
Administrator newAdmin = new Administrator(
    "John Smith",
    "System Admin",
    "john@phishnet.com",
    SecurityUtils.hashPassword("password123")  // Hash the password!
);

try {
    boolean created = adminDAO.create(newAdmin);
    
    if (created) {
        System.out.println("Administrator created with ID: " + newAdmin.getAdminID());
    }
} catch (SQLException e) {
    System.err.println("Error creating administrator: " + e.getMessage());
}
```

### Example 8: Update Administrator

```java
AdministratorDAO adminDAO = new AdministratorDAOImpl();

try {
    // First, get the administrator
    Administrator admin = adminDAO.findByEmail("john@phishnet.com");
    
    if (admin != null) {
        // Update properties
        admin.setName("John D. Smith");
        admin.setRole("Cybersecurity Staff");
        
        // Save changes
        boolean updated = adminDAO.update(admin);
        
        if (updated) {
            System.out.println("Administrator updated successfully!");
        }
    }
} catch (SQLException e) {
    System.err.println("Error updating administrator: " + e.getMessage());
}
```

### Example 9: Delete Administrator

```java
AdministratorDAO adminDAO = new AdministratorDAOImpl();

try {
    int adminIdToDelete = 5;
    boolean deleted = adminDAO.delete(adminIdToDelete);
    
    if (deleted) {
        System.out.println("Administrator deleted successfully!");
    } else {
        System.out.println("Administrator not found");
    }
} catch (SQLException e) {
    System.err.println("Error deleting administrator: " + e.getMessage());
}
```

---

## Service Layer Examples

### Example 10: Using AdministratorService

```java
AdministratorService adminService = new AdministratorService();

// Get administrator by ID
Administrator admin = adminService.getAdministratorById(1);
if (admin != null) {
    System.out.println("Found: " + admin.getName());
}

// Get administrator by email
Administrator admin2 = adminService.getAdministratorByEmail("admin@phishnet.com");

// Get all administrators
List<Administrator> allAdmins = adminService.getAllAdministrators();
System.out.println("Total: " + allAdmins.size());
```

### Example 11: Update Administrator via Service

```java
AdministratorService adminService = new AdministratorService();

// Get administrator
Administrator admin = adminService.getAdministratorByEmail("jane@phishnet.com");

if (admin != null) {
    // Update
    admin.setName("Jane D. Smith");
    
    // Save through service (includes validation)
    boolean success = adminService.updateAdministrator(admin);
    
    if (success) {
        System.out.println("Update successful!");
    }
}
```

---

## Controller Examples

### Example 12: JavaFX Login Controller

```java
@FXML
private PasswordField emailField;

@FXML
private PasswordField passwordField;

private AuthenticationService authService;

@FXML
public void initialize() {
    authService = new AuthenticationService();
}

@FXML
private void handleLogin() {
    String email = emailField.getText().trim();
    String password = passwordField.getText();
    
    // Validate
    if (email.isEmpty() || password.isEmpty()) {
        showAlert(Alert.AlertType.ERROR, "Error", "Please fill all fields");
        return;
    }
    
    // Authenticate
    Administrator admin = authService.login(email, password);
    
    if (admin != null) {
        showAlert(Alert.AlertType.INFORMATION, "Success", "Welcome " + admin.getName());
        // Navigate to dashboard
    } else {
        showAlert(Alert.AlertType.ERROR, "Error", "Invalid credentials");
    }
}

private void showAlert(Alert.AlertType type, String title, String message) {
    Alert alert = new Alert(type);
    alert.setTitle(title);
    alert.setContentText(message);
    alert.showAndWait();
}
```

### Example 13: Loading FXML View

```java
public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        // Load FXML
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/SceneBuilder/LogIn.fxml")
        );
        Parent root = loader.load();
        
        // Get controller if needed
        LoginController controller = loader.getController();
        
        // Set up scene
        Scene scene = new Scene(root, 600, 400);
        primaryStage.setTitle("PhishNet Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
```

---

## Utility Examples

### Example 14: Password Hashing

```java
// Hash a password
String password = "mySecurePassword123";
String hash = SecurityUtils.hashPassword(password);
System.out.println("Hashed: " + hash);

// Verify password
boolean isValid = SecurityUtils.verifyPassword("mySecurePassword123", hash);
System.out.println("Password valid: " + isValid);

// Wrong password
boolean isInvalid = SecurityUtils.verifyPassword("wrongPassword", hash);
System.out.println("Wrong password: " + isInvalid);  // false
```

### Example 15: Email Validation

```java
// Valid email
boolean valid1 = ValidationUtils.isValidEmail("user@example.com");
System.out.println("Valid: " + valid1);  // true

// Invalid emails
boolean valid2 = ValidationUtils.isValidEmail("notanemail");
System.out.println("Valid: " + valid2);  // false

boolean valid3 = ValidationUtils.isValidEmail("user@");
System.out.println("Valid: " + valid3);  // false
```

### Example 16: Input Validation

```java
// Check if string is not empty
String name = "John Doe";
boolean notEmpty = ValidationUtils.isNotEmpty(name);
System.out.println("Not empty: " + notEmpty);  // true

// Check if number is positive
int userId = 5;
boolean positive = ValidationUtils.isPositive(userId);
System.out.println("Positive: " + positive);  // true

// Validate enum value
String role = "System Admin";
boolean validRole = ValidationUtils.isValidEnumValue(
    role, 
    new String[]{"System Admin", "Cybersecurity Staff"}
);
System.out.println("Valid role: " + validRole);  // true
```

### Example 17: Date Utilities

```java
// Get current time
LocalDateTime now = DateUtils.now();
System.out.println("Now: " + now);

// Format for display
String formatted = DateUtils.formatForDisplay(now);
System.out.println("Formatted: " + formatted);

// Convert to database format
String dbFormat = DateUtils.toDatabaseFormat(now);
System.out.println("DB Format: " + dbFormat);

// Parse from database
LocalDateTime parsed = DateUtils.fromDatabaseFormat("2024-11-11 10:30:00");
System.out.println("Parsed: " + parsed);
```

### Example 18: Data Anonymization

```java
// Anonymize email (RA 10173 compliance)
String email = "john.doe@example.com";
String anonymized = SecurityUtils.anonymizeEmail(email);
System.out.println("Anonymized email: " + anonymized);  // jo***@example.com

// Anonymize name
String name = "John Doe";
String anonName = SecurityUtils.anonymizeName(name);
System.out.println("Anonymized name: " + anonName);  // J***
```

### Example 19: Database Connection Management

```java
// Get connection
try {
    Connection conn = DatabaseConnection.getConnection();
    System.out.println("Connected: " + !conn.isClosed());
    
    // Use connection for queries
    // ...
    
} catch (SQLException e) {
    System.err.println("Connection failed: " + e.getMessage());
}

// Test connection
boolean connected = DatabaseConnection.testConnection();
System.out.println("Database accessible: " + connected);

// Close when application exits
DatabaseConnection.closeConnection();
```

---

## Complete Workflow Example

### Example 20: Complete Login to Dashboard Flow

```java
public class ApplicationWorkflow {
    
    public static void main(String[] args) {
        // 1. Initialize services
        AuthenticationService authService = new AuthenticationService();
        AdministratorService adminService = new AdministratorService();
        
        // 2. User enters credentials
        String email = "admin@phishnet.com";
        String password = "admin123";
        
        // 3. Attempt login
        Administrator currentUser = authService.login(email, password);
        
        if (currentUser != null) {
            System.out.println("=== Login Successful ===");
            System.out.println("User: " + currentUser.getName());
            System.out.println("Role: " + currentUser.getRole());
            System.out.println("Email: " + currentUser.getContactEmail());
            
            // 4. User is now logged in - can access dashboard
            // Load dashboard, enable features based on role
            
            if (currentUser.getRole().equals("System Admin")) {
                System.out.println("Access: Full system access");
                // Enable all features
            } else {
                System.out.println("Access: Standard features");
                // Enable standard features
            }
            
            // 5. Get all administrators (System Admin feature)
            List<Administrator> allAdmins = adminService.getAllAdministrators();
            System.out.println("\nTotal administrators: " + allAdmins.size());
            
            // 6. Logout when done
            authService.logout();
            System.out.println("\nLogged out successfully");
            
        } else {
            System.out.println("Login failed - invalid credentials");
        }
    }
}
```

---

## Error Handling Patterns

### Example 21: Proper Error Handling

```java
public class ErrorHandlingExample {
    
    public void loginWithErrorHandling(String email, String password) {
        AuthenticationService authService = new AuthenticationService();
        
        try {
            // Validate inputs first
            if (!ValidationUtils.isValidEmail(email)) {
                throw new IllegalArgumentException("Invalid email format");
            }
            
            if (!ValidationUtils.isValidPassword(password)) {
                throw new IllegalArgumentException("Password too short");
            }
            
            // Attempt login
            Administrator admin = authService.login(email, password);
            
            if (admin != null) {
                System.out.println("Login successful!");
            } else {
                System.err.println("Authentication failed");
            }
            
        } catch (IllegalArgumentException e) {
            System.err.println("Validation error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
```

---

## Testing Examples

### Example 22: Unit Testing Service Layer

```java
// Example unit test (using JUnit - not included in dependencies yet)
public class AuthenticationServiceTest {
    
    private AuthenticationService authService;
    
    @Before
    public void setUp() {
        authService = new AuthenticationService();
    }
    
    @Test
    public void testSuccessfulLogin() {
        Administrator admin = authService.login(
            "admin@phishnet.com", 
            "admin123"
        );
        assertNotNull(admin);
        assertEquals("System Administrator", admin.getName());
    }
    
    @Test
    public void testFailedLogin() {
        Administrator admin = authService.login(
            "admin@phishnet.com", 
            "wrongpassword"
        );
        assertNull(admin);
    }
    
    @Test
    public void testInvalidEmail() {
        Administrator admin = authService.login(
            "notanemail", 
            "password"
        );
        assertNull(admin);
    }
}
```

---

These examples demonstrate the proper usage of the layered architecture. Always:
1. Use service layer for business logic
2. Use DAO layer for database access
3. Use controllers for UI logic only
4. Validate inputs at multiple layers
5. Handle errors appropriately
6. Use utilities for common operations

