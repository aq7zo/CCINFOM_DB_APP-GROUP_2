-- Migration script to add 'Rejected' status to IncidentReports table
-- Run this script if you're getting "Data truncated for column 'Status'" errors

USE CybersecurityDB;

-- Add 'Rejected' to the Status ENUM
ALTER TABLE `IncidentReports` 
MODIFY COLUMN `Status` ENUM('Pending', 'Validated', 'Rejected') DEFAULT 'Pending';

-- Verify the change
SHOW COLUMNS FROM `IncidentReports` LIKE 'Status';

