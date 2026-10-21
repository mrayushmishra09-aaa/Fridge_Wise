package com.example.fridgewise.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Base64;
import android.util.Log;

import androidx.biometric.BiometricManager;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.example.fridgewise.model.CustomSpace;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.HashSet;
import java.util.Set;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class SecurityManager {
    private static final String TAG = "SecurityManager";
    private static final String PREF_NAME = "FridgeWise_SecurityPrefs";

    private static final String KEY_MASTER_PIN_HASH = "master_pin_hash";
    private static final String KEY_MASTER_PIN_SALT = "master_pin_salt";
    private static final String KEY_RECOVERY_KEY_HASH = "recovery_key_hash";
    private static final String KEY_RECOVERY_KEY_SALT = "recovery_key_salt";
    private static final String KEY_MASTER_LOCK_ENABLED = "master_lock_enabled";
    private static final String KEY_BIOMETRIC_LOCK_ENABLED = "biometric_lock_enabled";
    private static final String KEY_FAILED_ATTEMPTS = "failed_attempts";
    private static final String KEY_LOCKOUT_UNTIL = "lockout_until";

    private static final int PBKDF2_ITERATIONS = 10000;
    private static final int KEY_LENGTH_BITS = 256;
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_MS = 30000; // 30 seconds

    private static SecurityManager instance;
    private SharedPreferences prefs;

    // In-memory unlocked session management
    private final Set<Integer> unlockedSpaceIds = new HashSet<>();
    private boolean isAppUnlockedInSession = false;

    private SecurityManager(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context.getApplicationContext())
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            prefs = EncryptedSharedPreferences.create(
                    context.getApplicationContext(),
                    PREF_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize EncryptedSharedPreferences for SecurityManager", e);
            prefs = context.getApplicationContext().getSharedPreferences("FridgeWise_Security_Fallback", Context.MODE_PRIVATE);
        }
    }

    public static synchronized SecurityManager getInstance(Context context) {
        if (instance == null) {
            instance = new SecurityManager(context);
        }
        return instance;
    }

    // --- Master PIN Management ---

    public boolean isMasterPinSet() {
        return prefs.contains(KEY_MASTER_PIN_HASH) && prefs.getString(KEY_MASTER_PIN_HASH, null) != null;
    }

    public boolean isMasterLockEnabled() {
        return prefs.getBoolean(KEY_MASTER_LOCK_ENABLED, false) && isMasterPinSet();
    }

    public boolean setupMasterPin(String pin) {
        if (pin == null || pin.length() < 4) return false;
        String salt = generateSalt();
        String hash = hashPin(pin, salt);
        if (hash == null) return false;

        prefs.edit()
                .putString(KEY_MASTER_PIN_HASH, hash)
                .putString(KEY_MASTER_PIN_SALT, salt)
                .putBoolean(KEY_MASTER_LOCK_ENABLED, true)
                .putInt(KEY_FAILED_ATTEMPTS, 0)
                .putLong(KEY_LOCKOUT_UNTIL, 0)
                .apply();
        return true;
    }

    public boolean verifyMasterPin(String pin) {
        if (isLockedOut()) return false;

        String savedHash = prefs.getString(KEY_MASTER_PIN_HASH, null);
        String savedSalt = prefs.getString(KEY_MASTER_PIN_SALT, null);

        if (savedHash == null || savedSalt == null) return false;

        String computedHash = hashPin(pin, savedSalt);
        boolean isValid = savedHash.equals(computedHash);

        if (isValid) {
            resetFailedAttempts();
        } else {
            recordFailedAttempt();
        }

        return isValid;
    }

    public boolean disableMasterLock(String currentPin) {
        if (verifyMasterPin(currentPin)) {
            clearMasterPin();
            return true;
        }
        return false;
    }

    public String generateAndStoreRecoveryKey() {
        String rawKey = "FW-" + generateRandomBlock() + "-" + generateRandomBlock() + "-" + generateRandomBlock();
        String salt = generateSalt();
        String hash = hashPin(rawKey, salt);
        if (hash != null) {
            prefs.edit()
                    .putString(KEY_RECOVERY_KEY_HASH, hash)
                    .putString(KEY_RECOVERY_KEY_SALT, salt)
                    .apply();
            return rawKey;
        }
        return null;
    }

    private String generateRandomBlock() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[2];
        random.nextBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    public boolean verifyRecoveryKey(String recoveryKey) {
        if (recoveryKey == null) return false;
        String savedHash = prefs.getString(KEY_RECOVERY_KEY_HASH, null);
        String savedSalt = prefs.getString(KEY_RECOVERY_KEY_SALT, null);
        if (savedHash == null || savedSalt == null) return false;
        String computedHash = hashPin(recoveryKey.trim(), savedSalt);
        return savedHash.equals(computedHash);
    }

    public boolean resetMasterPinWithRecoveryKey(String newPin, String recoveryKey) {
        if (verifyRecoveryKey(recoveryKey)) {
            return setupMasterPin(newPin);
        }
        return false;
    }

    public void clearMasterPin() {
        prefs.edit()
                .remove(KEY_MASTER_PIN_HASH)
                .remove(KEY_MASTER_PIN_SALT)
                .remove(KEY_RECOVERY_KEY_HASH)
                .remove(KEY_RECOVERY_KEY_SALT)
                .putBoolean(KEY_MASTER_LOCK_ENABLED, false)
                .putBoolean(KEY_BIOMETRIC_LOCK_ENABLED, false)
                .putInt(KEY_FAILED_ATTEMPTS, 0)
                .putLong(KEY_LOCKOUT_UNTIL, 0)
                .apply();
    }

    // --- Space-Specific PIN Helper Methods ---

    public String createSaltForSpace() {
        return generateSalt();
    }

    public String hashPinForSpace(String pin, String salt) {
        return hashPin(pin, salt);
    }

    public boolean verifySpacePin(CustomSpace space, String pin) {
        if (space == null || pin == null) return false;
        if (isLockedOut()) return false;

        boolean isValid = false;
        if ("CUSTOM_PIN".equals(space.getProtectionType()) && space.getPinHash() != null && space.getPinSalt() != null) {
            String computedHash = hashPin(pin, space.getPinSalt());
            isValid = space.getPinHash().equals(computedHash);
        } else {
            // Default to Master PIN
            isValid = verifyMasterPin(pin);
        }

        if (isValid) {
            resetFailedAttempts();
            unlockSpaceSession(space.getId());
        } else {
            recordFailedAttempt();
        }

        return isValid;
    }

    // --- Biometric Authentication Support ---

    public boolean isBiometricAvailable(Context context) {
        BiometricManager biometricManager = BiometricManager.from(context);
        int result = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.BIOMETRIC_WEAK
        );
        return result == BiometricManager.BIOMETRIC_SUCCESS;
    }

    public boolean isBiometricEnabled() {
        return prefs.getBoolean(KEY_BIOMETRIC_LOCK_ENABLED, false) && isMasterPinSet();
    }

    public void setBiometricEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_LOCK_ENABLED, enabled).apply();
    }

    // --- Lockout & Rate Limiting ---

    public boolean isLockedOut() {
        long lockoutUntil = prefs.getLong(KEY_LOCKOUT_UNTIL, 0);
        if (lockoutUntil > System.currentTimeMillis()) {
            return true;
        }
        return false;
    }

    public long getLockoutRemainingSeconds() {
        long lockoutUntil = prefs.getLong(KEY_LOCKOUT_UNTIL, 0);
        long now = System.currentTimeMillis();
        if (lockoutUntil > now) {
            return (lockoutUntil - now) / 1000;
        }
        return 0;
    }

    private void recordFailedAttempt() {
        int failed = prefs.getInt(KEY_FAILED_ATTEMPTS, 0) + 1;
        if (failed >= MAX_FAILED_ATTEMPTS) {
            long lockoutUntil = System.currentTimeMillis() + LOCKOUT_DURATION_MS;
            prefs.edit()
                    .putInt(KEY_FAILED_ATTEMPTS, 0)
                    .putLong(KEY_LOCKOUT_UNTIL, lockoutUntil)
                    .apply();
        } else {
            prefs.edit().putInt(KEY_FAILED_ATTEMPTS, failed).apply();
        }
    }

    private void resetFailedAttempts() {
        prefs.edit()
                .putInt(KEY_FAILED_ATTEMPTS, 0)
                .putLong(KEY_LOCKOUT_UNTIL, 0)
                .apply();
    }

    // --- Session Unlock Tokens ---

    public boolean isSpaceUnlockedInSession(int spaceId) {
        return unlockedSpaceIds.contains(spaceId);
    }

    public void unlockSpaceSession(int spaceId) {
        unlockedSpaceIds.add(spaceId);
    }

    public void lockSpaceSession(int spaceId) {
        unlockedSpaceIds.remove(spaceId);
    }

    public void clearAllSessions() {
        unlockedSpaceIds.clear();
        isAppUnlockedInSession = false;
    }

    public boolean isAppUnlockedInSession() {
        return isAppUnlockedInSession;
    }

    public void setAppUnlockedInSession(boolean unlocked) {
        this.isAppUnlockedInSession = unlocked;
    }

    // --- Cryptographic Helpers ---

    private String generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.encodeToString(salt, Base64.NO_WRAP);
    }

    private String hashPin(String pin, String saltBase64) {
        try {
            byte[] salt = Base64.decode(saltBase64, Base64.NO_WRAP);
            PBEKeySpec spec = new PBEKeySpec(pin.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH_BITS);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return Base64.encodeToString(hash, Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            Log.e(TAG, "PBKDF2 hashing failed", e);
            return null;
        }
    }
}
