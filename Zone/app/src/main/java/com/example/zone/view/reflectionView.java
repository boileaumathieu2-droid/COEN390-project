package com.example.zone.view;

import static android.app.ProgressDialog.show;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zone.R;

import java.util.concurrent.atomic.AtomicBoolean;

public class reflectionView extends AppCompatActivity {

   private EditText ratingInput;
   private boolean objectiveSelected;
   private boolean objective;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.reflection_popup);

      EditText ratingInput = findViewById(R.id.editTextNumber);
      Button noButton = findViewById(R.id.btnNo);
      Button yesButton = findViewById(R.id.btnYes);
      Button extendSessionButton = findViewById(R.id.extendSeshButton);
      Button finishSessionButton = findViewById(R.id.finishSeshButton);

      noButton.setOnClickListener(v -> {
         objective = false;
         objectiveSelected = true;
      });
      yesButton.setOnClickListener(v -> {
         objective = true;
         objectiveSelected = true;
      });
      extendSessionButton.setOnClickListener(v -> {
         Intent intent = new Intent(this, MainView.class);
         intent.putExtra("Countdown", true);
         startActivity(intent);
      });
      finishSessionButton.setOnClickListener(v -> {
         if (!objectiveSelected) {
            showPopup();
            return;
         }
         int rating;
         try {
            rating = Integer.parseInt(ratingInput.getText().toString().trim());
         } catch (NumberFormatException e) {
            showPopup();
            return;
         }
         if (!validateRating(rating)) {
            showPopup();
            return;
         }
         Intent intent = new Intent(this, MainView.class);
         intent.putExtra("objective", objective);
         intent.putExtra("rating", rating);
         intent.putExtra("complete", true);

         startActivity(intent);
         finish();
      });
   }

   private boolean validateRating(int rating) {
      return rating >= 0 && rating <= 10;
   }

   private void showPopup() {
      new AlertDialog.Builder(this)
              .setTitle("Warning")
              .setMessage("you need to fill out the required fields")
              .setPositiveButton("Dimiss", (dialog, which) -> {
                 dialog.dismiss();

              })
              .show();
   }

   boolean allowDestroy = false;

   @Override
   public void onBackPressed() {

      new AlertDialog.Builder(this)
              .setTitle("Warning")
              .setMessage("Going back without filling out the table means your session will not be logged. Are you sure?")
              .setPositiveButton("Yes", (dialog, which) -> {
                 super.onBackPressed();
              })
              .setNegativeButton("No", (dialog, which) -> {
                 dialog.dismiss();
              })
              .show();
   }
}