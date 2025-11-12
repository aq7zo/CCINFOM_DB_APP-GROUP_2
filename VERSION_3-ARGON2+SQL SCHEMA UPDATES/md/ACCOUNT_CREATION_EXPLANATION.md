# User Account Creation - Current Setup Explanation

## Overview

The application has **TWO separate account types** with different creation mechanisms:

1. **Victim/User Accounts** - Created dynamically through the application UI
2. **Administrator Accounts** - Pre-populated via `PhishNet-inserts.sql`

---

## 1. Victim/User Account Creation (Dynamic)

### Flow Diagram

```
User fills SignUp form
    ↓
SignUpController.handleSignUp()
    ↓
VictimAuthenticationService.register()
    ↓
[Validation] → [Check duplicate] → [Hash password] → [Create Victim object]
    ↓
VictimDAOImpl.create()
    ↓
INSERT INTO Victims (Name, ContactEmail, PasswordHash, AccountStatus)
    ↓
Database
```

### Current Implementation

**Location**: `controller/SignUpController.java` → `service/VictimAuthenticationService.java` → `dao/VictimDAOImpl.java`

**Process**:
1. **UI Layer** (`SignUpController`):
   - Collects: Name, Email, Password, Confirm Password
   - Validates: All fields filled, passwords match
   - Calls: `authService.register(name, email, password)`

2. **Service Layer** (`VictimAuthenticationService.register()`):
   - Validates name (not empty)
   - Validates email format (regex)
   - Validates password strength (min 6 characters)
   - Checks for duplicate email
   - **Hashes password with Argon2**: `SecurityUtils.hashPassword(password)`
   - Creates `Victim` object
   - Sets `AccountStatus = "Active"`
   - Calls: `victimDAO.create(newVictim)`

3. **DAO Layer** (`VictimDAOImpl.create()`):
   - Executes SQL: `INSERT INTO Victims (Name, ContactEmail, PasswordHash, AccountStatus)`
   - Uses `PreparedStatement` (SQL injection safe)
   - Retrieves auto-generated `VictimID`
   - Returns `true` on success

### ✅ What's Working

- ✅ Complete registration flow
- ✅ Input validation (UI + Service layers)
- ✅ Duplicate email checking
- ✅ Argon2 password hashing
- ✅ Database insertion with auto-generated ID
- ✅ Error handling at each layer
- ✅ User feedback via alerts

### ⚠️ What's Missing / Leftover Logic

1. **Email Verification** (Not Implemented)
   - No email confirmation step
   - Accounts are immediately active
   - **Recommendation**: Add email verification workflow

2. **Password Strength Requirements** (Basic)
   - Currently only checks minimum 6 characters
   - No complexity requirements (uppercase, numbers, special chars)
   - **Recommendation**: Enhance `ValidationUtils.isValidPassword()`

3. **Account Status Management** (Basic)
   - All accounts default to "Active"
   - No workflow for "Flagged" or "Suspended" statuses
   - **Recommendation**: Add admin tools to manage account status

4. **DateCreated Field** (Auto-set by DB)
   - Database sets `DateCreated` via `DEFAULT CURRENT_TIMESTAMP`
   - Not explicitly set in code (relies on DB default)
   - **Status**: ✅ Working correctly (no action needed)

5. **Error Messages to User** (Could be Better)
   - Currently shows generic "Registration failed" message
   - Doesn't distinguish between different failure reasons
   - **Recommendation**: Return specific error codes/messages

---

## 2. Administrator Account Creation (Static)

### Flow

```
Database Setup
    ↓
Run PhishNet-structure.sql (creates schema)
    ↓
Run PhishNet-inserts.sql (populates admins)
    ↓
Administrators table populated with 5 admin accounts
```

### Current Implementation

**Location**: `PhishNet-inserts.sql`

**Process**:
- **Static SQL file** with `INSERT` statements
- Contains **5 administrator accounts**:
  1. System Administrator
  2. Zach Benedict Hallare
  3. Benette Enzo Campo
  4. Brent Prose Rebollos
  5. Georgina Karylle Ravelo

**Password**: All use `admin123` (password needs to be hashed)

### ⚠️ Current Issue: Placeholder Hashes

**Problem**: `PhishNet-inserts.sql` contains **placeholder Argon2 hashes**:
```sql
'$argon2id$v=19$m=65536,t=3,p=4$c2FsdHNhbHRzYWx0c2FsdA$hashplaceholder'
```

**Status**: ❌ **NOT READY TO USE** - Hashes need to be generated

**Solution**: Generate real Argon2 hashes and replace placeholders

---

## Relationship: inserts.sql vs User Registration

