USE CybersecurityDB;

-- ==============================
-- Fix All Admin Password Hashes
-- ==============================
-- This script updates all administrator password hashes to use
-- a hash that works with the current SecurityUtils verification code.
-- Password for all administrators: PhishNetAdmin124
-- ==============================

-- First, check current hash lengths to see if they're truncated
SELECT 
    ContactEmail,
    Name,
    LENGTH(PasswordHash) AS CurrentHashLength,
    SUBSTRING(PasswordHash, 1, 50) AS HashPreview
FROM Administrators
ORDER BY ContactEmail;

-- ==============================
-- Update all admin hashes (fixing truncated ones)
-- Password for all: PhishNetAdmin124
-- ==============================

-- Option 1: Use the hash from PhishNet-inserts.sql (97 chars - same as admin@phishnet.com)
UPDATE Administrators 
SET PasswordHash = '$argon2id$v=19$m=65536,t=3,p=4$ZWgiDa9T95Bv2X+maeqSEQ$gmrWCU6zCEjJZ6Scgs+tX26VESOa+0MJ6qxOLntc+ys'
WHERE ContactEmail IN (
    'zach_benedict_hallare@dlsu.edu.ph',
    'benette_campo@dlsu.edu.ph',
    'brent_rebollos@dlsu.edu.ph',
    'georgina_ravelo@dlsu.edu.ph'
);

-- Note: admin@phishnet.com already has the correct hash (97 chars), so we skip it above
-- If you want to update ALL admins including admin@phishnet.com, uncomment below:
-- UPDATE Administrators 
-- SET PasswordHash = '$argon2id$v=19$m=65536,t=3,p=4$ZWgiDa9T95Bv2X+maeqSEQ$gmrWCU6zCEjJZ6Scgs+tX26VESOa+0MJ6qxOLntc+ys'
-- WHERE ContactEmail IN (
--     'admin@phishnet.com',
--     'zach_benedict_hallare@dlsu.edu.ph',
--     'benette_campo@dlsu.edu.ph',
--     'brent_rebollos@dlsu.edu.ph',
--     'georgina_ravelo@dlsu.edu.ph'
-- );

-- ==============================
-- Option 2: Generate a NEW hash and use it (if Option 1 doesn't work)
-- Run: mvn compile exec:java -Dexec.mainClass="util.SecurityUtilsTest" -Dexec.args="--generate-admin-hash"
-- Then replace the hash below with the newly generated one
-- ==============================
-- UPDATE Administrators 
-- SET PasswordHash = '<PASTE_NEWLY_GENERATED_HASH_HERE>'
-- WHERE ContactEmail IN (
--     'zach_benedict_hallare@dlsu.edu.ph',
--     'benette_campo@dlsu.edu.ph',
--     'brent_rebollos@dlsu.edu.ph',
--     'georgina_ravelo@dlsu.edu.ph'
-- );

-- After updating, verify the hashes:
SELECT 
    ContactEmail,
    Name,
    LENGTH(PasswordHash) AS NewHashLength,
    CASE 
        WHEN LENGTH(PasswordHash) < 90 THEN '⚠️ Hash may be truncated!'
        WHEN LENGTH(PasswordHash) >= 90 AND LENGTH(PasswordHash) <= 200 THEN '✓ Hash length looks good'
        ELSE '⚠️ Hash length unusual'
    END AS HashStatus
FROM Administrators
ORDER BY ContactEmail;

