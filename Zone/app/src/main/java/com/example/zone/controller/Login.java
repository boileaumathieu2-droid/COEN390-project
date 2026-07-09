package com.example.zone.controller;

import android.util.Base64;

import com.example.zone.model.Database;

import java.security.MessageDigest;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class Login {

    private Database database;

    public Login(Database database) {
        this.database = database;
    }

    public boolean login(String username, String password) {

        if (!database.verifyUser(username)) {
            return false;
        }

        String storedHash = database.findPassword(username);

        return verifyPassword(password, storedHash);
    }

    private boolean verifyPassword(String password, String storedHash) {

        if (storedHash == null) {
            return false;
        }

        try {
            byte[] saltAndHash = Base64.decode(storedHash, Base64.DEFAULT);

            byte[] salt = new byte[16];
            System.arraycopy(saltAndHash, 0, salt, 0, 16);

            byte[] storedPasswordHash = new byte[saltAndHash.length - 16];
            System.arraycopy(
                    saltAndHash,
                    16,
                    storedPasswordHash,
                    0,
                    storedPasswordHash.length
            );

            PBEKeySpec spec = new PBEKeySpec(
                    password.toCharArray(),
                    salt,
                    65536,
                    128
            );

            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            byte[] newHash = factory.generateSecret(spec).getEncoded();

            spec.clearPassword();

            return MessageDigest.isEqual(newHash, storedPasswordHash);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}