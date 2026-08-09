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
import com.example.zone.model.GradeAdapter;
import com.example.zone.model.VirtualDatabase;

import java.util.ArrayList;

public class SubjectView extends AppCompatActivity {

    private String subjectName;
    private String subjectID;
    private ListView gradesList;
    private TextView noGrades;
    private ArrayList<String> subjectGrades;
    private ArrayList<VirtualDatabase.GradeRecord> gradeRecords;

    private GradeAdapter adapter;

    private void refresh() {
        VirtualDatabase db = new VirtualDatabase();

        db.getGradeRecords(records -> {
            gradeRecords.clear();
            gradeRecords.addAll(records);
            subjectGrades.clear();
            for (VirtualDatabase.GradeRecord record : records) {
                subjectGrades.add(record.getGrade());
            }

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
        subjectName = getIntent().getStringExtra("subjectName");
        subjectID = getIntent().getStringExtra("subjectID");
        Button newGrade = findViewById(R.id.newGradeButton);
        gradesList = findViewById(R.id.gradeList);
        noGrades = findViewById(R.id.noGradesTextView);
        VirtualDatabase db = new VirtualDatabase();
        subjectGrades = new ArrayList<>();
        gradeRecords = new ArrayList<>();


        adapter = new GradeAdapter(
                this,
                subjectGrades
        );
        gradesList.setAdapter(adapter);
        gradesList.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < gradeRecords.size()) {
                showGradeActions(gradeRecords.get(position));
            }
        });
        refresh();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(subjectName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        newGrade.setOnClickListener(view -> showGradeEditor(null));
    }

    private void showGradeActions(VirtualDatabase.GradeRecord record) {
        new AlertDialog.Builder(this)
                .setTitle(record.getGrade() + "%")
                .setItems(new String[]{"Edit grade", "Delete grade"}, (dialog, which) -> {
                    if (which == 0) {
                        showGradeEditor(record);
                    } else {
                        confirmDeleteGrade(record);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showGradeEditor(VirtualDatabase.GradeRecord existingGrade) {
        View popupView = getLayoutInflater().inflate(R.layout.add_grade_popup, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(existingGrade == null ? "Add grade" : "Edit grade")
                .setView(popupView)
                .create();
        Button cancel = popupView.findViewById(R.id.buttonCancel);
        Button save = popupView.findViewById(R.id.buttonSave);
        EditText userGrade = popupView.findViewById(R.id.gradeEditText);
        if (existingGrade != null) {
            userGrade.setText(existingGrade.getGrade());
            userGrade.setSelection(userGrade.length());
        }
        cancel.setOnClickListener(view -> dialog.dismiss());
        save.setOnClickListener(view -> {
            String normalizedGrade = SubjectController.normalizeGrade(
                    userGrade.getText().toString()
            );
            if (normalizedGrade == null) {
                userGrade.setError("Grade must be a number from 0 to 100");
                return;
            }

            VirtualDatabase database = new VirtualDatabase();
            if (existingGrade == null) {
                database.saveGrade("", normalizedGrade, subjectID, success ->
                        finishGradeChange(success, dialog, "Grade could not be saved"));
            } else {
                database.editGrade(
                        success -> finishGradeChange(
                                success,
                                dialog,
                                "Grade could not be updated"
                        ),
                        subjectID,
                        existingGrade.getId(),
                        normalizedGrade,
                        existingGrade.getType()
                );
            }
        });
        dialog.show();
    }

    private void confirmDeleteGrade(VirtualDatabase.GradeRecord record) {
        new AlertDialog.Builder(this)
                .setTitle("Delete this grade?")
                .setMessage(record.getGrade() + "% will be permanently deleted.")
                .setPositiveButton("Delete", (dialog, which) ->
                        new VirtualDatabase().deleteGrade(
                                subjectID,
                                record.getId(),
                                success -> finishGradeChange(
                                        success,
                                        null,
                                        "Grade could not be deleted"
                                )
                        ))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void finishGradeChange(
            boolean success,
            AlertDialog dialog,
            String errorMessage
    ) {
        if (success) {
            if (dialog != null) {
                dialog.dismiss();
            }
            refresh();
        } else {
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            refresh();
        }
    }
}

