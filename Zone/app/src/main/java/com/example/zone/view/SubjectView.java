package com.example.zone.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zone.R;
import com.example.zone.controller.SubjectController;
import com.example.zone.model.Database;
import com.example.zone.model.GradeAdapter;

import java.util.ArrayList;

public class SubjectView extends AppCompatActivity {

    private SubjectController controller;
    private String subjectName;
    private int subjectID;
    private ListView gradesList;
    private ArrayList<String> subjectGrades;

    private GradeAdapter adapter;

    private void refresh() {
        subjectGrades.clear();
        subjectGrades.addAll(controller.getGrades(subjectID));
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_menu1, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem option) {

        int id = option.getItemId();

        if (id == R.id.action_delete_subject) {
            controller.deleteSubject(subjectID);
            Intent intent = new Intent(this, GradesTrackerView.class);
            startActivity(intent);
            Toast.makeText(
                    getApplicationContext(),
                    "Subject deleted",
                    Toast.LENGTH_SHORT
            ).show();
        }

        return super.onOptionsItemSelected(option);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subject_page);


        controller = new SubjectController(new Database(this));
        subjectName = getIntent().getStringExtra("subjectName");
        subjectID = getIntent().getIntExtra("subjectID", -1);
        Button newGrade = findViewById(R.id.newGradeButton);
        gradesList = findViewById(R.id.gradeList);

        subjectGrades = new ArrayList<>();

        adapter = new GradeAdapter(
                this,
                subjectGrades
        );

        gradesList.setAdapter(adapter);

        refresh();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(subjectName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        newGrade.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(SubjectView.this);
            View popupView = getLayoutInflater().inflate(R.layout.add_grade_popup, null);
            builder.setView(popupView);
            AlertDialog dialog = builder.create();
            Button cancel = popupView.findViewById(R.id.buttonCancel);
            Button save = popupView.findViewById(R.id.buttonSave);
            EditText userGrade = popupView.findViewById(R.id.gradeEditText);
            cancel.setOnClickListener(x -> dialog.dismiss());
            save.setOnClickListener(View -> {
                String grade = userGrade.getText().toString();

                if (grade.isEmpty()) {
                    userGrade.setError("Enter a grade");
                    return;
                }
                else {
                    controller.addGrade(subjectID, grade);

                }
                refresh();
                dialog.dismiss();
            });
            dialog.show();
        });


    }

}
