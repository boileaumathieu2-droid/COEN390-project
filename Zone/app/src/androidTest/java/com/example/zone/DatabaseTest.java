package com.example.zone;

import static org.junit.Assert.*;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import com.example.zone.model.Database;

import org.junit.Test;

public class DatabaseTest {

    @Test
    public void testDatabaseCreation() {
        Context context = InstrumentationRegistry
                .getInstrumentation()
                .getTargetContext();

        Database db = new Database(context);

        assertNotNull(db);
    }

    @Test
    public void testGoodUserInput() {
        Context context = InstrumentationRegistry
                .getInstrumentation()
                .getTargetContext();

        Database db = new Database(context);
        boolean result = db.addUser("goodUsername", "Goodpassword");
        assertTrue(result);
    }

    @Test
    public void testBadUername() {
        Context context = InstrumentationRegistry
                .getInstrumentation()
                .getTargetContext();

        Database db = new Database(context);
        boolean result = db.addUser("Badus", "GoodPassword");
        assertFalse(result);
    }

    @Test
    public void testBadPassword() {
        Context context = InstrumentationRegistry
                .getInstrumentation()
                .getTargetContext();

        Database db = new Database(context);
        boolean result = db.addUser("GoodUsername1", "Badpa");
        assertFalse(result);
    }

    @Test

    public void testBadUserinput() {
        Context context = InstrumentationRegistry
                .getInstrumentation()
                .getTargetContext();

        Database db = new Database(context);
        boolean result = db.addUser("Badus", "Badpas");
        assertFalse(result);
    }

    @Test
    public void TestVerifiyExistingUser() {
        Context context = InstrumentationRegistry
                .getInstrumentation()
                .getTargetContext();

        Database db = new Database(context);
        db.addUser("GoodUsername2", "GoodPassword");
        boolean result = db.verifyUser("GoodUsername2");
        assertTrue(result);

    }

    @Test
    public void TestVerifyNonExistingUser() {
        Context context = InstrumentationRegistry
                .getInstrumentation()
                .getTargetContext();

        Database db = new Database(context);
        db.addUser("GoodUsername3", "GoodPassword");
        boolean result = db.verifyUser("GoodUser");
        assertFalse(result);
    }

    @Test
    public void testFindExistingPassword() {
        Context context = InstrumentationRegistry
                .getInstrumentation()
                .getTargetContext();

        Database db = new Database(context);
        db.addUser("GoodUsername4", "GoodPassword");
        String password = db.findPassword("GoodUsername4");
        assertEquals(password, "GoodPassword");
    }

    @Test
    public void testFindNonExistingPassword() {
        Context context = InstrumentationRegistry
                .getInstrumentation()
                .getTargetContext();

        Database db = new Database(context);
        db.addUser("GoodUsername5", "GoodPassword");
        String password = db.findPassword("GoodUsername5");
        assertNotEquals(password, "notCorrectPassword");

    }

    public void TestUniqueUsername() {
        Context context = InstrumentationRegistry
                .getInstrumentation()
                .getTargetContext();

        Database db = new Database(context);
        db.addUser("GoodUsername4", "GoodPassword");
        boolean result = db.addUser("GoodUsername4", "GoodPassword");
        assertFalse(result);
    }

    public void testUsernameSpecialCharacters() {
        Context context = InstrumentationRegistry
                .getInstrumentation()
                .getTargetContext();

        Database db = new Database(context);
        boolean result = db.addUser("GoodUsername4!", "GoodPassword");
        assertFalse(result);
    }
}
