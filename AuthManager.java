import java.nio.file.*;
import java.security.*;
import java.util.*;
import java.io.*;
import java.util.stream.*;

public class AuthManager {
    private final Path usersFile;
    private final Map<String, String> users = new LinkedHashMap<>();

    public AuthManager(Path gameDir) throws IOException {
        this.usersFile = gameDir.resolve("users.txt");
        if (Files.exists(usersFile)) {
            for (String l: Files.readAllLines(usersFile)) {
                String[] p = l.split(",",2);
                if (p.length==2) users.put(p[0], p[1]);
            }
        }
    }
    public Map<String, String> getUsers() {
        return Collections.unmodifiableMap(users);
    }

    public void initAdmin(Console console) throws IOException {
        if (!users.isEmpty()) throw new IllegalStateException("Admin already exists");
        String pwd = readHidden(console, "Set admin password: ");
        users.put("admin", hash(pwd));
        save();
    }

    public void addUser(String name, Console console) throws IOException {
        if (users.size() >= 11)
            throw new IllegalStateException("Maximum number of users reached (10)");
        if ("admin".equalsIgnoreCase(name)) throw new IllegalArgumentException("'admin' reserved");
        if (users.containsKey(name)) throw new IllegalArgumentException("User already exists");
        String pwd = readHidden(console, "Password for " + name + ": ");
        users.put(name, hash(pwd));
        save();
    }

    public void removeUser(String name) throws IOException {
        if (!users.containsKey(name)) throw new IllegalArgumentException("User does not exist");
        if ("admin".equalsIgnoreCase(name)) throw new IllegalArgumentException("You cannot remove 'admin'");
        users.remove(name); save();
    }

    public void requireAdmin(Console console) {
        String pwd = readHidden(console, "Admin password: ");
        if (!hash(pwd).equals(users.get("admin")))
            throw new SecurityException("Password admin incorrect");
    }

    public void requireUser(String user, Console console) {
        if (!users.containsKey(user))
            throw new SecurityException("Unknown User");
        String pwd = readHidden(console, "Password for " + user + ": ");
        if (!hash(pwd).equals(users.get(user)))
            throw new SecurityException("Incorrect password");
    }

    private void save() throws IOException {
        List<String> out = users.entrySet().stream()
            .map(e->e.getKey()+","+e.getValue())
            .collect(Collectors.toList());
        Files.write(usersFile, out);
    }

    private static String readHidden(Console console, String prompt) {
        if (console == null) throw new IllegalStateException("No console available");
        char[] pwd = console.readPassword(prompt);
        return new String(pwd);
    }

    private static String hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA3-256");
            byte[] b = md.digest(input.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(b);
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
