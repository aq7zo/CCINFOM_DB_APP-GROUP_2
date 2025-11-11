CREATE DATABASE IF NOT EXISTS CybersecurityDB;
USE CybersecurityDB;

-- ==============================
-- TABLE: Victims
-- ==============================
CREATE TABLE IF NOT EXISTS Victims (
    VictimID INT AUTO_INCREMENT PRIMARY KEY,
    Name VARCHAR(100) NOT NULL,
    ContactEmail VARCHAR(100) UNIQUE NOT NULL,
    AccountStatus ENUM('Active', 'Flagged', 'Suspended') DEFAULT 'Active',
    DateCreated DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ==============================
-- TABLE: Perpetrators
-- ==============================
CREATE TABLE IF NOT EXISTS Perpetrators (
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
CREATE TABLE IF NOT EXISTS AttackTypes (
    AttackTypeID INT AUTO_INCREMENT PRIMARY KEY,
    AttackName VARCHAR(100) NOT NULL,
    Description TEXT,
    SeverityLevel ENUM('Low', 'Medium', 'High') DEFAULT 'Low'
);

-- ==============================
-- TABLE: Administrators (UPDATED WITH PASSWORD FIELD)
-- ==============================
CREATE TABLE IF NOT EXISTS Administrators (
    AdminID INT AUTO_INCREMENT PRIMARY KEY,
    Name VARCHAR(100) NOT NULL,
    Role ENUM('System Admin', 'Cybersecurity Staff') NOT NULL,
    ContactEmail VARCHAR(100) UNIQUE NOT NULL,
    PasswordHash VARCHAR(255) NOT NULL,
    DateAssigned DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ==============================
-- TABLE: IncidentReports
-- ==============================
CREATE TABLE IF NOT EXISTS IncidentReports (
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
-- TABLE: EvidenceUpload
-- ==============================
CREATE TABLE IF NOT EXISTS EvidenceUpload (
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
-- TABLE: ThreatLevelLog
-- ==============================
CREATE TABLE IF NOT EXISTS ThreatLevelLog (
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
-- TABLE: VictimStatusLog
-- ==============================
CREATE TABLE IF NOT EXISTS VictimStatusLog (
    LogID INT AUTO_INCREMENT PRIMARY KEY,
    VictimID INT NOT NULL,
    OldStatus ENUM('Active', 'Flagged', 'Suspended'),
    NewStatus ENUM('Active', 'Flagged', 'Suspended'),
    ChangeDate DATETIME DEFAULT CURRENT_TIMESTAMP,
    AdminID INT,
    FOREIGN KEY (VictimID) REFERENCES Victims(VictimID) ON DELETE CASCADE,
    FOREIGN KEY (AdminID) REFERENCES Administrators(AdminID)
);

-- ==============================
-- INSERT SAMPLE ADMINISTRATOR
-- Password: admin123 (hashed using SHA-256)
-- ==============================
INSERT INTO Administrators (Name, Role, ContactEmail, PasswordHash) 
VALUES ('System Administrator', 'System Admin', 'admin@phishnet.com', 
'240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9')
ON DUPLICATE KEY UPDATE Name = Name;

