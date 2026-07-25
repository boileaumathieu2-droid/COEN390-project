package com.example.zone;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import com.example.zone.controller.Login;
import com.example.zone.controller.Registration;
import com.example.zone.model.Database;

import org.junit.After;
import org.junit.Test;

public class testControllerRegistration {

    private final Context context =
            InstrumentationRegistry
                    .getInstrumentation()
                    .getTargetContext();

    private final Database db =
            new Database(context);

    private final Registration registration =
            new Registration(db);

    private String createUniqueUsername(String prefix) {
        return prefix
                + Long.toUnsignedString(System.nanoTime());
    }

    @After
    public void closeDatabase() {
        db.close();
    }

    @Test
    public void testSuccessfulRegistration() {
        String username =
                createUniqueUsername("ValidUser");

        String password = "Password1";

        boolean result =
                registration.confirmRegistration(
                        username,
                        password,
                        password
                );

        assertTrue(result);

        Login login = new Login(db);

        assertTrue(
                login.login(username, password)
        );
    }

    @Test
    public void testRegistrationWithShortUsername() {
        String username = "user1";
        String password = "Password1";

        boolean result =
                registration.confirmRegistration(
                        username,
                        password,
                        password
                );

        assertFalse(result);
    }

    @Test
    public void testRegistrationWithSpecialCharacterUsername() {
        String username = "User_123";
        String password = "Password1";

        boolean result =
                registration.confirmRegistration(
                        username,
                        password,
                        password
                );

        assertFalse(result);
    }

    @Test
    public void testRegistrationWithPasswordMismatch() {
        String username =
                createUniqueUsername("MismatchUser");

        String password = "Password1";
        String confirmation = "DifferentPassword1";

        boolean result =
                registration.confirmRegistration(
                        username,
                        password,
                        confirmation
                );

        assertFalse(result);
    }

    @Test
    public void testRegistrationWithShortPassword() {
        String username =
                createUniqueUsername("ShortPasswordUser");

        String password = "Pass1";

        boolean result =
                registration.confirmRegistration(
                        username,
                        password,
                        password
                );

        assertFalse(result);
    }

    @Test
    public void testHashPassword() {
        String plainPassword = "HashingPassword1";

        String firstHash =
                registration.hashPassword(plainPassword);

        String secondHash =
                registration.hashPassword(plainPassword);

        assertNotNull(firstHash);
        assertNotNull(secondHash);

        assertNotEquals(
                plainPassword,
                firstHash
        );

        /*
         * The hashes should be different because each one
         * uses a new random salt.
         */
        assertNotEquals(
                firstHash,
                secondHash
        );
    }
}