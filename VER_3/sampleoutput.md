# Cybersecurity Incident Reporting System - Sample Output

This document provides example console interactions and outputs from the Cybersecurity Incident Reporting System.

## Table of Contents
1. [System Startup](#system-startup)
2. [Victim Management](#victim-management)
3. [Perpetrator Management](#perpetrator-management)
4. [Attack Type Management](#attack-type-management)
5. [Administrator Management](#administrator-management)
6. [Incident Reporting](#incident-reporting)
7. [Evidence Upload](#evidence-upload)
8. [Report Generation](#report-generation)
9. [Database Records](#database-records)

---

## System Startup

```
Initializing Cybersecurity Incident Reporting System...
✓ Database connection established successfully.

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

Select option:
```

---

## Victim Management

### Register New Victim

```
=== VICTIM MANAGEMENT ===
1. Register New Victim
2. View All Victims
3. View Victim by ID
4. Update Victim Status
5. Delete Victim
6. Back to Main Menu
Select option: 1

--- Register New Victim ---
Enter name: Juan Dela Cruz
Enter email: juan.delacruz@email.com
Victim created successfully.
```

### View All Victims

```
=== VICTIM MANAGEMENT ===
Select option: 2

--- All Victims ---
ID         Name                           Email                           Status        
-------------------------------------------------------------------------------------
1          Juan Dela Cruz                juan.delacruz@email.com          Active        
2          Maria Santos                  maria.santos@email.com          Active        
3          Jose Garcia                   jose.garcia@email.com           Flagged       
```

### Auto-Flagging Example

When a victim reports more than 5 incidents within a month, the system automatically flags them:

```
Incident report created successfully.
Auto-flagged VictimID 3 (>5 incidents in last month)
```

---

## Perpetrator Management

### Add New Perpetrator

```
=== PERPETRATOR MANAGEMENT ===
1. Add New Perpetrator
2. View All Perpetrators
3. View Perpetrator by ID
4. Update Threat Level
5. Back to Main Menu
Select option: 1

--- Add New Perpetrator ---
Enter identifier: +639123456789
Select identifier type:
1. Phone Number
2. Email Address
3. Social Media Account
4. Website URL
5. IP Address
Choice: 1
Enter associated name (optional): John Doe
Perpetrator created successfully. ID: 1
```

### View All Perpetrators

```
=== PERPETRATOR MANAGEMENT ===
Select option: 2

--- All Perpetrators ---
ID         Identifier                    Type                      Associated Name      Threat Level    
----------------------------------------------------------------------------------------------------
1          +639123456789                 Phone Number              John Doe             UnderReview    
2          scammer@phishing.com          Email Address             N/A                  Malicious      
3          192.168.1.100                IP Address                N/A                  Suspected      
```

### Auto-Escalation Example

When ≥3 unique victims report the same perpetrator within 7 days, the system automatically escalates the threat level:

```
Incident report created successfully.
Auto-escalated PerpetratorID 2 to Malicious (≥3 victims in 7 days)
```

---

## Attack Type Management

### Add Attack Type

```
=== ATTACK TYPE MANAGEMENT ===
1. Add Attack Type
2. View All Attack Types
3. Update Attack Type
4. Delete Attack Type
5. Back to Main Menu
Select option: 1

Enter attack name: Phishing
Enter description: Fraudulent attempt to obtain sensitive information
Select severity:
1. Low
2. Medium
3. High
Choice: 3
Attack type created successfully.
```

### View All Attack Types

```
--- All Attack Types ---
ID         Name                          Description                                        Severity      
---------------------------------------------------------------------------------------------------------
1          Phishing                      Fraudulent attempt to obtain sensitive information High          
2          Ransomware                    Malicious software that encrypts files              High          
3          Identity Theft                Unauthorized use of personal information          Medium        
4          Social Engineering            Manipulation to divulge confidential information  Medium        
```

---

## Administrator Management

### Register Administrator

```
=== ADMINISTRATOR MANAGEMENT ===
1. Register Administrator
2. View All Administrators
3. Authenticate (Login)
4. Back to Main Menu
Select option: 1

Enter name: Admin User
Select role:
1. System Admin
2. Cybersecurity Staff
Choice: 1
Enter email: admin@cybersecurity.local
Administrator created successfully.
```

### Authenticate (Login)

```
=== ADMINISTRATOR MANAGEMENT ===
Select option: 3

Enter email to login: admin@cybersecurity.local

✓ Login successful!
Welcome, Admin User (System Admin)
```

---

## Incident Reporting

### Create Incident Report

```
Select option: 5

--- Create Incident Report ---
Enter Victim ID: 1
Enter Perpetrator Identifier: +639123456789
Select Identifier Type:
1. Phone Number
2. Email Address
3. Social Media Account
4. Website URL
5. IP Address
Choice: 1
Enter Associated Name (optional): 
Enter Attack Type ID: 1
Enter Admin ID (or 0 for none): 1
Enter description: Received suspicious SMS asking for OTP code
Incident report created successfully.
```

### View Incidents

```
Select option: 7

--- All Incident Reports ---
Incident ID  Victim ID  Perpetrator ID  Attack Type  Admin ID   Date Reported          Status        
---------------------------------------------------------------------------------------------------
1            1          1               1            1          2024-01-15 10:30:00    Pending       
2            2          2               1            1          2024-01-15 11:15:00    Validated     
3            3          1               2            2          2024-01-15 14:20:00    Pending       

Options:
1. Validate Incident Report
2. Back
Choice: 1
Enter Incident ID to validate: 1
Enter Admin ID: 1
Incident report validated successfully.
```

---

## Evidence Upload

### Upload Evidence

```
Select option: 6

--- Upload Evidence ---
Enter Incident ID: 1
Select Evidence Type:
1. Screenshot
2. Email
3. File
4. Chat Log
Choice: 1
Enter file path: C:\Evidence\screenshot_001.png
Enter Admin ID (or 0 for none): 1
Evidence uploaded successfully.
```

### Manage Evidence

```
=== EVIDENCE MANAGEMENT ===
1. View All Evidence
2. View Evidence by Incident ID
3. Update Evidence Status
4. Back to Main Menu
Select option: 1

--- All Evidence ---
Evidence ID  Incident ID  Type           File Path                                Submission Date       Status        
-------------------------------------------------------------------------------------------------------------------
1            1            Screenshot     C:\Evidence\screenshot_001.png          2024-01-15 10:35:00   Pending       
2            2            Email          C:\Evidence\email_phishing.eml           2024-01-15 11:20:00   Verified      
3            3            File           C:\Evidence\malicious_file.exe           2024-01-15 14:25:00   Pending       

=== EVIDENCE MANAGEMENT ===
Select option: 3

Enter Evidence ID: 1
Select new status:
1. Pending
2. Verified
3. Rejected
Choice: 2
Enter Admin ID: 1
Evidence status updated successfully.
⚠️  Verified evidence pattern detected. Perpetrator escalated to Malicious.
```

---

## Report Generation

### Monthly Attack Trends Report

```
=== REPORT GENERATION ===
1. Monthly Attack Trends Report
2. Top Perpetrators Report
3. Victim Activity Report
4. Incident Evidence Summary Report
5. Back to Main Menu
Select option: 1

--- Monthly Attack Trends Report ---
1. Current Month
2. Specific Month
Choice: 1

=== MONTHLY ATTACK TRENDS REPORT ===
Period: 2024-01

ATTACK TYPE BREAKDOWN:
Attack Type                   Severity        Incidents       Victims         Evidence        
------------------------------------------------------------------------------------------
Phishing                      High            15              12              8               
Ransomware                    High            8               6               5               
Identity Theft                Medium          5               5               3               

TIME OF DAY ANALYSIS:
Hour      Incidents        
-------------------------
8         2                
9         3                
10        4                
11        2                
14        3                
15        1                
```

### Top Perpetrators Report

```
=== REPORT GENERATION ===
Select option: 2

--- Top Perpetrators Report ---
1. Current Month
2. Specific Month
Choice: 1

=== TOP PERPETRATORS REPORT ===
Period: 2024-01

ID    Identifier                    Type                 Threat Level    Incidents    Victims      Attack Types                   
-----------------------------------------------------------------------------------------------------------------------------
1     1    +639123456789                 Phone Number         Malicious       8            5            Phishing, Ransomware          
2     2    scammer@phishing.com          Email Address        Malicious       6            4            Phishing                      
3     3    192.168.1.100                IP Address           Suspected       3            3            Identity Theft               
```

### Victim Activity Report

```
=== REPORT GENERATION ===
Select option: 3

--- Victim Activity Report ---
1. Current Month
2. Specific Month
Choice: 1

=== VICTIM ACTIVITY REPORT ===
Period: 2024-01

Victim ID   Name                           Status           Incidents       Unique Perpetrators    
------------------------------------------------------------------------------------------
3          J***                            Flagged          8              3                       
1          J***                            Active            5              2                       
2          M***                            Active            3              2                       

⚠️  FREQUENTLY TARGETED VICTIMS (>5 incidents):
Victim ID   Name                           Incidents        
-------------------------------------------------------
3          J***                            8                
1          J***                            5                
```

*Note: Names are anonymized for privacy compliance (RA 10173)*

### Incident Evidence Summary Report

```
=== REPORT GENERATION ===
Select option: 4

--- Incident Evidence Summary Report ---

=== INCIDENT EVIDENCE SUMMARY REPORT ===

Evidence ID  Incident ID  Type           File Path                            Submission Date       Status         Reviewing Admin                   
---------------------------------------------------------------------------------------------------------------------------------------
2            2            Email          C:\Evidence\email_phishing.eml       2024-01-15 11:20:00   Verified      Admin User                        
1            1            Screenshot     C:\Evidence\screenshot_001.png       2024-01-15 10:35:00   Verified      Admin User                        
3            3            File           C:\Evidence\malicious_file.exe       2024-01-15 14:25:00   Pending       N/A                               

SUMMARY STATISTICS:
Status                Count           
-----------------------------------
Pending               1                
Verified              2                
Rejected              0                
```

---

## Database Records

### Sample Victims Table

```sql
SELECT * FROM Victims;

VictimID | Name           | ContactEmail              | AccountStatus | DateCreated
---------|----------------|---------------------------|---------------|-------------------
1        | Juan Dela Cruz | juan.delacruz@email.com   | Active        | 2024-01-10 09:00:00
2        | Maria Santos   | maria.santos@email.com    | Active        | 2024-01-11 10:15:00
3        | Jose Garcia    | jose.garcia@email.com     | Flagged       | 2024-01-12 11:30:00
```

### Sample Perpetrators Table

```sql
SELECT * FROM Perpetrators;

PerpetratorID | Identifier            | IdentifierType    | AssociatedName | ThreatLevel  | LastIncidentDate
--------------|----------------------|-------------------|----------------|--------------|------------------
1             | +639123456789        | Phone Number      | John Doe       | Malicious    | 2024-01-15 14:20:00
2             | scammer@phishing.com | Email Address     | NULL           | Malicious    | 2024-01-15 11:15:00
3             | 192.168.1.100        | IP Address        | NULL           | Suspected    | 2024-01-14 16:45:00
```

### Sample IncidentReports Table

```sql
SELECT * FROM IncidentReports;

IncidentID | VictimID | PerpetratorID | AttackTypeID | AdminID | DateReported          | Description                          | Status
-----------|---------|---------------|--------------|---------|----------------------|--------------------------------------|----------
1          | 1       | 1             | 1            | 1       | 2024-01-15 10:30:00  | Received suspicious SMS asking for OTP| Validated
2          | 2       | 2             | 1            | 1       | 2024-01-15 11:15:00  | Phishing email received              | Validated
3          | 3       | 1             | 2            | 2       | 2024-01-15 14:20:00  | Ransomware attack detected           | Pending
```

### Sample ThreatLevelLog Table

```sql
SELECT * FROM ThreatLevelLog;

LogID | PerpetratorID | OldThreatLevel | NewThreatLevel | ChangeDate            | AdminID
------|---------------|---------------|----------------|----------------------|--------
1     | 1             | UnderReview    | Suspected      | 2024-01-13 09:00:00  | 1
2     | 1             | Suspected      | Malicious      | 2024-01-15 10:30:00  | NULL
3     | 2             | UnderReview    | Malicious      | 2024-01-14 08:00:00  | NULL
```

*Note: AdminID is NULL for auto-escalations*

### Sample VictimStatusLog Table

```sql
SELECT * FROM VictimStatusLog;

LogID | VictimID | OldStatus | NewStatus | ChangeDate            | AdminID
------|----------|-----------|-----------|----------------------|--------
1     | 3        | Active    | Flagged   | 2024-01-15 14:20:00  | NULL
```

*Note: AdminID is NULL for auto-flagging*

### Sample EvidenceUpload Table

```sql
SELECT * FROM EvidenceUpload;

EvidenceID | IncidentID | EvidenceType | FilePath                          | SubmissionDate      | VerifiedStatus | AdminID
-----------|------------|--------------|-----------------------------------|---------------------|----------------|--------
1          | 1          | Screenshot   | C:\Evidence\screenshot_001.png   | 2024-01-15 10:35:00  | Verified       | 1
2          | 2          | Email        | C:\Evidence\email_phishing.eml    | 2024-01-15 11:20:00  | Verified       | 1
3          | 3          | File         | C:\Evidence\malicious_file.exe     | 2024-01-15 14:25:00  | Pending        | NULL
```

---

## Key Features Demonstrated

1. **Auto-Flagging**: Victims with >5 incidents in a month are automatically flagged
2. **Auto-Escalation**: Perpetrators with ≥3 unique victims in 7 days are escalated to "Malicious"
3. **Evidence Verification**: Verified evidence can trigger pattern detection
4. **Privacy Compliance**: Reports anonymize victim data (RA 10173 compliance)
5. **Comprehensive Logging**: All status and threat level changes are logged
6. **Role-Based Access**: System supports different administrator roles

---

## System Exit

```
Select option: 10

Thank you for using the Cybersecurity Incident Reporting System!

System shutdown complete.
```

---

## Notes

- All timestamps are in `YYYY-MM-DD HH:MM:SS` format
- Email addresses and names are anonymized in reports for privacy compliance
- Auto-escalations and auto-flagging are logged with NULL AdminID
- The system uses prepared statements to prevent SQL injection
- Database connection is tested on startup

