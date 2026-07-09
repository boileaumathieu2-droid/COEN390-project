package com.example.zone.controller;

import android.util.Base64;

import com.example.zone.model.Database;

import java.security.SecureRandom;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class Registration {

    private Database database;

    public Registration(Database database) {
        this.database = database;
    }

    public boolean confirmRegistration(String username, String password, String confirmPassword) {

        if (!username.matches("^[a-zA-Z0-9]+$")) {
            return false;
        }

        if (!password.equals(confirmPassword) || password.length() < 6 || username.length() < 6) {
            return false;
        }

        String hashedPassword = hashPassword(password);

        return database.addUser(username, hashedPassword);
    }


    public String hashPassword(String password) {

        try {
            byte[] salt = new byte[16];
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(salt);

            PBEKeySpec spec = new PBEKeySpec(
                    password.toCharArray(),
                    salt,
                    65536,
                    128
            );

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = factory.generateSecret(spec).getEncoded();

            byte[] saltAndHash = new byte[salt.length + hash.length];

            System.arraycopy(salt, 0, saltAndHash, 0, salt.length);
            System.arraycopy(hash, 0, saltAndHash, salt.length, hash.length);

            return Base64.encodeToString(
                    saltAndHash,
                    Base64.DEFAULT
            );

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}