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
            VirtualDatabase db = new VirtualDatabase();
            String Username = username.getText().toString().trim();
            String Password = password.getText().toString().trim();
            String Confirm = confirm.getText().toString().trim();
            if (!Password.equals(Confirm)) {
                Toast.makeText(this, "Password does not match, please try again", Toast.LENGTH_SHORT).show();
                return;
            }
            System.out.println("this button is being clicked");
            db.createAccount(Username, Password, this, success -> {
                if (success) {
                    Toast.makeText(this, "Account Created!", Toast.LENGTH_SHORT).show();
                    
                    // Initialize local session
                    int localID = controller.getUserID(Username);
                    if (localID == -1) {
                        Database sqliteDb = new Database(this);
                        sqliteDb.addUser(Username, "FIREBASE_AUTHED");
                        localID = sqliteDb.getUserID(Username);
                    }
                    Session.setUserID(localID);
                    Session.setUsername(Username);

                    Intent intent = new Intent(this, MainView.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Account not created, Invalid registration information", Toast.LENGTH_SHORT).show();
                }

            });
        });


//        register.setOnClickListener(v -> {
//            String enteredUsername = username.getText().toString().trim();
//            boolean success = controller.confirmRegistration(
//                    enteredUsername,
//                    password.getText().toString(),
//                    confirm.getText().toString()
//            );
//            if (success) {
//                Toast.makeText(this, "User created!", Toast.LENGTH_SHORT).show();
//                Session.setUsername(enteredUsername);
//                Session.setUserID(controller.getUserID(enteredUsername));
//                Intent intent = new Intent(this, MainView.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
//                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                startActivity(intent);
//                finish();
//            } else {
//                Toast.makeText(this, "User not created!", Toast.LENGTH_SHORT).show();
//            }
//        });
        login.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginView.class);
            startActivity(intent);
            finish();
        });
    }
}
