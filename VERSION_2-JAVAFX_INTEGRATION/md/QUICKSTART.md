# Quick Start Guide - JavaFX Version

## 🚀 Getting Started in 5 Minutes

### Step 1: Database Setup (2 minutes)

```bash
# Start MySQL
# Windows: 
net start MySQL80

# Run the schema
mysql -u root -p < DB_SCHEMA_V2.sql
```

### Step 2: Configure (1 minute)

Open `src/main/java/com/group2/dbapp/util/DatabaseConnection.java` and update:

```java
private static final String DB_PASSWORD = "YOUR_MYSQL_PASSWORD";
```

### Step 3: Run (2 minutes)

```bash
cd VERSION_2-JAVAFX_INTEGRATION
mvn javafx:run
```

### Step 4: Login

- **Email**: `admin@phishnet.com`
- **Password**: `admin123`

---

## 📁 Project Structure at a Glance

```
controller/     → UI controllers (handles user actions)
service/        → Business logic (processes data)
dao/            → Database operations (CRUD)
model/          → Data models (entities)
util/           → Helper utilities
```

## 🔑 Key Classes

### For UI Development
- `LoginController.java` - Example of JavaFX controller

### For Business Logic
- `AuthenticationService.java` - Login/registration logic
- `AdministratorService.java` - Admin management

### For Database
- `AdministratorDAO.java` - Database interface
- `AdministratorDAOImpl.java` - Database implementation

## 🛠️ Common Tasks

### Adding a New Administrator Programmatically

```java
AuthenticationService authService = new AuthenticationService();
authService.register("John Doe", "Cybersecurity Staff", 
    "john@phishnet.com", "password123");
```

### Verifying Database Connection

The application automatically tests the database connection on startup. Check console output:

```
Database connection successful!
Application started successfully!
```

## 🐛 Debugging Tips

1. **Check console output** - All errors are logged to console
2. **Verify database** - Run: `mysql -u root -p -e "USE CybersecurityDB; SELECT * FROM Administrators;"`
3. **Test connection** - `DatabaseConnection.testConnection()` returns boolean

## 📊 Architecture Flow

```
User clicks Login Button
         ↓
LoginController.handleLogin()
         ↓
AuthenticationService.login()
         ↓
AdministratorDAO.findByEmail()
         ↓
MySQL Database Query
         ↓
Result returned up the chain
         ↓
UI shows success/error
```

## 🔐 Security Notes

- Passwords are **never stored in plain text**
- All passwords are hashed with **SHA-256**
- Database queries use **PreparedStatements** (SQL injection safe)
- Email validation before processing

## 📝 Testing Checklist

- [ ] Database connection successful
- [ ] Login with valid credentials works
- [ ] Login with invalid credentials fails appropriately
- [ ] Error messages display correctly
- [ ] Application closes cleanly

## 🎯 Next Development Phase

1. Create main dashboard FXML
2. Add dashboard controller
3. Navigate from login to dashboard
4. Implement incident reporting features

---

**Need Help?** Check the full README.md for detailed documentation.

