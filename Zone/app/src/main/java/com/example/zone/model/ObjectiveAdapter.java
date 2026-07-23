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

public class ObjectiveAdapter extends ArrayAdapter<Objective> {

    public ObjectiveAdapter(Context context, ArrayList<Objective> objectives) {
        super(context, 0, objectives);
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.subject_row, parent, false);
        }

        Objective objective = getItem(position);

        TextView objectiveText = convertView.findViewById(R.id.subjectName);

        if (objective != null) {

            String display = objective.getObjectiveDate() + " - " + objective.getEventName();

            objectiveText.setText(display);
        }

        return convertView;
    }
}
