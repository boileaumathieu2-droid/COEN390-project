package com.example.zone.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.zone.R;
import com.example.zone.controller.Login;
import com.example.zone.model.Database;
import com.example.zone.model.Session;
import com.example.zone.model.VirtualDatabase;

import java.util.Locale;


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
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
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
            String userStr = username.getText().toString()
                    .trim()
                    .toLowerCase(Locale.ROOT);
            if (!Patterns.EMAIL_ADDRESS.matcher(userStr).matches()) {
                username.setError("Enter a valid email address");
                return;
            }
            if (password.getText().toString().isEmpty()) {
                password.setError("Enter your password");
                return;
            }
            loginButton.setEnabled(false);
            db.signIn(userStr, password.getText().toString(), success-> {
                loginButton.setEnabled(true);
                if (success) {
                    Toast.makeText(this, "Sign in successful", Toast.LENGTH_SHORT).show();
                    
                    // Initialize local session
                    ensureLocalSession(userStr);

                    Intent intent = new Intent(this, MainContainerActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
                else {
                    Toast.makeText(this, "Incorrect email or password.", Toast.LENGTH_SHORT).show();
                }
            });
        });
        forgot_password.setOnClickListener(v -> {
            String email = username.getText().toString()
                    .trim()
                    .toLowerCase(Locale.ROOT);
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                username.setError("Enter your email first");
                username.requestFocus();
                return;
            }
            forgot_password.setEnabled(false);
            db.sendPasswordResetEmail(email, (success, message) -> {
                forgot_password.setEnabled(true);
                Toast.makeText(
                        LoginView.this,
                        message,
                        success ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT
                ).show();
            });
        });
        Register_now.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegistrationView.class);
            startActivity(intent);
        });
    }

    private void ensureLocalSession(String username) {
        Database localDatabase = new Database(this);
        String normalizedUsername = username.trim().toLowerCase(Locale.ROOT);
        int localId = localDatabase.ensureRemoteUser(normalizedUsername);
        Session.setUserID(localId);
        Session.setUsername(normalizedUsername);
    }
}
