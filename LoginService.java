import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Employee Management System - STQA Academic Project
 * LoginService Class
 * Handles administrator authentication, session token generation,
 * session validation, and logout.
 */
public class LoginService {

    // Administrator credentials
    public static final String ADMIN_USERNAME = "sanman";
    public static final String ADMIN_PASSWORD = "sanman123";

    // In-memory active session tokens for secure session tracking
    private static final Set<String> activeSessions = new HashSet<>();

    /**
     * Authenticates the administrator username and password.
     * Uses ADMIN_USERNAME and ADMIN_PASSWORD.
     * Username is case-insensitive.
     * Also supports admin / admin123 as academic backup.
     */
    public boolean authenticate(String username, String password) {
        if (username == null || password == null) {
            return false;
        }
        String u = username.trim();
        String p = password.trim();

        // Primary check using configured admin credentials
        if (ADMIN_USERNAME.equalsIgnoreCase(u) && ADMIN_PASSWORD.equals(p)) {
            return true;
        }

        // Academic backup: admin / admin123
        if ("admin".equalsIgnoreCase(u) && "admin123".equals(p)) {
            return true;
        }

        return false;
    }

    /**
     * Creates a new session upon successful login.
     * @return Unique session token string
     */
    public synchronized String createSession() {
        String token = "EMS_SESSION_" + UUID.randomUUID().toString().replace("-", "");
        activeSessions.add(token);
        return token;
    }

    /**
     * Checks if a session token is valid and active.
     */
    public synchronized boolean isValidSession(String sessionToken) {
        if (sessionToken == null || sessionToken.trim().isEmpty()) {
            return false;
        }
        return activeSessions.contains(sessionToken.trim());
    }

    /**
     * Terminates a session on logout.
     */
    public synchronized void invalidateSession(String sessionToken) {
        if (sessionToken != null) {
            activeSessions.remove(sessionToken.trim());
        }
    }

    /**
     * Extracts session cookie value from HTTP Cookie header.
     */
    public String extractSessionToken(String cookieHeader) {
        if (cookieHeader == null || cookieHeader.trim().isEmpty()) {
            return null;
        }
        String[] cookies = cookieHeader.split(";");
        for (String c : cookies) {
            String trimmed = c.trim();
            if (trimmed.startsWith("EMS_AUTH_TOKEN=")) {
                return trimmed.substring("EMS_AUTH_TOKEN=".length()).trim();
            }
            if (trimmed.startsWith("SESSION=")) {
                return trimmed.substring("SESSION=".length()).trim();
            }
            if (trimmed.startsWith("SESSIONID=")) {
                return trimmed.substring("SESSIONID=".length()).trim();
            }
        }
        return null;
    }

    public static String getAdminUsername() {
        return ADMIN_USERNAME;
    }
}
