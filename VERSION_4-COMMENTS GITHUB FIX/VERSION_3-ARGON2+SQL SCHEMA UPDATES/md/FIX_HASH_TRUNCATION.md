# Fix for Argon2 Hash Truncation Error

## Problem
The database contains a truncated/placeholder hash (`[new_hash_here]`) instead of a proper Argon2 hash. The hash length is only 46 characters when it should be around 97 characters.

## Root Cause
The `verify_admin.sql` file contains a placeholder hash that was executed, overwriting the correct hash from `PhishNet-inserts.sql`.

## Solution

### Step 1: Update the Database Hash
Run the SQL script to fix the admin password hash:

```sql
USE CybersecurityDB;

UPDATE Administrators 
SET PasswordHash = '$argon2id$v=19$m=65536,t=3,p=4$ZWgiDa9T95Bv2X+maeqSEQ$gmrWCU6zCEjJZ6Scgs+tX26VESOa+0MJ6qxOLntc+ys'
WHERE ContactEmail = 'admin@phishnet.com';
```

Or run the provided script:
```bash
mysql -u your_username -p CybersecurityDB < fix_admin_password_hash.sql
```

### Step 2: Verify the Hash
After updating, verify the hash length:

```sql
SELECT 
    ContactEmail,
    LENGTH(PasswordHash) AS HashLength,
    SUBSTRING(PasswordHash, 1, 50) AS HashPreview
FROM Administrators 
WHERE ContactEmail = 'admin@phishnet.com';
```

Expected:
- HashLength: ~97 characters
- HashPreview: `$argon2id$v=19$m=65536,t=3,p=4$ZWgiDa9T95Bv2X+maeqSEQ$...`

### Step 3: Test Login
Try logging in with:
- Email: `admin@phishnet.com`
- Password: `PhishNetAdmin124`

## Code Changes Made

1. **SecurityUtils.java** - Updated verification method:
   - Changed from `.with(ARGON2_FUNCTION)` to `.withArgon2()` for verification
   - Added fallback to `.with(ARGON2_FUNCTION)` if `.withArgon2()` fails
   - Added hash trimming to handle whitespace issues
   - Enhanced error diagnostics

2. **fix_admin_password_hash.sql** - Created SQL script to fix the database hash

## Notes

- The hash in `PhishNet-inserts.sql` is correct and should work after the database update
- If you need to regenerate hashes, use `util.GenerateAndUpdateHash` utility
- Always verify hash length is ~97 characters for Argon2 hashes

