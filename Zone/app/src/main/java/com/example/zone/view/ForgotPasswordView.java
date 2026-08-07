package com.example.zone.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zone.R;
import com.example.zone.model.VirtualDatabase;

import java.util.Locale;

public class ForgotPasswordView extends AppCompatActivity {
    private EditText emailInput;
    private TextView submit;
    private TextView backToLogin;
    private TextView registration;
    VirtualDatabase db = new VirtualDatabase();

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgor_password);
        emailInput = findViewById(R.id.forgot_password_email);
        submit = findViewById(R.id.resetPasswordButton);
        backToLogin = findViewById(R.id.back2Login);
        registration = findViewById(R.id.backtoRegistration);

        backToLogin.setOnClickListener(v-> {
            Intent intent = new Intent(this, LoginView.class);
            startActivity(intent);
        });
        registration.setOnClickListener(v->{
            Intent intent = new Intent(this, RegistrationView.class);
            startActivity(intent);
        });
        submit.setOnClickListener(v->{
            String email = emailInput.getText().toString()
                    .trim()
                    .toLowerCase(Locale.ROOT);
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailInput.setError("Enter your email first");
                emailInput.requestFocus();
                return;
            }
            submit.setEnabled(false);
            db.sendPasswordResetEmail(email, (success, message) -> {
                submit.setEnabled(true);
                Toast.makeText(
                        ForgotPasswordView.this,
                        message,
                        success ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT
                ).show();
            });
        });
    }
}
