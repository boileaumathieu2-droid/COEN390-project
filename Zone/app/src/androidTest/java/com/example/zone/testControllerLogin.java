//package com.example.zone;
//
//import static org.junit.Assert.assertFalse;
//import static org.junit.Assert.assertTrue;
//
//import android.content.Context;
//
//import androidx.test.platform.app.InstrumentationRegistry;
//
//import com.example.zone.controller.Login;
//import com.example.zone.controller.Registration;
//import com.example.zone.model.Database;
//
//import org.junit.After;
//import org.junit.Test;
//
//public class testControllerLogin {
//
//    private final Context context =
//            InstrumentationRegistry.getInstrumentation().getTargetContext();
//
//    private final Database db = new Database(context);
//
//    private String createUniqueUsername(String prefix) {
//        return prefix + Long.toUnsignedString(System.nanoTime());
//    }
//
//    private void addTestUser(String username, String password) {
//        Registration registration = new Registration(db);
//        String hashedPassword = registration.hashPassword(password);
//        assertTrue(db.addUser(username, hashedPassword));
//    }
//
//    @After
//    public void closeDatabase() {
//        db.close();
//    }
//
//    @Test
//    public void testGoodLogin() {
//        String username = createUniqueUsername("GoodUser");
//        String password = "GoodPassword1";
//
//        addTestUser(username, password);
//
//        Login login = new Login(db);
//        assertTrue(login.login(username, password));
//    }
//
//    @Test
//    public void testBadUsernameLogin() {
//        String username = createUniqueUsername("RealUser");
//        String password = "GoodPassword1";
//
//        addTestUser(username, password);
//
//        Login login = new Login(db);
//        boolean result = login.login("Wrong" + username, password);
//
//        assertFalse(result);
//    }
//
//    @Test
//    public void testBadPasswordLogin() {
//        String username = createUniqueUsername("PasswordUser");
//        String correctPassword = "GoodPassword1";
//
//        addTestUser(username, correctPassword);
//
//        Login login = new Login(db);
//        boolean result = login.login(username, "WrongPassword1");
//
//        assertFalse(result);
//    }
//
//    @Test
//    public void testPasswordVerificationThroughLogin() {
//        String username = createUniqueUsername("VerifyUser");
//        String password = "Password123";
//
//        addTestUser(username, password);
//
//        Login login = new Login(db);
//
//        assertTrue(login.login(username, password));
//        assertFalse(login.login(username, "IncorrectPassword"));
//    }
//}