CREATE DATABASE CybersecurityDB;
USE CybersecurityDB;

-- ==============================
-- TABLE: Victims
-- ==============================
CREATE TABLE Victims (
    VictimID INT AUTO_INCREMENT PRIMARY KEY,
    Name VARCHAR(100) NOT NULL,
    ContactEmail VARCHAR(100) UNIQUE NOT NULL,
    AccountStatus ENUM('Active', 'Flagged', 'Suspended') DEFAULT 'Active',
    DateCreated DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ==============================
-- TABLE: Perpetrators
-- ==============================
CREATE TABLE Perpetrators (
    PerpetratorID INT AUTO_INCREMENT PRIMARY KEY,
    Identifier VARCHAR(255) UNIQUE NOT NULL,
    IdentifierType ENUM('Phone Number', 'Email Address', 'Social Media Account', 'Website URL', 'IP Address') NOT NULL,
    AssociatedName VARCHAR(100),
    ThreatLevel ENUM('UnderReview', 'Suspected', 'Malicious', 'Cleared') DEFAULT 'UnderReview',
    LastIncidentDate DATETIME
);

-- ==============================
-- TABLE: AttackTypes
-- ==============================
CREATE TABLE AttackTypes (
    AttackTypeID INT AUTO_INCREMENT PRIMARY KEY,
    AttackName VARCHAR(100) NOT NULL,
    Description TEXT,
    SeverityLevel ENUM('Low', 'Medium', 'High') DEFAULT 'Low'
);

-- ==============================
-- TABLE: Administrators
-- ==============================
CREATE TABLE Administrators (
    AdminID INT AUTO_INCREMENT PRIMARY KEY,
    Name VARCHAR(100) NOT NULL,
    Role ENUM('System Admin', 'Cybersecurity Staff') NOT NULL,
    ContactEmail VARCHAR(100) UNIQUE NOT NULL,
    DateAssigned DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ==============================
-- TRANSACTION TABLE: IncidentReports
-- ==============================
CREATE TABLE IncidentReports (
    IncidentID INT AUTO_INCREMENT PRIMARY KEY,
    VictimID INT NOT NULL,
    PerpetratorID INT NOT NULL,
    AttackTypeID INT NOT NULL,
    AdminID INT,
    DateReported DATETIME DEFAULT CURRENT_TIMESTAMP,
    Description TEXT,
    Status ENUM('Pending', 'Validated') DEFAULT 'Pending',
    FOREIGN KEY (VictimID) REFERENCES Victims(VictimID) ON DELETE CASCADE,
    FOREIGN KEY (PerpetratorID) REFERENCES Perpetrators(PerpetratorID) ON DELETE CASCADE,
    FOREIGN KEY (AttackTypeID) REFERENCES AttackTypes(AttackTypeID),
    FOREIGN KEY (AdminID) REFERENCES Administrators(AdminID)
);

-- ==============================
-- TRANSACTION TABLE: EvidenceUpload
-- ==============================
CREATE TABLE EvidenceUpload (
    EvidenceID INT AUTO_INCREMENT PRIMARY KEY,
    IncidentID INT NOT NULL,
    EvidenceType ENUM('Screenshot', 'Email', 'File', 'Chat Log') NOT NULL,
    FilePath VARCHAR(255) NOT NULL,
    SubmissionDate DATETIME DEFAULT CURRENT_TIMESTAMP,
    VerifiedStatus ENUM('Pending', 'Verified', 'Rejected') DEFAULT 'Pending',
    AdminID INT,
    FOREIGN KEY (IncidentID) REFERENCES IncidentReports(IncidentID) ON DELETE CASCADE,
    FOREIGN KEY (AdminID) REFERENCES Administrators(AdminID)
);

-- ==============================
-- TRANSACTION TABLE: ThreatLevelLog
-- ==============================
CREATE TABLE ThreatLevelLog (
    LogID INT AUTO_INCREMENT PRIMARY KEY,
    PerpetratorID INT NOT NULL,
    OldThreatLevel ENUM('UnderReview', 'Suspected', 'Malicious', 'Cleared'),
    NewThreatLevel ENUM('UnderReview', 'Suspected', 'Malicious', 'Cleared'),
    ChangeDate DATETIME DEFAULT CURRENT_TIMESTAMP,
    AdminID INT,
    FOREIGN KEY (PerpetratorID) REFERENCES Perpetrators(PerpetratorID) ON DELETE CASCADE,
    FOREIGN KEY (AdminID) REFERENCES Administrators(AdminID)
);

-- ==============================
-- TRANSACTION TABLE: VictimStatusLog
-- ==============================
CREATE TABLE VictimStatusLog (
    LogID INT AUTO_INCREMENT PRIMARY KEY,
    VictimID INT NOT NULL,
    OldStatus ENUM('Active', 'Flagged', 'Suspended'),
    NewStatus ENUM('Active', 'Flagged', 'Suspended'),
    ChangeDate DATETIME DEFAULT CURRENT_TIMESTAMP,
    AdminID INT,
    FOREIGN KEY (VictimID) REFERENCES Victims(VictimID) ON DELETE CASCADE,
    FOREIGN KEY (AdminID) REFERENCES Administrators(AdminID)
);