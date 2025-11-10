Here’s a clean Markdown blueprint for your \*\*Role-Based Distinction Implementation\*\* in `CybersecurityDBApp`:



```markdown

\# CybersecurityDBApp: Role-Based Distinction Blueprint



\*\*Tech Stack:\*\* Java + MySQL, MVC Architecture, JavaFX GUI Layer (FXML Controllers and Scenes)



\*\*Objective:\*\*  

Upgrade the existing CybersecurityDBApp to include:

\- Role-based login (User vs Admin)

\- Session-based identity management

\- Permission-based menu functionality

\- Admin-level report creation and validation

\- Foundation for JavaFX GUI integration



---



\## 1. Role-Based Login System



\*\*Login Menu (Startup)\*\*



```



===================================

CYBERSECURITY DATABASE SYSTEM

=============================



1\. Log in as User (Victim)

2\. Log in as Admin

3\. Exit



````



\*\*Login Behavior:\*\*

\- User inputs email + password (simulated password for now)

\- System checks credentials:

&nbsp; - If in `Victims` table → log in as User

&nbsp; - If in `Administrators` table → log in as Admin

\- On success:

&nbsp; - Create `SessionManager` object storing:

&nbsp;   - `userRole` (“User” or “Admin”)

&nbsp;   - `userId` (VictimID or AdminID)

&nbsp;   - `name`

&nbsp;   - `email`

\- On failure → display \*\*"Invalid credentials"\*\* message



---



\## 2. Session-Based Identity



\- All user-related actions automatically pull info from `SessionManager`

\- Example:

&nbsp; - Submitting an incident report:

&nbsp;   ```java

&nbsp;   int victimId = SessionManager.getCurrentUser().getId();

&nbsp;   // No need to re-enter name/email

&nbsp;   incidentController.createIncident(victimId, perpetratorId, attackTypeId, description);

&nbsp;   ```

\- Admin identity auto-linked to validation, updates, and reports



---



\## 3. Permission-Based Menus



\### USER MENU (Victim)

````



==============================

USER DASHBOARD

==============



1\. Submit Incident Report

2\. Upload Evidence

3\. View My Reports

4\. Logout



```

\*\*Capabilities:\*\*

\- Submit new incident reports (VictimID auto-filled)

\- Upload evidence linked to own incidents

\- View only own reports

\- Cannot access management or validation features



\### ADMIN MENU

```



==============================

ADMIN DASHBOARD

===============



1\. Manage Victims

2\. Manage Perpetrators

3\. Manage Attack Types

4\. View All Incident Reports

5\. Create New Incident Report (any victim)

6\. Validate / Update Reports

7\. Generate Reports

8\. Logout



````

\*\*Capabilities:\*\*

\- Full management of all record types

\- View and edit all incident reports

\- Create reports on behalf of victims

\- Assign admins for validation

\- Update threat levels and victim statuses

\- Generate system-wide reports (attack trends, top perpetrators, etc.)



---



\## 4. Admin Report Creation Rule



\- \*\*Admin creating incident:\*\*

&nbsp; - Select existing VictimID (input, dropdown, or query)

&nbsp; - Fill Perpetrator, Attack Type, Description

&nbsp; - AdminID automatically linked

\- \*\*User creating incident:\*\*

&nbsp; - VictimID, Name, Email pulled from session

&nbsp; - Form asks only for Perpetrator, Attack Type, Description



\*\*Pseudocode Example:\*\*

```java

if (SessionManager.getCurrentUser().getRole().equals("Admin")) {

&nbsp;   System.out.print("Enter VictimID: ");

&nbsp;   int victimId = scanner.nextInt();

&nbsp;   incidentController.createIncident(victimId, perpetratorId, attackTypeId, description);

} else {

&nbsp;   int victimId = SessionManager.getCurrentUser().getId();

&nbsp;   incidentController.createIncident(victimId, perpetratorId, attackTypeId, description);

}

````



---



\## 5. Future JavaFX Integration



\* Each menu maps to an FXML scene:



&nbsp; \* `Login.fxml` → Login Menu

&nbsp; \* `UserDashboard.fxml` → User Menu

&nbsp; \* `AdminDashboard.fxml` → Admin Menu

\* Controllers handle role-based access using `SessionManager`

\* SessionManager ensures identity persists across scenes



---



\*\*Notes:\*\*



\* SessionManager is central to role-based distinction

\* Menu visibility and functionality strictly depend on user role

\* Admins have elevated access; users are restricted to personal data



```

