USE CybersecurityDB;

-- Check all admin password hash lengths and formats
SELECT 
    AdminID,
    Name,
    ContactEmail,
    LENGTH(PasswordHash) AS HashLength,
    SUBSTRING(PasswordHash, 1, 30) AS HashStart,
    CASE 
        WHEN PasswordHash LIKE '$argon2id$%' THEN '✓ Argon2id format'
        WHEN PasswordHash LIKE '$argon2i$%' THEN '✓ Argon2i format'
        WHEN PasswordHash LIKE '$argon2d$%' THEN '✓ Argon2d format'
        WHEN LENGTH(PasswordHash) < 50 THEN '⚠️ Hash too short (likely truncated)'
        ELSE '⚠️ Unknown format'
    END AS HashStatus,
    CASE 
        WHEN LENGTH(PasswordHash) < 90 THEN '⚠️ TRUNCATED - Needs update'
        WHEN LENGTH(PasswordHash) >= 90 AND LENGTH(PasswordHash) <= 200 THEN '✓ Length OK'
        ELSE '⚠️ Unusual length'
    END AS LengthStatus
FROM Administrators
ORDER BY ContactEmail;

-- Expected: HashLength should be around 97-161 characters
-- Expected: HashStart should be: $argon2id$v=19$m=65536,t=3,p=4$

