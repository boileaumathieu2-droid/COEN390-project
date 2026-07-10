package com.example.zone.view;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.zone.R;

public class MainView extends AppCompatActivity {
        @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_menu, menu);
        return true;}
    public boolean onOptionsItemSelected(MenuItem option) {
        int id = option.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainView.this, SettingsView.class);
            startActivity(intent);

        }

        return super.onOptionsItemSelected(option);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        mainView = this;
        mainController = new MainController(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public static void openTimerSettings() {
        Intent intent = new Intent(mainView, TimerSettingsView.class);
        mainView.startActivity(intent);
    }
}
