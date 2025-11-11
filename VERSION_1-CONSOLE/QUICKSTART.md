# Quick Start Guide - How to Run the Application

## Step 1: Prerequisites Check

Make sure you have:
- ✅ **Java JDK 17+** installed
  - Check: `java -version` in terminal/command prompt
- ✅ **MySQL Server** installed and running
  - Check: MySQL service is running
- ✅ **Maven** installed (or use Maven Wrapper)
  - Check: `mvn -version` in terminal

## Step 2: Set Up the Database

1. **Start MySQL Server** (if not already running)

2. **Create the database and tables:**
   
   **Option A: Using MySQL Command Line**
   ```bash
   mysql -u root -p < DB_SCHEMA.sql
   ```
   (Enter your MySQL password when prompted)

   **Option B: Using MySQL Workbench**
   - Open MySQL Workbench
   - Connect to your MySQL server
   - Open `DB_SCHEMA.sql` file
   - Execute the entire script (Run button or Ctrl+Shift+Enter)

   **Option C: Manual Execution**
   ```sql
   -- Copy and paste the contents of DB_SCHEMA.sql into MySQL client
   ```

3. **Verify database was created:**
   ```sql
   SHOW DATABASES;  -- Should see CybersecurityDB
   USE CybersecurityDB;
   SHOW TABLES;     -- Should see 8 tables
   ```

## Step 3: Configure Database Connection

Edit the file: `src/main/java/model/DatabaseConnection.java`

Update these lines (around line 11-13):
```java
private static final String DB_URL = "jdbc:mysql://localhost:3306/CybersecurityDB";
private static final String DB_USER = "root";           // Your MySQL username
private static final String DB_PASSWORD = "";           // Your MySQL password
```

**Important:** 
- If your MySQL uses a different port (not 3306), change it in the URL
- If your MySQL has a password, enter it in `DB_PASSWORD`
- If your username is not "root", change `DB_USER`

## Step 4: Build the Project

Open terminal/command prompt in the project root directory (`00 DB APP`).

**Using Maven:**
```bash
mvn clean compile
```

This will:
- Download MySQL connector dependency
- Compile all Java files
- Create `target/classes` directory with compiled files

**If Maven is not installed**, you can:
1. Download Maven from https://maven.apache.org/download.cgi
2. Or use an IDE (IntelliJ IDEA, Eclipse, NetBeans) which has built-in Maven support

## Step 5: Run the Application

**Option A: Using Maven (Recommended)**
```bash
mvn exec:java -Dexec.mainClass="Main"
```

**Option B: Using Java directly**
```bash
# First, compile (if not done)
mvn compile

# Then run
java -cp "target/classes;target/dependency/*" Main
```
(On Linux/Mac, use `:` instead of `;` in the classpath)

**Option C: Using IDE**
1. Open project in IntelliJ IDEA / Eclipse / NetBeans
2. Right-click on `Main.java`
3. Select "Run" or "Run Main"

## Step 6: Verify It's Working

You should see:
```
Initializing Cybersecurity Incident Reporting System...
✓ Database connection established successfully.

╔════════════════════════════════════════════════════════╗
║  CYBERSECURITY INCIDENT REPORTING SYSTEM              ║
╚════════════════════════════════════════════════════════╝

1. Manage Victims
2. Manage Perpetrators
...
```

If you see this, **you're ready to use the system!** 🎉

## Troubleshooting

### Error: "Database connection failed"

**Solutions:**
1. Check MySQL server is running:
   ```bash
   # Windows
   services.msc  # Look for MySQL service
   
   # Linux/Mac
   sudo systemctl status mysql
   ```

2. Verify database exists:
   ```sql
   SHOW DATABASES LIKE 'CybersecurityDB';
   ```

3. Check credentials in `DatabaseConnection.java`

4. Test connection manually:
   ```sql
   mysql -u root -p
   # Then: USE CybersecurityDB;
   ```

### Error: "MySQL JDBC Driver not found"

**Solution:**
```bash
mvn dependency:resolve
```

This downloads the MySQL connector JAR file.

### Error: "Could not find or load main class Main"

**Solutions:**
1. Make sure you're in the project root directory
2. Rebuild: `mvn clean compile`
3. Check that `target/classes/Main.class` exists

### Error: "Access denied for user"

**Solution:**
- Update `DB_USER` and `DB_PASSWORD` in `DatabaseConnection.java`
- Or create a MySQL user with proper permissions:
  ```sql
  CREATE USER 'cyberuser'@'localhost' IDENTIFIED BY 'password';
  GRANT ALL PRIVILEGES ON CybersecurityDB.* TO 'cyberuser'@'localhost';
  FLUSH PRIVILEGES;
  ```

## Quick Test Run

Once running, try this quick test:

1. **Add an Attack Type:**
   - Select option `3` (Manage Attack Types)
   - Select option `1` (Add Attack Type)
   - Enter: Name: "Phishing", Description: "Test", Severity: `1` (Low)

2. **Register a Victim:**
   - Select option `1` (Manage Victims)
   - Select option `1` (Register New Victim)
   - Enter: Name: "Test User", Email: "test@email.com"

3. **View Reports:**
   - Select option `9` (Generate Reports)
   - Try any report option

If these work, your system is fully operational! ✅

## Next Steps

- Read `README.md` for detailed feature documentation
- Check `sampleoutput.md` for example interactions
- Start adding real data and generating reports

---

**Need Help?** Check the error messages - they usually indicate what's wrong. Most common issues are database connection problems.

