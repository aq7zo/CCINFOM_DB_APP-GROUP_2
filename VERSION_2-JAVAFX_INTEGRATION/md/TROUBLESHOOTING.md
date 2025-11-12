# Troubleshooting Guide

## Common Issues and Solutions

### 1. ❌ Error: "Location is not set" (FXML Not Found)

**Error Message:**
```
java.lang.IllegalStateException: Location is not set.
    at javafx.fxml.FXMLLoader.loadImpl(FXMLLoader.java:2556)
```

**Cause:**
The FXMLLoader cannot find the FXML file because it's not in the correct location.

**Solution:**
1. FXML files MUST be in `src/main/resources/` directory
2. Verify file location:
   ```
   src/main/resources/SceneBuilder/LogIn.fxml  ✓ Correct
   SceneBuilder/LogIn.fxml                      ✗ Wrong
   ```

3. If you moved the FXML file, rebuild the project:
   ```bash
   mvn clean compile
   ```

**Why this happens:**
- JavaFX looks for resources in the classpath
- Maven copies files from `src/main/resources/` to `target/classes/`
- Files in the project root are not included in the classpath

---

### 2. ❌ Database Connection Failed

**Error Message:**
```
Could not connect to the database
```

**Possible Causes & Solutions:**

#### A. MySQL Server Not Running
```bash
# Windows - Start MySQL
net start MySQL80

# Check if MySQL is running
netstat -an | findstr "3306"
```

#### B. Wrong Database Credentials
Check `src/main/java/com/group2/dbapp/util/DatabaseConnection.java`:
```java
private static final String DB_URL = "jdbc:mysql://localhost:3306/CybersecurityDB";
private static final String DB_USER = "root";
private static final String DB_PASSWORD = "YOUR_PASSWORD_HERE";  // ← Update this!
```

#### C. Database Not Created
```bash
# Create the database
mysql -u root -p < DB_SCHEMA_V2.sql

# Verify database exists
mysql -u root -p -e "SHOW DATABASES LIKE 'CybersecurityDB';"
```

---

### 3. ❌ Maven Build Failure

**Error Message:**
```
[ERROR] Failed to execute goal
```

**Solutions:**

#### A. Clean and Rebuild
```bash
mvn clean install
```

#### B. Update Dependencies
```bash
mvn dependency:resolve
```

#### C. Check Java Version
```bash
java -version  # Should be 17+
mvn -version   # Check Maven configuration
```

---

### 4. ❌ JavaFX Runtime Components Missing

**Error Message:**
```
Error: JavaFX runtime components are missing
```

**Solution:**
Don't run with `java -jar`, use Maven:
```bash
mvn javafx:run  # ✓ Correct
java -jar target/dbapp-2.0.0.jar  # ✗ Won't work without module path
```

**Why:**
- JavaFX requires modules to be on the module path
- Maven plugin handles this automatically
- Running directly with java requires complex module configuration

---

### 5. ❌ Login Failed with Valid Credentials

**Possible Causes:**

#### A. Wrong Password Hash
The sample admin uses:
- **Password**: `admin123`
- **Hash**: `240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9`

Verify in database:
```sql
SELECT ContactEmail, PasswordHash FROM Administrators WHERE ContactEmail = 'admin@phishnet.com';
```

#### B. Password Hashing Mismatch
Test password hash:
```java
String hash = SecurityUtils.hashPassword("admin123");
System.out.println(hash);
// Should output: 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
```

---

### 6. ❌ ClassNotFoundException: com.mysql.cj.jdbc.Driver

**Error Message:**
```
java.lang.ClassNotFoundException: com.mysql.cj.jdbc.Driver
```

**Solution:**
```bash
# Update dependencies
mvn dependency:resolve

# Verify MySQL connector is downloaded
ls ~/.m2/repository/com/mysql/mysql-connector-j/8.0.33/
```

---

### 7. ❌ Scene Builder FXML Version Warning

**Warning Message:**
```
WARNING: Loading FXML document with JavaFX API of version 25 by JavaFX runtime of version 21.0.1
```

**Solution:**
This is just a warning and can be safely ignored. The FXML was created with Scene Builder using JavaFX 25, but runtime is 21. They are compatible.

To remove the warning, edit `LogIn.fxml`:
```xml
<!-- Change this: -->
xmlns:fx="http://javafx.com/fxml/1"

<!-- To match your runtime version -->
```

---

### 8. ❌ GUI Doesn't Show Up / Blank Window

**Possible Causes:**

#### A. FXML Controller Not Set
Check `LogIn.fxml`:
```xml
<Pane ... fx:controller="main.java.com.group2.dbapp.controller.LoginController">
```

#### B. FXML fx:id Mismatch
In FXML:
```xml
<PasswordField fx:id="emailField" />
```

In Controller:
```java
@FXML
private PasswordField emailField;  // ← Must match!
```

---

### 9. ❌ Port 3306 Already in Use

**Error Message:**
```
Can't connect to MySQL server on 'localhost:3306'
```

**Solution:**
```bash
# Check what's using port 3306
netstat -ano | findstr "3306"

# If another MySQL instance is running, stop it
net stop MySQL80
net start MySQL80
```

---

### 10. ❌ NullPointerException in Controller

**Common Causes:**

#### A. @FXML Fields Not Injected
Make sure:
1. Fields are annotated with `@FXML`
2. `fx:id` in FXML matches Java field name
3. `initialize()` method is called automatically

#### B. Accessing UI Components Before Initialization
```java
// ✗ Wrong - in constructor
public LoginController() {
    emailField.setText("test");  // NullPointerException!
}

// ✓ Correct - in initialize
@FXML
public void initialize() {
    emailField.setText("test");  // Works!
}
```

---

## Debugging Tips

### Enable Debug Logging
```bash
mvn javafx:run -X  # Verbose Maven output
```

### Test Database Connection Separately
```java
public static void main(String[] args) {
    boolean connected = DatabaseConnection.testConnection();
    System.out.println("Connected: " + connected);
}
```

### Verify FXML Loading
```java
URL fxmlUrl = getClass().getResource("/SceneBuilder/LogIn.fxml");
System.out.println("FXML URL: " + fxmlUrl);  // Should not be null
```

### Check Classpath Resources
```bash
# After building, check if FXML is in target
ls target/classes/SceneBuilder/LogIn.fxml
```

---

## Getting Help

### Check These First
1. ✅ MySQL is running
2. ✅ Database exists and has correct schema
3. ✅ Database credentials are correct
4. ✅ FXML files are in `src/main/resources/`
5. ✅ Running with `mvn javafx:run`
6. ✅ Java 17+ is installed
7. ✅ No other application using port 3306

### Still Having Issues?

1. **Clean rebuild:**
   ```bash
   mvn clean install
   mvn javafx:run
   ```

2. **Check logs:**
   - Look for red error messages in console
   - Check for SQLException messages
   - Note any ClassNotFoundException

3. **Verify file structure:**
   ```bash
   tree /F src  # Windows
   find src -type f  # Linux/Mac
   ```

---

## Quick Fixes Summary

| Issue | Quick Fix |
|-------|-----------|
| FXML not found | Move to `src/main/resources/` |
| DB connection failed | Check MySQL running & credentials |
| Maven build error | `mvn clean install` |
| JavaFX missing | Use `mvn javafx:run` |
| Login fails | Verify password hash in DB |
| NullPointerException | Check `@FXML` annotations |
| Port in use | Restart MySQL service |
| Class not found | `mvn dependency:resolve` |

---

**Last Updated:** November 2024

