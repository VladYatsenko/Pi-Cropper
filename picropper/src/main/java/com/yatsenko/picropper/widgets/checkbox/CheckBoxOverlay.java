package com.yatsenko.picropper.widgets.checkbox;

import android.content.Context;
import android.util.AttributeSet;

public class CheckBoxOverlay extends CheckBox2 {

    public CheckBoxOverlay(Context context, AttributeSet attrs) {
        super(context, attrs, 28);
        setDrawBackgroundAsArc(OVERLAY);
    }

}
