package com.example.zone.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zone.R;
import com.example.zone.model.StudySessionModel;
import com.example.zone.model.TimerModel;
import com.example.zone.model.VirtualDatabase;
import com.google.android.material.button.MaterialButton;

public class reflectionView extends AppCompatActivity {

    private final TimerModel timer = TimerModel.getInstance();
    private EditText ratingInput;
    private MaterialButton btnYes;
    private MaterialButton btnNo;
    private boolean objectiveSelected;
    private boolean objectiveMet;
    private boolean saving;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reflection_popup);

        ratingInput = findViewById(R.id.editTextNumber);
        btnNo = findViewById(R.id.btnNo);
        btnYes = findViewById(R.id.btnYes);
        Button extendButton = findViewById(R.id.extendSeshButton);
        Button finishButton = findViewById(R.id.finishSeshButton);

        btnNo.setOnClickListener(view -> {
            objectiveMet = false;
            objectiveSelected = true;
            updateSelectionUI();
        });
        btnYes.setOnClickListener(view -> {
            objectiveMet = true;
            objectiveSelected = true;
            updateSelectionUI();
        });

        extendButton.setOnClickListener(view -> {
            timer.discardLastCompletedSession();
            timer.startNewStudySession();
            returnToMainWithAction("Countdown");
        });

        finishButton.setOnClickListener(view -> {
            if (saving) {
                return;
            }
            Integer rating = readRating();
            if (!objectiveSelected || rating == null) {
                showMissingFieldsMessage();
                return;
            }

            StudySessionModel completed = timer.applyReflection(
                    objectiveMet,
                    rating
            );
            if (completed == null) {
                Toast.makeText(
                        this,
                        "No completed session was found",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            saving = true;
            finishButton.setEnabled(false);
            new VirtualDatabase().saveStudySession(completed, success ->
                    runOnUiThread(() -> {
                        saving = false;
                        finishButton.setEnabled(true);
                        if (success) {
                            timer.discardLastCompletedSession();
                            Toast.makeText(
                                    this,
                                    "Session saved to History",
                                    Toast.LENGTH_SHORT
                            ).show();
                            returnToMain();
                        } else {
                            new AlertDialog.Builder(this)
                                    .setTitle("Could not save session")
                                    .setMessage("Check that you are signed in and connected to the internet, then try again.")
                                    .setPositiveButton("OK", null)
                                    .show();
                        }
                    })
            );
        });

        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        confirmDiscard();
                    }
                }
        );
    }

    private void updateSelectionUI() {
      if (!objectiveSelected) return;

      if (objectiveMet) {
         btnYes.setStrokeWidth(10);
         btnNo.setStrokeWidth(0);
      } else {
         btnYes.setStrokeWidth(0);
         btnNo.setStrokeWidth(10);
      }
   }

   private Integer readRating() {
        try {
            int rating = Integer.parseInt(ratingInput.getText().toString().trim());
            return rating >= 0 && rating <= 10 ? rating : null;
        } catch (NumberFormatException error) {
            return null;
        }
    }

    private void showMissingFieldsMessage() {
        new AlertDialog.Builder(this)
                .setTitle("Required fields")
                .setMessage("Select whether you met your objective and enter a rating from 0 to 10.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void confirmDiscard() {
        new AlertDialog.Builder(this)
                .setTitle("Discard this session?")
                .setMessage("If you leave now, this session will not be added to History.")
                .setPositiveButton("Discard", (dialog, which) -> {
                    timer.discardLastCompletedSession();
                    setResult(RESULT_CANCELED);
                    finish();
                })
                .setNegativeButton("Stay", null)
                .show();
    }

    private void returnToMain() {
        Intent intent = new Intent(this, MainContainerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void returnToMainWithAction(String action) {
        Intent intent = new Intent(this, MainContainerActivity.class);
        intent.putExtra(action, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
