package com.example.zone.view;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zone.R;

/** Displays a heart-rate check-in above any screen that is currently open. */
public final class HeartRateWellnessAlertActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "wellness_message";

    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showSuggestion(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        showSuggestion(intent);
    }

    private void showSuggestion(Intent intent) {
        String message = intent == null ? null : intent.getStringExtra(EXTRA_MESSAGE);
        if (message == null || message.trim().isEmpty()) {
            finish();
            return;
        }

        if (alertDialog != null) {
            alertDialog.dismiss();
        }

        alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.heart_rate_check_in_title)
                .setMessage(message + "\n\n"
                        + getString(R.string.wellness_not_medical_advice))
                .setPositiveButton(R.string.open_timer, (dialog, which) -> openTimer())
                .setNegativeButton(R.string.dismiss, (dialog, which) -> finish())
                .setOnCancelListener(dialog -> finish())
                .create();
        alertDialog.setOnDismissListener(dialog -> {
            if (!isFinishing()) {
                finish();
            }
        });
        alertDialog.show();
    }

    private void openTimer() {
        Intent intent = new Intent(this, MainContainerActivity.class);
        intent.putExtra(MainContainerActivity.EXTRA_OPEN_TAB, 1);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        if (alertDialog != null) {
            alertDialog.setOnDismissListener(null);
            alertDialog.dismiss();
            alertDialog = null;
        }
        super.onDestroy();
    }
}
