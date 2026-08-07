package com.example.zone.view;
import com.example.zone.controller.Registration;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.zone.R;
import com.example.zone.model.Database;
import com.example.zone.model.Session;
import com.example.zone.model.VirtualDatabase;

import java.util.Locale;


public class RegistrationView extends AppCompatActivity {
    private Registration controller;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Session.init(getApplicationContext());
        setContentView(R.layout.create_account);
        EditText username = findViewById(R.id.usernameEditText);
        EditText password = findViewById(R.id.passwordInput);
        EditText confirm = findViewById(R.id.passwordInput2);
        TextView register = findViewById(R.id.createAccountButton);
        TextView login = findViewById(R.id.alreadyHaveAccountButton);
        controller = new Registration(new Database(this));
        register.setOnClickListener(v -> {
            VirtualDatabase db = new VirtualDatabase();
            String Username = username.getText().toString()
                    .trim()
                    .toLowerCase(Locale.ROOT);
            String Password = password.getText().toString().trim();
            String Confirm = confirm.getText().toString().trim();
            if (!Patterns.EMAIL_ADDRESS.matcher(Username).matches()) {
                username.setError("Enter a valid email address");
                return;
            }
            if (Password.length() < 6) {
                password.setError("Password must contain at least 6 characters");
                return;
            }
            if (!Password.equals(Confirm)) {
                Toast.makeText(this, "Password does not match, please try again", Toast.LENGTH_SHORT).show();
                return;
            }
            register.setEnabled(false);
            db.createAccount(Username, Password, (success, message) -> {
                register.setEnabled(true);
                if (success) {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    
                    // Initialize local session
                    Database sqliteDb = new Database(this);
                    int localID = sqliteDb.ensureRemoteUser(Username);
                    Session.setUserID(localID);
                    Session.setUsername(Username);

                    Intent intent = new Intent(this, MainContainerActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                }

            });
        });

        login.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginView.class);
            startActivity(intent);
            finish();
        });
    }
}
