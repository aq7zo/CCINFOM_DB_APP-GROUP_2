# Admin Login Troubleshooting Guide

## Issue: "No account found with this email" when logging in as admin

### Quick Checklist

1. **Are you using the Admin Login form?**
   - Make sure you clicked "Admin Login" link, not the regular user login
   - The admin login form should be separate from the victim/user login form

2. **Has the database been populated?**
   - Run `PhishNet-structure.sql` first (creates tables)
   - Then run `PhishNet-inserts.sql` (populates data including admin accounts)

3. **Verify the admin account exists in the database:**
   ```sql
   USE CybersecurityDB;
   SELECT * FROM Administrators WHERE ContactEmail = 'admin@phishnet.com';
   ```

### Admin Credentials

- **Email**: `admin@phishnet.com`
- **Password**: `PhishNetAdmin124`

### Steps to Fix

#### Step 1: Verify Database Setup

Run this SQL query to check if admin accounts exist:
```sql
USE CybersecurityDB;
SELECT AdminID, Name, ContactEmail FROM Administrators;
```

If the query returns no rows, you need to run the insert script:
```sql
-- Run PhishNet-inserts.sql
SOURCE PhishNet-inserts.sql;
-- Or copy and paste the contents into MySQL Workbench
```

#### Step 2: Check Database Connection

Verify your database connection settings in `DatabaseConnection.java`:
- Database: `CybersecurityDB`
- User: `root`
- Password: (check your MySQL password)

#### Step 3: Verify You're Using Admin Login Form

- The error "No account found with this email" from `VictimAuthenticationService` means you're using the **wrong login form**
- Make sure you're on the **Admin Login** page, not the regular user login page
- Look for the "Admin Login" link/hyperlink on the login page

#### Step 4: Check Console Output

When you try to log in, check the console/terminal output. You should see:
- `Admin authentication: No account found with email: admin@phishnet.com` (if account doesn't exist)
- `Admin authentication: Invalid password for email: admin@phishnet.com` (if password is wrong)
- `Admin authentication successful for: System Administrator` (if login succeeds)

### Diagnostic SQL Script

Run `verify_admin.sql` to check:
- If Administrators table exists
- If admin accounts are populated
- If password hash format is correct

```bash
mysql -u root -p CybersecurityDB < verify_admin.sql
```

### Common Issues

1. **Database not populated**: Run `PhishNet-inserts.sql`
2. **Wrong login form**: Use Admin Login, not User Login
3. **Case sensitivity**: Email matching is now case-insensitive (fixed in code)
4. **Whitespace**: Email is automatically trimmed (fixed in code)
5. **Wrong password**: Make sure you're using `PhishNetAdmin124` (not `admin123`)

### Testing the Fix

After running the inserts, test with:
```sql
-- Verify admin exists
SELECT * FROM Administrators WHERE ContactEmail = 'admin@phishnet.com';

-- Check password hash format
SELECT ContactEmail, 
       SUBSTRING(PasswordHash, 1, 20) AS HashPreview,
       CASE WHEN PasswordHash LIKE '$argon2id$%' THEN 'Valid' ELSE 'Invalid' END AS Format
FROM Administrators 
WHERE ContactEmail = 'admin@phishnet.com';
```

The hash should start with `$argon2id$v=19$m=65536,t=3,p=4$...`

