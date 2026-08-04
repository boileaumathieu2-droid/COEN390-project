package com.example.zone.view;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.example.zone.R;

public class MainContainerActivity extends AppCompatActivity {

    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_container);

        viewPager = findViewById(R.id.mainViewPager);
        MainPagerAdapter adapter = new MainPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Set MainView (index 1) as the default page
        viewPager.setCurrentItem(1, false);
        updateTitle(1);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateTitle(position);
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
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
