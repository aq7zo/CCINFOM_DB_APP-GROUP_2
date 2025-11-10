# Cybersecurity Incident Reporting System

A comprehensive Java + MySQL database application for managing cybersecurity incidents, built using the Model-View-Controller (MVC) architecture pattern.

## Features

- **Victim Management**: Register and manage victims with automatic flagging for high-risk accounts
- **Perpetrator Tracking**: Track offenders with automatic threat level escalation
- **Incident Reporting**: Create and validate incident reports linking victims, perpetrators, and attack types
- **Evidence Management**: Upload and verify evidence for incidents
- **Automated Logging**: Automatic logging of status changes and threat level escalations
- **Comprehensive Reports**: Generate monthly trends, top perpetrators, victim activity, and evidence summaries
- **Privacy Compliance**: Data anonymization for reports (RA 10173 - Philippine Data Privacy Act)

## Technology Stack

- **Java**: JDK 17+
- **Database**: MySQL
- **Build Tool**: Maven
- **Architecture**: MVC (Model-View-Controller)
- **Interface**: Console-based (designed for future GUI upgrade)

## Project Structure

```
src/
 ├── model/          # Data models (Victim, Perpetrator, etc.)
 ├── view/           # Console interfaces (future: GUI)
 ├── controller/     # Business logic and database operations
 ├── utils/          # Utility classes (Security, Validation, Reports, Dates)
 └── Main.java       # Application entry point
```

## Prerequisites

1. **Java Development Kit (JDK) 17 or higher**
   - Download from [Oracle](https://www.oracle.com/java/technologies/downloads/) or [OpenJDK](https://openjdk.org/)

2. **MySQL Server**
   - Download from [MySQL Official Site](https://dev.mysql.com/downloads/mysql/)
   - Ensure MySQL service is running

3. **Maven** (optional, can use Maven Wrapper)
   - Download from [Apache Maven](https://maven.apache.org/download.cgi)

## Setup Instructions

### 1. Database Setup

1. Start your MySQL server
2. Open MySQL command line or MySQL Workbench
3. Run the provided `DB_SCHEMA.sql` file to create the database and tables:

```sql
mysql -u root -p < DB_SCHEMA.sql
```

Or execute the SQL file contents in your MySQL client.

### 2. Configure Database Connection

Edit `src/main/java/model/DatabaseConnection.java` and update the connection parameters:

```java
private static final String DB_URL = "jdbc:mysql://localhost:3306/CybersecurityDB";
private static final String DB_USER = "root";        // Your MySQL username
private static final String DB_PASSWORD = "";        // Your MySQL password
```

### 3. Build the Project

Using Maven:

```bash
mvn clean compile
```

Or using Maven Wrapper (if available):

```bash
./mvnw clean compile
```

### 4. Run the Application

Using Maven:

```bash
mvn exec:java -Dexec.mainClass="Main"
```

Or compile and run manually:

```bash
javac -cp "target/classes:lib/*" src/main/java/**/*.java
java -cp "target/classes:lib/*" Main
```

## Usage

### Starting the Application

When you run the application, you'll see the main menu:

```
╔════════════════════════════════════════════════════════╗
║  CYBERSECURITY INCIDENT REPORTING SYSTEM              ║
╚════════════════════════════════════════════════════════╝

1. Manage Victims
2. Manage Perpetrators
3. Manage Attack Types
4. Manage Administrators
5. Create Incident Report
6. Upload Evidence
7. View Incidents
8. Manage Evidence
9. Generate Reports
10. Exit
```

### Key Features

#### Auto-Flagging
- Victims reporting **>5 incidents within a month** are automatically flagged
- Status changes are logged in `VictimStatusLog`

#### Auto-Escalation
- Perpetrators with **≥3 unique victims within 7 days** are automatically escalated to "Malicious"
- Threat level changes are logged in `ThreatLevelLog`

#### Evidence Verification
- Admins can verify evidence (Pending → Verified/Rejected)
- Verified evidence patterns can trigger additional escalations

#### Report Generation
- **Monthly Attack Trends**: Breakdown by attack type, time of day
- **Top Perpetrators**: Ranked by incident count
- **Victim Activity**: Shows victim activity with privacy-compliant anonymization
- **Evidence Summary**: Complete evidence tracking with verification status

## Sample Output

See `sampleoutput.md` for detailed examples of console interactions and database records.

## Security Features

- **Prepared Statements**: All database queries use prepared statements to prevent SQL injection
- **Input Validation**: Email format validation, enum value checking
- **Data Encryption**: Basic encryption utilities for sensitive data (extensible)
- **Privacy Compliance**: Report outputs anonymize personal information (RA 10173)

## Future Enhancements

The system is designed to be easily upgraded with a GUI:

- Replace console View classes with JavaFX or Swing interfaces
- Controllers and Models remain unchanged
- Business logic is fully separated from presentation

## Troubleshooting

### Database Connection Issues

If you see:
```
✗ Database connection failed: ...
```

Check:
1. MySQL server is running
2. Database `CybersecurityDB` exists (run `DB_SCHEMA.sql`)
3. Credentials in `DatabaseConnection.java` are correct
4. MySQL connector JAR is in classpath (handled by Maven)

### Compilation Errors

Ensure:
- JDK 17+ is installed and configured
- Maven dependencies are downloaded (`mvn dependency:resolve`)
- All source files are in correct package directories

## Database Schema

The system uses the following main tables:

- **Victims**: Victim information and account status
- **Perpetrators**: Offender identifiers and threat levels
- **AttackTypes**: Predefined attack categories
- **Administrators**: System users with roles
- **IncidentReports**: Links victims, perpetrators, and attack types
- **EvidenceUpload**: Evidence files linked to incidents
- **ThreatLevelLog**: History of perpetrator threat level changes
- **VictimStatusLog**: History of victim status changes

See `DB_SCHEMA.sql` for complete schema definition.

## License

This project is created for educational purposes as part of a database application course.

## Author

Cybersecurity Incident Reporting System
Built with Java + MySQL using MVC Architecture

