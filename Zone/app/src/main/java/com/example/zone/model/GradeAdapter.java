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

public class GradeAdapter extends ArrayAdapter<String> {

    public GradeAdapter(Context context, ArrayList<String> grades) {
        super(context, 0, grades);
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.subject_row, parent, false);
        }

        String grade = getItem(position);

        TextView text = convertView.findViewById(R.id.subjectName);

        if (grade != null) {
            text.setText(grade);
        }

        return convertView;
    }
}
