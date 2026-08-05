package com.example.zone.model;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class ListViewCustom extends ListView {

    private int maxHeight = 900;

    public ListViewCustom(Context context) {
        super(context);
    }

    public ListViewCustom(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ListViewCustom(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightSpec = MeasureSpec.makeMeasureSpec(
                maxHeight,
                MeasureSpec.AT_MOST
        );

        super.onMeasure(widthMeasureSpec, heightSpec);
    }

}
