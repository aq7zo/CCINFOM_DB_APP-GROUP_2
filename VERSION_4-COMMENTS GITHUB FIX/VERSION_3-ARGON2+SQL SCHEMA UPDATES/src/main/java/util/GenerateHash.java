package util;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * Simple one-off utility to generate a proper Argon2 hash for the admin password
 * and save it to a text file (admin_hash_output.txt) in the project root.
 *
 * This was used during development to quickly get a valid password4j hash
 * that could be copied into SQL insert scripts (PhishNet-inserts.sql).
 * 
 * After running, just copy the generated hash from the text file (or console)
 * and paste it into your SQL INSERT statements.
 */
public class GenerateHash {
    
    public static void main(String[] args) {
        // The actual admin password we want to support
        String password = "PhishNetAdmin124";
        
        System.out.println("Generating Argon2 hash for password: " + password);
        System.out.println();
        
        // Generate the hash using our centralized SecurityUtils (which uses password4j)
        String hash = SecurityUtils.hashPassword(password);
        
        if (hash != null) {
            // Try to write the hash to a file in the project root for easy copy-paste
            try {
                String projectRoot = System.getProperty("user.dir");
                String filePath = Paths.get(projectRoot, "admin_hash_output.txt").toString();
                
                FileWriter writer = new FileWriter(filePath);
                writer.write(hash);
                writer.close();
                
                System.out.println("Hash generated and saved successfully!");
                System.out.println("File location: " + filePath);
                System.out.println();
                System.out.println("Generated hash:");
                System.out.println(hash);
                System.out.println();
                System.out.println("You can now copy this hash into your SQL insert script.");
                
            } catch (IOException e) {
                // If writing fails (e.g. permission issue), still show the hash in console
                System.err.println("Warning: Could not write to file: " + e.getMessage());
                System.err.println("But don't worry - here's your hash anyway:");
                System.out.println(hash);
            }
        } else {
            System.err.println("Failed to generate hash! Something is wrong with SecurityUtils.");
            System.exit(1);
        }
    }
}