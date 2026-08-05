package com.example.zone.view;

import android.content.Intent;
import android.os.Bundle;
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

        if(db.getCurrentUserId() != null) {
            String email = db.getCurrentUserEmail();
            if (email != null) {
                ensureLocalSession(email);
            }
            Intent intent = new Intent(this, MainContainerActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.login_page);
        EditText username = findViewById(R.id.usernameEditText);
        EditText password = findViewById(R.id.passwordEditText);
        TextView loginButton = findViewById(R.id.loginButton);
        TextView forgot_password = findViewById(R.id.forgotPassword);
        TextView Register_now = findViewById(R.id.registerButton);
        controller = new Login(new Database(this));
        loginButton.setOnClickListener(v -> {
            String userStr = username.getText().toString();
            db.signIn(userStr, password.getText().toString(), success-> {
                if (success) {
                    Toast.makeText(this, "Sign in successful", Toast.LENGTH_SHORT).show();
                    
                    // Initialize local session
                    ensureLocalSession(userStr);

                    Intent intent = new Intent(this, MainContainerActivity.class);
                    startActivity(intent);
                    finish();
                }
                else {
                    Toast.makeText(this, "Incorect information, Try Again!", Toast.LENGTH_SHORT).show();
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

    private void ensureLocalSession(String username) {
        Database localDatabase = new Database(this);
        int localId = localDatabase.getUserID(username);
        if (localId == -1) {
            localDatabase.addUser(username, "FIREBASE_AUTHED");
            localId = localDatabase.getUserID(username);
        }
        Session.setUserID(localId);
        Session.setUsername(username);
    }
}
