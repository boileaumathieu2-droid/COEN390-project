package com.example.zone.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zone.R;
import com.example.zone.controller.GradesTracker;
import com.example.zone.model.Database;
import com.example.zone.model.Session;
import com.example.zone.model.Subject;
import com.example.zone.model.SubjectAdapter;
import com.example.zone.model.VirtualDatabase;

import java.util.ArrayList;

public class GradesTrackerView extends AppCompatActivity {

    private GradesTracker controller;
    private ListView subjectList;
    private TextView noSubject;
    private ArrayList<Subject> subjects;
    private SubjectAdapter adapter;
    private VirtualDatabase db = new VirtualDatabase();

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.action_menu, menu);
//        return true;}
//    public boolean onOptionsItemSelected(MenuItem option) {
//        int id = option.getItemId();
//        if (id == R.id.action_settings) {
//            Intent intent = new Intent(GradesTrackerView.this, SettingsView.class);
//            startActivity(intent);
//        }
//        return super.onOptionsItemSelected(option);
//    }
    private void refresh() {
        db.getSubjects(subjectList -> {
            subjects.clear();
            subjects.addAll(subjectList);

            adapter.notifyDataSetChanged();

            if (subjects.isEmpty()) {
                noSubject.setVisibility(View.VISIBLE);
            } else {
                noSubject.setVisibility(View.GONE);
            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Session.init(getApplicationContext());
        setContentView(R.layout.grades_tracker);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Grades Tracker");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        noSubject = findViewById(R.id.noSubjectsTextView);
        subjectList = findViewById(R.id.subjectListView);
        subjects = new ArrayList<>();
        adapter = new SubjectAdapter(this, subjects);
        subjectList.setAdapter(adapter);
        Button newSubject = findViewById(R.id.newSubjectButton);
        controller = new GradesTracker(new Database(this));
        refresh();
        newSubject.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(GradesTrackerView.this);
            View popupView = getLayoutInflater().inflate(R.layout.add_subject_popup, null);
            builder.setView(popupView);
            AlertDialog dialog = builder.create();
            Button cancel = popupView.findViewById(R.id.buttonCancel);
            Button save = popupView.findViewById(R.id.buttonSave);
            EditText subjectName = popupView.findViewById(R.id.subjectEditText);
            cancel.setOnClickListener(x -> dialog.dismiss());
            save.setOnClickListener(View -> {
                String name = subjectName.getText().toString().trim();
                if (name.isEmpty()) {
                    subjectName.setError("Error: Input a valid subject name");
                    return;
                }
                db.checkIfSubjectExists(name, exists -> {
                    if (exists) {
                        Toast.makeText(
                                GradesTrackerView.this,
                                "Subject already exists",
                                Toast.LENGTH_SHORT
                        ).show();
                    } else {
                        db.saveSubject(name);
                        Toast.makeText(
                                GradesTrackerView.this,
                                "Subject saved",
                                Toast.LENGTH_SHORT
                        ).show();
                        refresh();
                        dialog.dismiss();
                    }
                });
            });
            dialog.show();
        });
    }
}

