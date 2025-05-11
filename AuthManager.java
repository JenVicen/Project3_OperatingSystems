/*
By Jennifer Vicentes
Purpose: This class manages the authentication of users in the game. 
It allows for the creation, deletion, and validation of user accounts.
It uses SHA3-256 hashing for password storage and requires a console for user input.
It also handles the loading and saving of user data to a file.

Acclaimed AI-generated method: private static String hash(String input) 
All the other comments I wrote were put for me to keep track while developing, they are not AI generated.
*/

import java.nio.file.*;
import java.security.*;
import java.util.*;
import java.io.*;
import java.util.stream.*;

public class AuthManager {
    // Path to the users file
    private final Path usersFile;
    // Map to store usernames and their hashed passwords
    private final Map<String, String> users = new LinkedHashMap<>();

    public AuthManager(Path gameDir) throws IOException {
        // Resolve the path to the users file
        this.usersFile = gameDir.resolve("users.txt");
        // Create the file if it doesn't exist
        if (Files.exists(usersFile)) {
            for (String l: Files.readAllLines(usersFile)) {
                String[] p = l.split(",",2);
                if (p.length==2) users.put(p[0], p[1]);
            }
        }
    }

    // Returns an unmodifiable view of the users map
    public Map<String, String> getUsers() {
        return Collections.unmodifiableMap(users);
    }

    // Initializes the admin user by prompting for a password
    public void initAdmin(Console console) throws IOException {
        if (!users.isEmpty()) throw new IllegalStateException("Admin already exists"); // Ensure no admin exists
        String pwd = readHidden(console, "Set admin password: "); // Prompt for admin password
        users.put("admin", hash(pwd)); // Hash and store the password
        save(); // Save the updated users map to the file
    }

    // Adds a new user to the system
    public void addUser(String name, Console console) throws IOException {
        if (users.size() >= 11) // Check if the maximum number of users is reached
            throw new IllegalStateException("Maximum number of users reached (10)");
        if ("admin".equalsIgnoreCase(name)) // Ensure 'admin' is reserved
            throw new IllegalArgumentException("'admin' reserved");
        if (users.containsKey(name)) // Check if the user already exists
            throw new IllegalArgumentException("User already exists");
        String pwd = readHidden(console, "Password for " + name + ": "); // Prompt for the user's password
        users.put(name, hash(pwd)); // Hash and store the password
        save(); // Save the updated users map to the file
    }

    // Removes a user from the system
    public void removeUser(String name) throws IOException {
        if (!users.containsKey(name)) // Ensure the user exists
            throw new IllegalArgumentException("User does not exist");
        if ("admin".equalsIgnoreCase(name)) // Prevent removal of the admin user
            throw new IllegalArgumentException("You cannot remove 'admin'");
        users.remove(name); // Remove the user from the map
        save(); // Save the updated users map to the file
    }

    // Validates the admin password
    public void requireAdmin(Console console) {
        String pwd = readHidden(console, "Admin password: "); // Prompt for the admin password
        if (!hash(pwd).equals(users.get("admin"))) // Compare the hashed password with the stored hash
            throw new SecurityException("Password admin incorrect"); // Throw an exception if incorrect
    }

    // Validates a user's password
    public void requireUser(String user, Console console) {
        if (!users.containsKey(user)) // Ensure the user exists
            throw new SecurityException("Unknown User");
        String pwd = readHidden(console, "Password for " + user + ": "); // Prompt for the user's password
        if (!hash(pwd).equals(users.get(user))) // Compare the hashed password with the stored hash
            throw new SecurityException("Incorrect password"); // Throw an exception if incorrect
    }

    // Saves the users map to the file
    private void save() throws IOException {
        List<String> out = users.entrySet().stream()
            .map(e -> e.getKey() + "," + e.getValue()) // Convert each entry to a "username,hash" string
            .collect(Collectors.toList());
        Files.write(usersFile, out); // Write the list to the file
    }

    // Reads a password from the console without echoing it
    private static String readHidden(Console console, String prompt) {
        if (console == null) throw new IllegalStateException("No console available"); // Ensure a console is available
        char[] pwd = console.readPassword(prompt); // Read the password as a character array
        return new String(pwd); // Convert the character array to a string
    }

    // Hashes a string using SHA3-256
    /*
    AI Prompt: "Write a method that takes a string as input and returns its SHA3-256 hash as a Base64 encoded string.
    The method should handle exceptions and use UTF-8 encoding for the input string."
    Detailed Explanation: As per the requirements, the method takes a string input, computes its SHA3-256 hash,
    and returns the hash as a Base64 encoded string. It handles exceptions that may occur during hashing or encoding.
    It was needed to ensure the security of user passwords by hashing them before storage.
    The method uses the MessageDigest class to compute the hash and the Base64 class to encode it.
    The UTF-8 encoding is specified to ensure compatibility with a wide range of characters.
    */
    private static String hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA3-256"); // AI Generated
            byte[] b = md.digest(input.getBytes("UTF-8")); // AI Generated
            return Base64.getEncoder().encodeToString(b); // AI Generated
        } catch (Exception e) {
            throw new RuntimeException(e); // AI Generated
        }
    }
}
