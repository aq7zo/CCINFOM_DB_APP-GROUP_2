# Generate Working Password Hash

## The Problem

The hash in your database was generated with Python's argon2 library, which is **not compatible** with password4j (Java). Even though both are Argon2, they encode hashes slightly differently.

## Solution: Generate Hash Using password4j

### Step 1: Run This Command

```bash
mvn compile exec:java -Dexec.mainClass=util.SecurityUtilsTest -Dexec.args=--generate-admin-hash
```

### Step 2: Copy the Hash

The output will show:
- The hash (long string starting with `$argon2id$...`)
- Verification status (should be ✓ SUCCESS)
- SQL UPDATE command

### Step 3: Run the SQL

Copy the SQL UPDATE command from the output and run it in MySQL:

```sql
USE CybersecurityDB;

UPDATE Administrators
SET PasswordHash = '[PASTE_HASH_HERE]'
WHERE ContactEmail = 'admin@phishnet.com';
```

### Step 4: Test Login

After running the SQL:
1. Run diagnostic: `mvn compile exec:java -Dexec.mainClass=util.LoginDiagnostic`
2. Try logging in with:
   - Email: `admin@phishnet.com`
   - Password: `PhishNetAdmin124`

## Why This Works

The hash is generated using **exactly the same library and parameters** your app uses:
- Library: password4j v1.7.3
- Parameters: m=65536, t=3, p=4, Argon2id
- Salt length: 16 bytes
- Hash length: 32 bytes

This ensures 100% compatibility.

## Alternative Method

If the command doesn't work, you can also use `GetWorkingHash.java`:

```bash
mvn compile exec:java -Dexec.mainClass=util.GetWorkingHash
```

Both tools do the same thing - generate a password4j-compatible hash.

