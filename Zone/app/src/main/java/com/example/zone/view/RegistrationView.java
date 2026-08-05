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
        VirtualDatabase db = new VirtualDatabase();
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
            if(!passwordText.equals(confirmText)) {
                Toast.makeText(this, "password does not match", Toast.LENGTH_SHORT).show();
                return;
            }
            System.out.println("this button is being clicked");
            db.createAccount(usernameText, passwordText, this, success -> {
                if (success) {
                    String user = db.getCurrentUserId();
                    Toast.makeText(this, "Account Created!", Toast.LENGTH_SHORT).show();
                    Database SQLdb = new Database(this);
                    SQLdb.addUser(user, usernameText);
                    int localID = SQLdb.getUserID(user);
                    Session.setUserID(localID);
                    Session.setUsername(user);
                    Session.setUsername(usernameText);
                    Intent intent = new Intent(this, MainView.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Account not created, Invalid registration information", Toast.LENGTH_SHORT).show();
                }
            });
        });
        login.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginView.class);
            startActivity(intent);
            finish();
        });

        login.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginView.class);
            startActivity(intent);
        });
    }
}
