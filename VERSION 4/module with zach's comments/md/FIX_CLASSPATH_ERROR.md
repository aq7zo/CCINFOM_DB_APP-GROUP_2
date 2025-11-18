# Fix: NoClassDefFoundError for SecurityUtils

## The Problem

Error message:
```
Caused by: java.lang.NoClassDefFoundError: com/group2/dbapp/util/SecurityUtils
	at com.group2.dbapp.service.AdminAuthenticationService.authenticate(AdminAuthenticationService.java:31)
```

## Root Cause

The error shows it's looking for `com.group2.dbapp.util.SecurityUtils`, but:
- The actual class is in package `util` (not `com.group2.dbapp.util`)
- This means there are **old compiled .class files** in the `target/` directory with the wrong package structure

## Solution

### Step 1: Clean Build

Run this command to remove all old compiled classes:

```bash
mvn clean compile
```

Or manually delete the `target/` directory:
```bash
rm -rf target/
# or on Windows:
rmdir /s /q target
```

### Step 2: Rebuild

After cleaning, rebuild:

```bash
mvn compile
```

### Step 3: Run Application

After rebuilding, run the application again:

```bash
mvn javafx:run
# or
mvn exec:java -Dexec.mainClass=Main
```

## Why This Happens

When you refactor packages (change from `com.group2.dbapp.util` to `util`), the old `.class` files remain in `target/classes/`. Java finds the old classes first and tries to use them, causing the `NoClassDefFoundError`.

## Prevention

Always run `mvn clean` before building when:
- Changing package names
- Moving classes between packages
- After major refactoring

## Verify Fix

After cleaning and rebuilding, check that:
1. No errors during compilation
2. Application runs without `NoClassDefFoundError`
3. Admin login works correctly

