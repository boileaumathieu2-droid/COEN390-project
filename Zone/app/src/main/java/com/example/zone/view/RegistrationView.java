package com.example.zone.view;
import static com.example.zone.model.VirtualDatabase.isInternetAvailable;

import com.example.zone.controller.Registration;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.zone.R;
import com.example.zone.model.Database;
import com.example.zone.model.Session;
import com.example.zone.model.VirtualDatabase;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;


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
            String usernameText = username.getText().toString().trim();
            String passwordText = password.getText().toString().trim();
            String confirmText = confirm.getText().toString().trim();
            if (!isInternetAvailable(this)) {
                Toast.makeText(this,
                        "Internet connection required!",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (!passwordText.equals(confirmText)) {
                Toast.makeText(this,
                        "Passwords do not match",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            VirtualDatabase firebaseDb = new VirtualDatabase();
            firebaseDb.createAccount(
                    usernameText,
                    passwordText,
                    this,
                    success -> {
                        if (!success) {
                            Toast.makeText(this,
                                    "Account not created. Invalid registration information.",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Database localDb = new Database(this);
                        localDb.addUser(
                                firebaseDb.getCurrentUserId(),
                                usernameText,
                                localSuccess -> {
                                    if (!localSuccess) {
                                        Toast.makeText(this,
                                                "Account created, but local save failed.",
                                                Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    Toast.makeText(this,
                                            "Account Created!",
                                            Toast.LENGTH_SHORT).show();
                                    Intent intent =
                                            new Intent(this, MainView.class);
                                    startActivity(intent);
                                    finish();
                                });
                    });
        });



        login.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginView.class);
            startActivity(intent);
        });
    }
}