### ❌ **NO RELATIONSHIP**

- **`PhishNet-inserts.sql`**: Only for **administrator accounts** (pre-populated)
- **User Registration**: Creates **victim/user accounts** dynamically (not in inserts.sql)

### Two Separate Systems

| Aspect | Victim/User Accounts | Administrator Accounts |
|--------|---------------------|----------------------|
| **Creation Method** | Dynamic (via UI) | Static (via SQL file) |
| **File Used** | Application code | `PhishNet-inserts.sql` |
| **Password Hashing** | Argon2 (runtime) | Argon2 (needs generation) |
| **Table** | `Victims` | `Administrators` |
| **Who Creates** | End users | Database admin |

---

## Missing Implementation Checklist

### High Priority

- [ ] **Generate real Argon2 hashes** for `PhishNet-inserts.sql`
  - Use `SecurityUtils.hashPassword("admin123")` 
  - Replace placeholder hashes
  - Test admin login after update

### Medium Priority

- [ ] **Enhanced Password Validation**
  ```java
  // Current: Only checks length >= 6
  // Recommended: Add complexity requirements
  - Minimum 8 characters
  - At least one uppercase letter
  - At least one number
  - At least one special character
  ```

- [ ] **Better Error Messages**
  - Return specific error codes from service layer
  - Show user-friendly messages in UI
  - Distinguish: duplicate email, invalid format, weak password, etc.

### Low Priority (Future Enhancements)

- [ ] **Email Verification**
  - Send verification email on registration
  - Set account status to "Pending" until verified
  - Add verification token/expiry

- [ ] **Account Status Management**
  - Admin interface to flag/suspend accounts
  - Automatic flagging for suspicious activity
  - Status change logging

- [ ] **Password Reset Functionality**
  - "Forgot Password" link
  - Email-based password reset
  - Secure token generation

- [ ] **Registration Rate Limiting**
  - Prevent spam registrations
  - Limit registrations per IP/email
  - CAPTCHA integration

---

## Current Code Flow (Detailed)

### Registration Flow

```java
// 1. USER INPUT (SignUpController.java)
handleSignUp() {
    - Get: name, email, password, confirmPassword
    - Validate: fields not empty, passwords match
    - Call: authService.register(name, email, password)
}

// 2. BUSINESS LOGIC (VictimAuthenticationService.java)
register(name, email, password) {
    - Validate name (not empty)
    - Validate email (regex pattern)
    - Validate password (min 6 chars)
    - Check duplicate: victimDAO.findByEmail(email)
    - Hash password: SecurityUtils.hashPassword(password) → Argon2 hash
    - Create Victim object
    - Set AccountStatus = "Active"
    - Insert: victimDAO.create(newVictim)
}

// 3. DATABASE OPERATION (VictimDAOImpl.java)
create(Victim victim) {
    - SQL: INSERT INTO Victims (Name, ContactEmail, PasswordHash, AccountStatus)
    - Parameters: name, email, passwordHash, "Active"
    - Get generated VictimID
    - Return: true/false
}
```

### Login Flow

```java
// 1. USER INPUT (LoginController.java)
handleLogin() {
    - Get: email, password
    - Call: authService.login(email, password)
}

// 2. AUTHENTICATION (VictimAuthenticationService.java)
login(email, password) {
    - Validate email format
    - Validate password not empty
    - Find: victimDAO.findByEmail(email)
    - Verify: SecurityUtils.verifyPassword(password, victim.getPasswordHash())
    - Set: currentVictim = victim
    - Return: Victim object or null
}
```

---

## Summary

### ✅ **What Works**

1. **User Registration**: Fully functional, creates accounts dynamically
2. **Password Security**: Uses Argon2id hashing
3. **Validation**: Multi-layer validation (UI + Service)
4. **Database**: Proper INSERT with auto-generated IDs
5. **Error Handling**: Try-catch blocks at each layer

### ⚠️ **What Needs Attention**

1. **Admin Hashes**: `PhishNet-inserts.sql` has placeholder hashes - **needs real hashes**
2. **Password Strength**: Basic validation (only length check)
3. **Error Messages**: Generic messages, could be more specific
4. **Email Verification**: Not implemented (accounts immediately active)

### 📝 **Key Point**

**`PhishNet-inserts.sql` is ONLY for administrator accounts**, not for regular user accounts. User accounts are created entirely through the application's registration flow and do NOT use the inserts.sql file.

---

## Next Steps

1. **Immediate**: Generate real Argon2 hashes for admin accounts in `PhishNet-inserts.sql`
2. **Short-term**: Enhance password validation rules
3. **Medium-term**: Improve error messaging
4. **Long-term**: Add email verification and account management features

