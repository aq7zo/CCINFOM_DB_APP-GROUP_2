# Generating Argon2 Hashes for PhishNet-inserts.sql

This document explains how to generate Argon2 password hashes for the administrator accounts in `PhishNet-inserts.sql`.

## Method 1: Using SecurityUtils (Recommended)

Create a simple Java program to generate the hash:

```java
import com.group2.dbapp.util.SecurityUtils;

public class GenerateAdminHash {
    public static void main(String[] args) {
        String password = "admin123";
        String hash = SecurityUtils.hashPassword(password);
        System.out.println("Password: " + password);
        System.out.println("Argon2 Hash: " + hash);
    }
}
```

Compile and run:
```bash
mvn compile exec:java -Dexec.mainClass="GenerateAdminHash"
```

## Method 2: Using Password4j Directly

```java
import com.password4j.Argon2Function;
import com.password4j.Password;

public class GenerateHash {
    public static void main(String[] args) {
        Argon2Function argon2 = Argon2Function.getInstance(
            65536,      // Memory cost (64 MB in KB)
            3,          // Iterations
            4,          // Parallelism
            32,         // Hash length
            Argon2Function.Argon2Types.ARGON2id
        );
        
        String password = "admin123";
        String hash = Password.hash(password).with(argon2).getResult();
        System.out.println("Hash: " + hash);
    }
}
```

## Updating PhishNet-inserts.sql

After generating the hash, replace the placeholder values in `PhishNet-inserts.sql`:

1. Generate a hash for "admin123"
2. Replace all instances of `hashplaceholder` with the generated hash
3. Note: Since Argon2 uses random salts, each hash will be unique. You can either:
   - Generate one hash and use it for all administrators (simpler)
   - Generate individual hashes for each administrator (more secure)

## Example Hash Format

The generated hash will look like:
```
$argon2id$v=19$m=65536,t=3,p=4$[base64_salt]$[base64_hash]
```

Example:
```
$argon2id$v=19$m=65536,t=3,p=4$c2FsdHNhbHRzYWx0c2FsdA$YWJjZGVmZ2hpams=
```

## Important Notes

- The hash includes all necessary parameters (salt, iterations, memory, parallelism)
- Each hash is unique due to random salt generation
- The hash format is compatible with `SecurityUtils.verifyPassword()`
- Maximum password length is 128 characters (enforced by SecurityUtils)

