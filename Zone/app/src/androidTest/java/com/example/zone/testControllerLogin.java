package com.example.zone;


import static org.junit.Assert.*;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import com.example.zone.model.Database;
import com.example.zone.controller.Login;


import org.junit.Test;
public class testControllerLogin {
    Context context = InstrumentationRegistry
            .getInstrumentation()
            .getTargetContext();
    Database db = new Database(context);

    @Test

    public void testGoodLogin() {
        assertTrue(db.addUser("GoodUsername1", "GoodPassword1"));
        Login login1 = new Login(db);
        assertTrue(login1.login("GoodUsername1", "GoodPassword1"));
    }
    @Test

    public void testBadUsernameLogin() {
        db.addUser("Username", "Password");
        Login login = new Login(db);
        boolean result = login.login("Usernames", "Password");
        assertFalse(result);
    }
    @Test

    public void testBadPassword() {
        db.addUser("Username", "Password");
        Login login = new Login(db);
        boolean result = login.login("Usernames", "Passwor");
        assertFalse(result);
    }
    //verifyPassword
    @Test
    public void testVerifiyPassword() {
        db.addUser("Username", "Password");
        Login login = new Login(db);

        boolean result = login.verifyPassword("Username", "Password");
        assertTrue(result);
    }
}
