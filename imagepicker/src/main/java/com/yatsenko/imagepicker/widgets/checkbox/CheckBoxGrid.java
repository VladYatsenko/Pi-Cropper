package com.yatsenko.imagepicker.widgets.checkbox;

import android.content.Context;
import android.util.AttributeSet;

public class CheckBoxGrid extends CheckBox2 {

    public CheckBoxGrid(Context context, AttributeSet attrs) {
        super(context, attrs, 24);
        setDrawBackgroundAsArc(11);//CheckBoxBase.GRID);
    }

}
