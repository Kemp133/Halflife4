package com.halflife3.DatabaseUI;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;

public class Password {

    private static String alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    //Creates a salt for the user when creating a new account
    public static String returnSalt() {
        SecureRandom random = new SecureRandom();

        StringBuilder salt = new StringBuilder(16);

        for (int i = 0; i < 16; i++) {
            salt.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        return new String(salt);
    }

    public static byte[] hash(char[] password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, 1000, 256);
        Arrays.fill(password, Character.MIN_VALUE);
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new AssertionError("Error while hashing a password: " + e.getMessage(), e);
        } finally {
            spec.clearPassword();
        }
    }

    public static String generateSecurePassword(String password, String salt) {
        String returnValue = null;
        byte[] securePassword = hash(password.toCharArray(), salt.getBytes());

        returnValue = Base64.getEncoder().encodeToString(securePassword);

        return returnValue;
    }
}

