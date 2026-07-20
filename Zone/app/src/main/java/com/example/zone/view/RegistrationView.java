package com.example.zone.view;
import com.example.zone.controller.Registration;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.zone.R;
import com.example.zone.model.Database;


public class RegistrationView extends AppCompatActivity {
    private Registration controller;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_account);
        EditText username = findViewById(R.id.usernameEditText);
        EditText password = findViewById(R.id.passwordInput);
        EditText confirm = findViewById(R.id.passwordInput2);
        TextView register = findViewById(R.id.createAccountButton);
        TextView login = findViewById(R.id.alreadyHaveAccountButton);
        controller = new Registration(new Database(this));
        register.setOnClickListener(v -> {
            boolean success = controller.confirmRegistration(
                    username.getText().toString(),
                    password.getText().toString(),
                    confirm.getText().toString()
            );
            if (success) {
                Toast.makeText(this, "User created!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MainView.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "User not created!", Toast.LENGTH_SHORT).show();
            }
        });
        login.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginView.class);
            startActivity(intent);
        });
    }
}
