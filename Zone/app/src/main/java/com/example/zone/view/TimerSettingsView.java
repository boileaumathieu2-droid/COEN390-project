package com.example.zone.view;

import android.content.Intent;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import com.example.zone.controller.TimerSettingsController;
import com.example.zone.R;

public class TimerSettingsView extends AppCompatActivity {

    private EditText studyMinutes;
    private EditText studySeconds;
    private EditText breakMinutes;
    private EditText breakSeconds;
    private androidx.appcompat.widget.SwitchCompat breakTimerSwitch;
    private TimerSettingsController timerSettingsController;

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
            Intent intent = new Intent(this, SettingsView.class);
            startActivity(intent);

        }
        else if (id == R.id.action_grade_view){
            Intent intent = new Intent(this, GradesTrackerView.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(option);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.timer_settings);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Configure Timer");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize UI components
        studyMinutes = findViewById(R.id.edit_study_minutes);
        studySeconds = findViewById(R.id.edit_study_seconds);
        breakMinutes = findViewById(R.id.edit_break_minutes);
        breakSeconds = findViewById(R.id.edit_break_seconds);
        breakTimerSwitch = findViewById(R.id.switch_break_timer);

        android.view.View rootView = findViewById(R.id.timer);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // Initialize Controller
        timerSettingsController = new TimerSettingsController(this);
        timerSettingsController.initializeView();
    }

    // --- View setters (called by Controller) ---
    public void setStudyMins(String mins) { studyMinutes.setText(mins); }
    public void setStudySecs(String secs) { studySeconds.setText(secs); }
    public void setBreakMins(String mins) { breakMinutes.setText(mins); }
    public void setBreakSecs(String secs) { breakSeconds.setText(secs); }
    public void setBreakEnabled(boolean enabled) { breakTimerSwitch.setChecked(enabled); }

    // --- View getters (called by Controller) ---
    public String getStudyMinsText() { return studyMinutes.getText().toString(); }
    public String getStudySecsText() { return studySeconds.getText().toString(); }
    public String getBreakMinsText() { return breakMinutes.getText().toString(); }
    public String getBreakSecsText() { return breakSeconds.getText().toString(); }
    public boolean isBreakTimerEnabled() {
        return breakTimerSwitch != null && breakTimerSwitch.isChecked();
    }
}
