package com.example.zone.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.example.zone.R;
import com.example.zone.controller.HeartRateSensorManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainContainerActivity extends AppCompatActivity {

    public static final String EXTRA_OPEN_TAB = "open_tab";
    private static final String STATE_SELECTED_TAB = "selected_tab";

    private ViewPager2 viewPager;
    private BottomNavigationView bottomNavigationView;
    private ImageView cloudFar;
    private ImageView cloudNear;
    private float screenDensity;
    private HeartRateSensorManager sensorManager;
    private boolean wellnessDialogShowing;
    private final HeartRateSensorManager.WellnessListener wellnessListener =
            this::showWellnessSuggestion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_container);
        sensorManager = HeartRateSensorManager.getInstance(getApplicationContext());

        screenDensity = getResources().getDisplayMetrics().density;
        viewPager = findViewById(R.id.mainViewPager);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        cloudFar = findViewById(R.id.cloudFar);
        cloudNear = findViewById(R.id.cloudNear);

        MainPagerAdapter adapter = new MainPagerAdapter(this);
        viewPager.setAdapter(adapter);

        int initialTab = savedInstanceState == null
                ? getIntent().getIntExtra(EXTRA_OPEN_TAB, 1)
                : savedInstanceState.getInt(STATE_SELECTED_TAB, 1);
        initialTab = Math.max(0, Math.min(3, initialTab));
        viewPager.setCurrentItem(initialTab, false);
        syncBottomNav(initialTab);
        updateTitle(initialTab);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                float scrollTotal = position + positionOffset;
                
                // Parallax logic: shift clouds based on scroll
                // - scrollTotal 1.0 (Timer page) is the baseline
                float offset = scrollTotal - 1.0f;
                
                if (cloudFar != null) {
                    cloudFar.setTranslationX(-offset * (100f * screenDensity));
                }
                if (cloudNear != null) {
                    cloudNear.setTranslationX(-offset * (250f * screenDensity));
                }
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateTitle(position);
                syncBottomNav(position);
            }
        });

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_calendar) {
                viewPager.setCurrentItem(0, true);
                return true;
            } else if (id == R.id.nav_timer) {
                viewPager.setCurrentItem(1, true);
                return true;
            } else if (id == R.id.nav_analytics) {
                viewPager.setCurrentItem(2, true);
                return true;
            } else if (id == R.id.nav_settings) {
                viewPager.setCurrentItem(3, true);
                return true;
            }
            return false;
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (viewPager.getCurrentItem() != 1) {
                    viewPager.setCurrentItem(1, true);
                } else {
                    moveTaskToBack(true);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        sensorManager.addWellnessListener(wellnessListener);
    }

    @Override
    protected void onStop() {
        sensorManager.removeWellnessListener(wellnessListener);
        super.onStop();
    }

    private void showWellnessSuggestion(String message) {
        if (isFinishing() || isDestroyed() || wellnessDialogShowing) {
            return;
        }
        wellnessDialogShowing = true;
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(R.string.heart_rate_check_in_title)
                .setMessage(message + "\n\n" + getString(R.string.wellness_not_medical_advice))
                .setPositiveButton(R.string.open_timer, (dialog, which) -> switchToTab(1))
                .setNegativeButton(R.string.dismiss, null)
                .setOnDismissListener(dialog -> wellnessDialogShowing = false)
                .show();
    }

    private void syncBottomNav(int position) {
        switch (position) {
            case 0: bottomNavigationView.setSelectedItemId(R.id.nav_calendar); break;
            case 1: bottomNavigationView.setSelectedItemId(R.id.nav_timer); break;
            case 2: bottomNavigationView.setSelectedItemId(R.id.nav_analytics); break;
            case 3: bottomNavigationView.setSelectedItemId(R.id.nav_settings); break;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        switchToTab(intent.getIntExtra(EXTRA_OPEN_TAB, viewPager.getCurrentItem()));
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(STATE_SELECTED_TAB, viewPager.getCurrentItem());
        super.onSaveInstanceState(outState);
    }

    private void updateTitle(int position) {
        if (getSupportActionBar() == null) return;
        switch (position) {
            case 0:
                getSupportActionBar().setTitle("Calendar & Objectives");
                break;
            case 1:
                getSupportActionBar().setTitle("Zone - Timer");
                break;
            case 2:
                getSupportActionBar().setTitle("Analytics");
                break;
            case 3:
                getSupportActionBar().setTitle("Settings");
                break;
        }
    }

    public void switchToTab(int index) {
        if (viewPager != null) {
            viewPager.setCurrentItem(index, true);
        }
    }

    private static class MainPagerAdapter extends FragmentStateAdapter {

        public MainPagerAdapter(@NonNull AppCompatActivity activity) {
            super(activity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new ObjectiveView();
                case 1:
                    return new MainView();
                case 2:
                    return new AnalyticsView();
                case 3:
                    return new SettingsView();
                default:
                    return new MainView();
            }
        }

        @Override
        public int getItemCount() {
            return 4;
        }
    }
}
