package com.example.zone.view;

import static com.example.zone.model.VirtualDatabase.isInternetAvailable;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.zone.R;
import com.example.zone.controller.Login;
import com.example.zone.model.Database;
import com.example.zone.model.Session;
import com.example.zone.model.VirtualDatabase;
public class LoginView extends AppCompatActivity {
    private Login controller;
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Session.init(this);
        VirtualDatabase db = new VirtualDatabase();
        Database SQLdb = new Database(this);

        // If Android recreated the launcher Activity after the app was minimized,
        // restore the existing authenticated session instead of showing login again.
//        if (Session.getUsername() != null && Session.getUserID() != -1) {
//            Intent intent = new Intent(this, MainView.class);
//            startActivity(intent);
//            finish();
//            return;
//        }
        if(db.getCurrentUserId() != null) {
            String user = db.getCurrentUserId();
            System.out.println("USERNAME!!!: " + user);
            Session.setUsername(user);
            int SQLID  = SQLdb.getUserID(user);
            Session.setUserID(SQLID);
            Log.d("userID= ", "USERID!!!: " + SQLdb.getUserID(user));
            System.out.println("USERID!! = " +(SQLdb.getUserID(user)));
            Intent intent = new Intent(this, MainView.class);
            startActivity(intent);
        }
        setContentView(R.layout.login_page);
        EditText username = findViewById(R.id.usernameEditText);
        EditText password = findViewById(R.id.passwordEditText);
        TextView loginButton = findViewById(R.id.loginButton);
        TextView forgot_password = findViewById(R.id.forgotPassword);
        TextView Register_now = findViewById(R.id.registerButton);
        controller = new Login(new Database(this));
        loginButton.setOnClickListener(v -> {
            String userStr = username.getText().toString().trim();
            String passwordStr = password.getText().toString().trim();
            db.signIn(userStr, passwordStr, success -> {
                if (success) {
                    Toast.makeText(this, "Sign in successful", Toast.LENGTH_SHORT).show();
                    int localID = controller.getUserID(db.getCurrentUserId());
                    if (localID == -1) {
                        Database sqliteDb = new Database(this);
                        localID = sqliteDb.getUserID(userStr);

                    }

                    Session.setUserID(localID);
                    Session.setUsername(userStr);
                    System.out.println("USERID!! = " +(SQLdb.getUserID(db.getCurrentUserId())));


                    Intent intent = new Intent(this, MainView.class);
                    startActivity(intent);
                    finish();

                } else {
                    if (!isInternetAvailable(this)) {
                        Toast.makeText(this, "Internet connection required!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Incorrect information, try again!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });


        forgot_password.setOnClickListener(v -> {
            //Intent intent = new Intent(this, MainView.class);
            //startActivity(intent);
        });
        Register_now.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegistrationView.class);
            startActivity(intent);
        });
    }
}
