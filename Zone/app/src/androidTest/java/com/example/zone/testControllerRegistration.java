package com.example.zone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import com.example.zone.controller.Login;
import com.example.zone.controller.Registration;
import com.example.zone.model.Database;

import org.junit.Test;


public class testControllerRegistration {
    Context context = InstrumentationRegistry
            .getInstrumentation()
            .getTargetContext();
    Database db = new Database(context);
    Registration registration = new Registration(db);

    @Test
    public void testRegistration() {
       String username = "Username";
       String password = "Password";
       String ConfirmPassword ="Password";
       boolean result = registration.confirmRegistration(username, password, ConfirmPassword);
       assertTrue(result);
    }
    @Test
    public void testRegistration_short_username1() {
        String username = "usern";
        String password = "Password";
        String ConfirmPassword ="Passsword";
        boolean result = registration.confirmRegistration(username, password, ConfirmPassword);
        assertFalse(result);
    }
    @Test
    public void testRegistration_special_Character_username() {
        String username = "usern_!n";
        String password = "Password";
        String ConfirmPassword = "Passsword";
        boolean result = registration.confirmRegistration(username, password, ConfirmPassword);
        assertFalse(result);
    }
    @Test
    public void testRegistration_passwordMismatch() {
        String username = "username";
        String password = "Password";
        String ConfirmPassword = "Passswor";
        boolean result = registration.confirmRegistration(username, password, ConfirmPassword);
        assertFalse(result);
    }
    @Test
    public void testRegistration_shortpassword() {
        String username = "Username";
        String password = "Passw";
        String ConfirmPassword = "Passsw";
        boolean result = registration.confirmRegistration(username, password, ConfirmPassword);
        assertFalse(result);
    }

    @Test
    public void  TesthashPassword() {
        Login login = new Login(db);
        String password = registration.hashPassword("Hashing");
        db.addUser("Username", password);
        assertNotEquals(password, null);
        String password3 = registration.hashPassword("hashing2");
        assertNotEquals(password, password3);
    }
