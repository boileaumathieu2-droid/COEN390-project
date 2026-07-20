package com.example.zone.model;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

/** Simple list adapter for the grade strings displayed on a subject page. */
public class GradeAdapter extends ArrayAdapter<String> {
    public GradeAdapter(Context context, ArrayList<String> grades) {
        super(context, android.R.layout.simple_list_item_1, grades);
    }
}
