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
import com.example.zone.controller.SubjectController;
import com.example.zone.model.Database;
import com.example.zone.model.GradeAdapter;
import com.example.zone.model.VirtualDatabase;

import java.util.ArrayList;

public class SubjectView extends AppCompatActivity {

    private SubjectController controller;
    private String subjectName;
    private String subjectID;
    private ListView gradesList;
    private TextView noGrades;
    private ArrayList<String> subjectGrades;

    private GradeAdapter adapter;

    private void refresh() {
        VirtualDatabase db = new VirtualDatabase();

        db.getGrades(grades -> {
            subjectGrades.clear();
            subjectGrades.addAll(grades);

            adapter.notifyDataSetChanged();

            if (subjectGrades.isEmpty()) {
                noGrades.setVisibility(View.VISIBLE);
            } else {
                noGrades.setVisibility(View.GONE);
            }

        }, subjectID);
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
            new AlertDialog.Builder(this)
                    .setTitle("Delete this subject?")
                    .setMessage("The subject and its grades will be permanently deleted.")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        VirtualDatabase db = new VirtualDatabase();
                        db.deleteSubject(subjectID, success -> {
                            if (success) {
                                setResult(RESULT_OK);
                                Toast.makeText(this, "Subject deleted", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(this, "Subject could not be deleted", Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        }

        return super.onOptionsItemSelected(option);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subject_page);
        controller = new SubjectController(new Database(this));

        subjectName = getIntent().getStringExtra("subjectName");
        subjectID = getIntent().getStringExtra("subjectID");
        Button newGrade = findViewById(R.id.newGradeButton);
        gradesList = findViewById(R.id.gradeList);
        noGrades = findViewById(R.id.noGradesTextView);
        VirtualDatabase db = new VirtualDatabase();
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
                String grade = userGrade.getText().toString().trim();
                if (grade.isEmpty()) {
                    userGrade.setError("Enter a grade");
                    return;
                }
                if (!SubjectController.isGradeInRange(grade)) {
                    userGrade.setError("Grade must be a number from 0 to 100");
                    return;
                }
                db.saveGrade("", grade, subjectID, success -> {
                    if (success) {
                        refresh();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(
                                SubjectView.this,
                                "Grade could not be saved",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
            });
            dialog.show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            refresh();
        }
    }
}

