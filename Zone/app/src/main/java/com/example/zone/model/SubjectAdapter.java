package com.example.zone.model;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.zone.R;
import com.example.zone.controller.SubjectController;
import com.example.zone.view.SubjectView;

import java.util.ArrayList;

public class SubjectAdapter extends ArrayAdapter<Subject> {

    private final SubjectController controller;

    public SubjectAdapter(Context context, ArrayList<Subject> subjects) {
        super(context, 0, subjects);
        // Initialize controller once in constructor rather than inside getView()
        this.controller = new SubjectController(new Database(context));
    }


    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.subject_row1, parent, false);
        }

        TextView subjectNameTextView = convertView.findViewById(R.id.subjectName);
        ImageView deleteButton = convertView.findViewById(R.id.deleteSubjectButton);

        Subject currentSubject = getItem(position);

        if (currentSubject != null) {
            subjectNameTextView.setText(currentSubject.getSubjectName());

            subjectNameTextView.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), SubjectView.class);
                intent.putExtra("subjectName", currentSubject.getSubjectName());
                intent.putExtra("subjectID", currentSubject.getSubjectID());
                getContext().startActivity(intent);
            });

            deleteButton.setOnClickListener(v -> {

                controller.deleteSubject(currentSubject.getSubjectID());

                remove(currentSubject);

                notifyDataSetChanged();
            });
        if (subject != null) {
            System.out.println("Subject ID: " + subject.getSubjectID());
            System.out.println("Subject Name: " + subject.getSubjectName());

            text.setText(subject.getSubjectName());
        }

        return convertView;
    }
}
