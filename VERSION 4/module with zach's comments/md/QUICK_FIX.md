# Quick Fix for Login Issue

## Problem
Password verification is failing because the database has the **old placeholder hash** (69 characters) instead of the **real Argon2 hash** (97 characters).

## Solution

### Option 1: Run the Fix SQL Script (Recommended)

```sql
-- Run this in MySQL:
SOURCE fix_admin_password.sql;
```

Or copy and paste the contents of `fix_admin_password.sql` into MySQL Workbench.

### Option 2: Manual SQL Update

```sql
USE CybersecurityDB;

UPDATE Administrators 
SET PasswordHash = '$argon2id$v=19$m=65536,t=3,p=4$ZWgiDa9T95Bv2X+maeqSEQ$gmrWCU6zCEjJZ6Scgs+tX26VESOa+0MJ6qxOLntc+ys'
WHERE ContactEmail = 'admin@phishnet.com';
```

### Option 3: Re-run Inserts (if you want to start fresh)

```sql
-- Delete existing admins first
DELETE FROM Administrators;

-- Then run PhishNet-inserts.sql again
SOURCE PhishNet-inserts.sql;
```

## Verify the Fix

After running the update, verify:

```sql
SELECT 
    ContactEmail,
    LENGTH(PasswordHash) AS HashLength,
    SUBSTRING(PasswordHash, 1, 30) AS HashPreview
FROM Administrators 
WHERE ContactEmail = 'admin@phishnet.com';
```

Expected result:
- HashLength: **97** (not 69)
- HashPreview: `$argon2id$v=19$m=65536,t=3,p=4$ZWgi...`

## Test Login Again

After updating the hash:
1. Run the diagnostic tool: `mvn compile exec:java -Dexec.mainClass=util.LoginDiagnostic`
2. Try logging in with:
   - Email: `admin@phishnet.com`
   - Password: `PhishNetAdmin124`

## Why This Happened

The database was populated with the old placeholder hash before we generated the real Argon2 hash. The `PhishNet-inserts.sql` file now has the correct hash, but if you ran it before we updated it, you'll have the old placeholder hash in your database.

