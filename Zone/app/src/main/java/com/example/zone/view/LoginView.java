package com.example.zone.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.example.zone.R;
import com.example.zone.controller.Login;
import com.example.zone.model.Database;
import com.example.zone.model.Session;


public class LoginView extends AppCompatActivity {
    private Login controller;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        Session.init(this);
        setContentView(R.layout.login_page);
        EditText username = findViewById(R.id.usernameEditText);
        EditText password = findViewById(R.id.passwordEditText);
        TextView loginButton = findViewById(R.id.loginButton);
        TextView forgot_password = findViewById(R.id.forgotPassword);
        TextView Register_now = findViewById(R.id.registerButton);
        controller = new Login(new Database(this));
        loginButton.setOnClickListener(v -> {
            boolean success = controller.login(
                    username.getText().toString(),
                    password.getText().toString()
            );
            if (success) {
                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                Session.setUsername(username.getText().toString());
                Session.setUserID(controller.getUserID(Session.getUsername()));
                Intent intent = new Intent(this, MainView.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Invalid username or password.", Toast.LENGTH_SHORT).show();
            }
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
