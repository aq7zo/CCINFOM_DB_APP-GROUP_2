\# JavaFX Migration Blueprint



\## Overview



This document outlines the \*\*architectural blueprint\*\* for migrating the existing console-based Cybersecurity Incident Reporting System to a \*\*modern JavaFX application\*\* using \*\*FXML\*\* and \*\*Scene Builder\*\*.

The new architecture emphasizes \*\*separation of concerns\*\*, \*\*modularity\*\*, and \*\*maintainability\*\*, aligning with industry-standard layering patterns.



---



\## Migration Goals



| Objective                | Description                                                                                               |

| ------------------------ | --------------------------------------------------------------------------------------------------------- |

| \*\*UI Modernization\*\*     | Replace pure Java GUI/console with JavaFX FXML and Scene Builder for cleaner, responsive design.          |

| \*\*Layered Architecture\*\* | Introduce `Controller → Service → DAO` architecture for improved scalability and code reusability.        |

| \*\*CSS Styling\*\*          | Use JavaFX CSS for consistent theming and branding across all views.                                      |

| \*\*Loose Coupling\*\*       | Ensure FXML controllers interact only with Services, not directly with DAOs.                              |

| \*\*Dependency Injection\*\* | Use clean instantiation or DI frameworks to manage relationships between controllers, services, and DAOs. |



---



\## Target Architecture



\### High-Level Diagram



```

┌────────────────────────────┐

│         JavaFX UI          │  ← FXML + Scene Builder + CSS

│ (FXML Controllers / Views) │

└─────────────┬──────────────┘

&nbsp;             │

&nbsp;             ↓

┌────────────────────────────┐

│        Service Layer        │  ← Business Logic, Validation, Coordination

└─────────────┬──────────────┘

&nbsp;             │

&nbsp;             ↓

┌────────────────────────────┐

│          DAO Layer          │  ← Database CRUD, Persistence Logic

└────────────────────────────┘

```



---



\## Layer Responsibilities



\### 1. \*\*UI Layer (JavaFX + FXML)\*\*



\* Designed using \*\*Scene Builder\*\* with layout XML (`.fxml`) files.

\* FXML controllers handle \*\*UI logic only\*\* (button actions, field binding, view switching).

\* \*\*CSS\*\* applied globally via a `theme.css` for consistent visuals.



\*\*Example Structure:\*\*



```

src/main/resources/

├── fxml/

│   ├── Dashboard.fxml

│   ├── VictimView.fxml

│   ├── IncidentReportView.fxml

│   └── LoginView.fxml

├── css/

│   └── theme.css

└── images/

&nbsp;   └── icons/

```



\*\*Example FXML Controller:\*\*



```java

public class VictimViewController extends BaseController {

&nbsp;   @FXML private TableView<Victim> victimTable;

&nbsp;   @FXML private Button addVictimButton;



&nbsp;   private final VictimService victimService = new VictimService();



&nbsp;   @FXML

&nbsp;   private void initialize() {

&nbsp;       victimTable.setItems(victimService.getAllVictims());

&nbsp;   }



&nbsp;   @FXML

&nbsp;   private void handleAddVictim() {

&nbsp;       victimService.addVictim(...);

&nbsp;   }

}

```



---



\### 2. \*\*Service Layer\*\*



\* Acts as a \*\*bridge between Controllers and DAOs\*\*.

\* Contains \*\*business logic\*\*, \*\*validation\*\*, and \*\*workflow coordination\*\*.

\* Services should not contain any GUI-specific logic.



\*\*Key Principles:\*\*



\* Controllers never directly access DAOs.

\* Services may combine multiple DAO calls.

\* Services handle transaction consistency and error propagation.



\*\*Example:\*\*



```java

public class IncidentService {

&nbsp;   private final IncidentDAO incidentDAO = new IncidentDAO();

&nbsp;   private final PerpetratorService perpetratorService = new PerpetratorService();



&nbsp;   public void createIncident(Incident incident) {

&nbsp;       incidentDAO.insert(incident);

&nbsp;       perpetratorService.autoEscalateThreatLevel(incident.getPerpetratorId());

&nbsp;   }

}

```



---



\### 3. \*\*DAO Layer\*\*



\* Responsible solely for \*\*database access\*\* (CRUD operations, queries).

