package com.example.zone.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.zone.R;

import java.util.ArrayList;

public class MainViewObjectiveAdapter extends ArrayAdapter<Objective> {

    public MainViewObjectiveAdapter(Context context, ArrayList<Objective> objectives) {
        super(context, 0, objectives);
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.daily_objective_row, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Objective objective = getItem(position);
        if (objective != null) {
            holder.title.setText(objective.getEventName());
            String time = objective.getCompletionTime().trim();
            holder.details.setText(time.isEmpty()
                    ? objective.getTaskType()
                    : objective.getTaskType() + " • " + time + " min");
        }
        return convertView;
    }

    private static final class ViewHolder {
        final TextView title;
        final TextView details;

        ViewHolder(View view) {
            title = view.findViewById(R.id.dailyObjectiveTitle);
            details = view.findViewById(R.id.dailyObjectiveDetails);
        }
    }
}
