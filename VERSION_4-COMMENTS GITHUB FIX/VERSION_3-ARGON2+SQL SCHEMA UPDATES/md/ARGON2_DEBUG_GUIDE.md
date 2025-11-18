# Argon2 Debugging Guide

## Current Implementation Status

✅ **Compilation**: SUCCESS - Code compiles without errors
⚠️ **Runtime**: Needs testing

## Testing the Implementation

### Option 1: Run the Test Utility

```bash
cd "VERSION_3-ARGON2+SQL SCHEMA UPDATES"
mvn compile exec:java -Dexec.mainClass="com.group2.dbapp.util.SecurityUtilsTest"
```

### Option 2: Test in Your Application

1. **Test Password Hashing**:
   ```java
   String hash = SecurityUtils.hashPassword("admin123");
   System.out.println("Hash: " + hash);
   ```

2. **Test Password Verification**:
   ```java
   boolean verified = SecurityUtils.verifyPassword("admin123", hash);
   System.out.println("Verified: " + verified);
   ```

## Common Issues and Solutions

### Issue 1: `Argon2.ID` Not Found

**Symptom**: Compilation error about `Argon2.ID` not being found

**Solution**: The import path might be different in Password4j 1.7.3. Try:
- Check if it's `com.password4j.types.Argon2` or `com.password4j.Argon2`
- Or use the simpler API without specifying the variant

### Issue 2: Runtime Exception

**Symptom**: `NoClassDefFoundError` or `ClassNotFoundException`

**Solution**: 
- Ensure Password4j dependency is properly downloaded: `mvn dependency:resolve`
- Check if all transitive dependencies are available

### Issue 3: Hash Generation Returns Null

**Symptom**: `hashPassword()` returns null

**Possible Causes**:
- Exception during hashing (check console for error messages)
- Password length exceeds 128 characters
- Password is null or empty

**Debug Steps**:
1. Check console output for error messages
2. Verify password length
3. Add try-catch to see exception details

### Issue 4: Verification Always Returns False

**Symptom**: `verifyPassword()` always returns false even with correct password

**Possible Causes**:
- Hash format mismatch
- Parameters mismatch between hashing and verification
- Legacy hash detection not working

**Debug Steps**:
1. Print the hash string to verify format
2. Check if hash starts with `$argon2id$`
3. Verify parameters match

## Debugging Code

Add this to your code to debug:

```java
public static void debugArgon2() {
    String password = "test123";
    
    System.out.println("=== Argon2 Debug Info ===");
    System.out.println("Password: " + password);
    
    // Test hashing
    String hash = SecurityUtils.hashPassword(password);
    System.out.println("Hash: " + hash);
    System.out.println("Hash is null: " + (hash == null));
    
    if (hash != null) {
        System.out.println("Hash starts with $argon2id$: " + hash.startsWith("$argon2id$"));
        System.out.println("Hash length: " + hash.length());
    }
    
    // Test verification
    if (hash != null) {
        boolean verified = SecurityUtils.verifyPassword(password, hash);
        System.out.println("Verification result: " + verified);
        
        // Test wrong password
        boolean wrongVerified = SecurityUtils.verifyPassword("wrong", hash);
        System.out.println("Wrong password verification: " + wrongVerified);
    }
}
```

## Version Compatibility

- **Password4j Version**: 1.7.3
- **Java Version**: 17+
- **Argon2 Variant**: Argon2id (default)

## Parameters Used

- Memory Cost: 65536 KB (64 MB)
- Iterations: 3
- Parallelism: 4
- Hash Length: 32 bytes
- Salt Length: 16 bytes (auto-generated)

## Next Steps

1. Run the test utility to verify basic functionality
2. Test in your application context
3. Check console output for any error messages
4. Verify hash format matches expected Argon2 format
5. Test with actual database operations

## Getting Help

If issues persist:
1. Check Password4j documentation: https://password4j.com/
2. Verify dependency version compatibility
3. Check Java version compatibility
4. Review error messages and stack traces