\* Implements a \*\*Data Access Object (DAO)\*\* pattern for each entity.

\* Uses JDBC (or JPA in future iterations) for persistence.



\*\*Example:\*\*



```java

public class VictimDAO {

&nbsp;   public List<Victim> findAll() {

&nbsp;       String query = "SELECT \* FROM Victims";

&nbsp;       // Execute query → map ResultSet to Victim objects

&nbsp;   }



&nbsp;   public void insert(Victim victim) {

&nbsp;       String query = "INSERT INTO Victims (Name, Email) VALUES (?, ?)";

&nbsp;       // PreparedStatement with parameters

&nbsp;   }

}

```



---



\## Project Structure (Proposed)



```

src/

└── main/

&nbsp;   ├── java/

&nbsp;   │   ├── app/

&nbsp;   │   │   ├── MainApp.java              # JavaFX entry point

&nbsp;   │   │   ├── controllers/              # FXML Controllers

&nbsp;   │   │   ├── services/                 # Business logic

&nbsp;   │   │   ├── dao/                      # Database access

&nbsp;   │   │   ├── models/                   # Data entities

&nbsp;   │   │   ├── utils/                    # Reusable helpers

&nbsp;   │   │   └── config/                   # DB / dependency setup

&nbsp;   │   └── module-info.java

&nbsp;   ├── resources/

&nbsp;   │   ├── fxml/

&nbsp;   │   ├── css/

&nbsp;   │   └── images/

&nbsp;   └── pom.xml

```



---



\## Integration Flow



```

User Interaction (FXML)

&nbsp;       ↓

FXML Controller

&nbsp;       ↓

Service Layer

&nbsp;       ↓

DAO Layer (Database)

```



\*\*Rules:\*\*



\* Controllers: UI coordination only

\* Services: Core logic and data orchestration

\* DAOs: Pure data access



---



\## Styling Guidelines



\* \*\*Global Stylesheet\*\*: `/resources/css/theme.css`

\* \*\*CSS Variables\*\* for consistency:



&nbsp; ```css

&nbsp; :root {

&nbsp;     -fx-primary-color: #1a73e8;

&nbsp;     -fx-accent-color: #0d47a1;

&nbsp;     -fx-font-family: "Segoe UI";

&nbsp; }

&nbsp; ```

\* \*\*Component Classes\*\*: Use `.btn-primary`, `.table-card`, `.text-title`, etc.



---



\## Design Patterns



| Pattern                  | Purpose                                         |

| ------------------------ | ----------------------------------------------- |

| \*\*MVC (Revised)\*\*        | JavaFX Controller = View+Controller layer       |

| \*\*DAO\*\*                  | Isolate persistence logic                       |

| \*\*Service Layer\*\*        | Coordinate multiple DAOs, handle business rules |

| \*\*Dependency Injection\*\* | Clean initialization and modular testing        |

| \*\*Observer Pattern\*\*     | For dynamic UI updates (ObservableLists)        |



---



\## Example Workflow



\*\*Creating an Incident Report\*\*



```

IncidentReportViewController.submitReport()

&nbsp;   ↓

IncidentService.createIncident()

&nbsp;   ↓

IncidentDAO.insert()

&nbsp;   ↓

PerpetratorService.autoEscalateThreatLevel()

&nbsp;   ↓

PerpetratorDAO.updateThreatLevel()

```



\*\*Result:\*\*

Modular, testable, and clean execution path separating GUI, business logic, and data access.



---



\## Future-Proofing



\* \*\*Dependency Injection Framework\*\*: Guice or Spring (optional).

\* \*\*ORM Migration\*\*: Hibernate for scalable persistence.

\* \*\*FXML Modularization\*\*: Separate screens into modules for large-scale apps.

\* \*\*REST Integration (Phase 2)\*\*: Add API gateway for remote reporting.



---



\## Summary



✅ Transition from Console → \*\*JavaFX FXML + Scene Builder\*\*

✅ Adopt \*\*Controller → Service → DAO\*\* architecture

✅ Apply \*\*CSS-based UI styling\*\*

✅ Maintain \*\*loose coupling\*\* and \*\*modularity\*\*

✅ Improve \*\*scalability\*\*, \*\*maintainability\*\*, and \*\*UI quality\*\*

