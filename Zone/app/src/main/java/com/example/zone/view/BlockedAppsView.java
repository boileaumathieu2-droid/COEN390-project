package com.example.zone.view;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zone.R;
import com.example.zone.model.BlockedAppsStore;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class BlockedAppsView extends AppCompatActivity {
    private final List<AppEntry> apps = new ArrayList<>();
    private AppsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_blocked_apps);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.manage_blocked_apps);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ListView appList = findViewById(R.id.availableAppsList);
        TextView emptyText = findViewById(R.id.emptyAppsText);
        adapter = new AppsAdapter();
        appList.setAdapter(adapter);
        appList.setEmptyView(emptyText);
        loadLaunchableApps();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void loadLaunchableApps() {
        PackageManager packageManager = getPackageManager();
        Intent launcherIntent = new Intent(Intent.ACTION_MAIN);
        launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolvedApps = packageManager.queryIntentActivities(launcherIntent, 0);
        Set<String> seenPackages = new HashSet<>();
        apps.clear();
        for (ResolveInfo info : resolvedApps) {
            if (info.activityInfo == null) {
                continue;
            }
            String packageName = info.activityInfo.packageName;
            if (getPackageName().equals(packageName) || !seenPackages.add(packageName)) {
                continue;
            }
            CharSequence label = info.loadLabel(packageManager);
            apps.add(new AppEntry(
                    label == null ? packageName : label.toString(),
                    packageName,
                    info.loadIcon(packageManager)
            ));
        }
        apps.sort(Comparator.comparing(entry -> entry.label.toLowerCase(Locale.ROOT)));
        adapter.notifyDataSetChanged();
    }

    private final class AppsAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return apps.size();
        }

        @Override
        public AppEntry getItem(int position) {
            return apps.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(BlockedAppsView.this)
                        .inflate(R.layout.blocked_app_row, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            AppEntry entry = getItem(position);
            holder.icon.setImageDrawable(entry.icon);
            holder.label.setText(entry.label);
            holder.packageName.setText(getString(R.string.blocked_app_package, entry.packageName));
            holder.toggle.setOnCheckedChangeListener(null);
            holder.toggle.setChecked(BlockedAppsStore.isBlocked(
                    BlockedAppsView.this, entry.packageName));
            holder.toggle.setOnCheckedChangeListener((button, checked) ->
                    BlockedAppsStore.setBlocked(
                            BlockedAppsView.this, entry.packageName, checked));
            SwitchMaterial rowToggle = holder.toggle;
            convertView.setOnClickListener(view -> rowToggle.performClick());
            return convertView;
        }
    }

    private static final class ViewHolder {
        final ImageView icon;
        final TextView label;
        final TextView packageName;
        final SwitchMaterial toggle;

        ViewHolder(View view) {
            icon = view.findViewById(R.id.appIcon);
            label = view.findViewById(R.id.appLabel);
            packageName = view.findViewById(R.id.appPackage);
            toggle = view.findViewById(R.id.appBlockedToggle);
        }
    }

    private static final class AppEntry {
        final String label;
        final String packageName;
        final Drawable icon;

        AppEntry(String label, String packageName, Drawable icon) {
            this.label = label;
            this.packageName = packageName;
            this.icon = icon;
        }
    }
}
