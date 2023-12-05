package comp3911.cwk2;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BruteForceBlock {
    private static final int MAX_LOGIN_ATTEMPTS = 3; // Adjust as needed
    private static final long LOCKOUT_DURATION = 5 * 60 * 1000; // 5 minutes in milliseconds

    private final Map<String, Integer> failedLoginAttempts = new ConcurrentHashMap<>();
    private final Map<String, Long> lockedAccounts = new ConcurrentHashMap<>();

    public boolean isAccountLocked(String username) {
        Long lockoutTime = lockedAccounts.get(username);
        return lockoutTime != null && System.currentTimeMillis() - lockoutTime < LOCKOUT_DURATION;
    }

    public void handleFailedLogin(String username) {
        int attempts = failedLoginAttempts.getOrDefault(username, 0) + 1;
        failedLoginAttempts.put(username, attempts);

        if (attempts >= MAX_LOGIN_ATTEMPTS) {
            lockedAccounts.put(username, System.currentTimeMillis());
        }
    }

    public void handleSuccessfulLogin(String username) {
        failedLoginAttempts.remove(username);
        lockedAccounts.remove(username);
    }
}
