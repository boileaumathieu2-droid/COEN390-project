package com.example.zone.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zone.R;
import com.example.zone.controller.GradesTracker;
import com.example.zone.controller.Login;
import com.example.zone.model.Database;
import com.example.zone.model.Session;
import com.example.zone.model.Subject;
import com.example.zone.model.SubjectAdapter;

import java.util.ArrayList;

public class GradesTrackerView extends AppCompatActivity {

    private GradesTracker controller;
    private ListView subjectList;
    private ArrayList<Subject> subjects;
    private SubjectAdapter adapter;

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
            Intent intent = new Intent(GradesTrackerView.this, SettingsView.class);
            startActivity(intent);

        }

        return super.onOptionsItemSelected(option);
    }
    private void  refresh() {
        subjects.clear();
        subjects.addAll(controller.getSubjects(Session.getUserID()));
        adapter.notifyDataSetChanged();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.grades_tracker);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Grades Tracker");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        subjectList = findViewById(R.id.subjectListView);
        subjects = new ArrayList<>();
        adapter = new SubjectAdapter(this, subjects);
        subjectList.setAdapter(adapter);
        Button newSubject = findViewById(R.id.newSubjectButton);
        controller = new GradesTracker(new Database(this));
        refresh();
        subjectList.setOnItemClickListener((parent, view, position, id) -> {

            Subject selectedSubject = subjects.get(position);

            Intent intent = new Intent(
                    GradesTrackerView.this,
                    SubjectView.class
            );

            intent.putExtra("subjectName", selectedSubject.getSubjectName());
            intent.putExtra("subjectID", selectedSubject.getSubjectID());

            startActivity(intent);
        });
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

                String name = subjectName.getText().toString();

                if (name.isEmpty()) {
                    subjectName.setError("Error: Input a valid subject name");
                }
                else if (controller.subjectAlreadyExists(
                        Session.getUserID(),
                        name
                )) {
                    subjectName.setError(
                            "Error: You already have a subject called " + name
                    );
                }
                else {
                    controller.addSubject(
                            name,
                            Session.getUserID()
                    );

                    refresh();
                    dialog.dismiss();
                }
            });
            dialog.show();
        });

    }

}
