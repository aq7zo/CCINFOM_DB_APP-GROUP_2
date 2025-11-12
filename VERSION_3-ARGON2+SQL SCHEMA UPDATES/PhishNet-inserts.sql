USE CybersecurityDB;

-- ==============================
-- PhishNet Initial Data Inserts
-- Version: 3.0 (Argon2 Integration)
-- ==============================
-- This file contains initial administrator data with Argon2 password hashes.
-- 
-- IMPORTANT: Run PhishNet-structure.sql FIRST before running this file.
-- ==============================

-- ==============================
-- INSERT ADMINISTRATORS
-- Password for all administrators: admin123
-- Password hash: Argon2id hash (generated with parameters: m=65536, t=3, p=4)
-- ==============================
-- 
-- To generate a new Argon2 hash for a password, use SecurityUtils.hashPassword()
-- or run the GenerateHash.java utility included in this project.
-- 
-- Example hash format: $argon2id$v=19$m=65536,t=3,p=4$[salt]$[hash]
-- ==============================

INSERT INTO Administrators (Name, Role, ContactEmail, PasswordHash)
VALUES
('System Administrator', 'System Admin', 'admin@phishnet.com',
 '$argon2id$v=19$m=65536,t=3,p=4$c2FsdHNhbHRzYWx0c2FsdA$hashplaceholder'),

('Zach Benedict Hallare', 'Cybersecurity Staff', 'zach_benedict_hallare@dlsu.edu.ph',
 '$argon2id$v=19$m=65536,t=3,p=4$c2FsdHNhbHRzYWx0c2FsdA$hashplaceholder'),

('Benette Enzo Campo', 'Cybersecurity Staff', 'benette_campo@dlsu.edu.ph',
 '$argon2id$v=19$m=65536,t=3,p=4$c2FsdHNhbHRzYWx0c2FsdA$hashplaceholder'),

('Brent Prose Rebollos', 'Cybersecurity Staff', 'brent_rebollos@dlsu.edu.ph',
 '$argon2id$v=19$m=65536,t=3,p=4$c2FsdHNhbHRzYWx0c2FsdA$hashplaceholder'),

('Georgina Karylle Ravelo', 'Cybersecurity Staff', 'georgina_ravelo@dlsu.edu.ph',
 '$argon2id$v=19$m=65536,t=3,p=4$c2FsdHNhbHRzYWx0c2FsdA$hashplaceholder')
ON DUPLICATE KEY UPDATE Name = Name;

-- ==============================
-- NOTE: The hash values above are placeholders.
-- Before using this file, generate actual Argon2 hashes by:
-- 1. Running the application and using SecurityUtils.hashPassword("admin123")
-- 2. Or running the GenerateHash.java utility
-- 3. Replace "hashplaceholder" with the actual generated hash
-- 
-- Since Argon2 uses random salts, each hash will be unique.
-- For consistency, you may want to generate one hash and use it for all admins,
-- or generate individual hashes for each administrator.
-- ==============================

