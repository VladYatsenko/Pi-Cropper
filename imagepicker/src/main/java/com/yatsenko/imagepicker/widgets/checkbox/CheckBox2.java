package com.yatsenko.imagepicker.widgets.checkbox;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.CheckBox;

public class CheckBox2 extends View {

    private CheckBoxBase checkBoxBase;

    public CheckBox2(Context context, AttributeSet attrs) {
        super(context, attrs);
        checkBoxBase = new CheckBoxBase(this, 24);
        setDrawBackgroundAsArc(11);
    }

    public void setProgressDelegate(CheckBoxBase.ProgressDelegate delegate) {
        checkBoxBase.setProgressDelegate(delegate);
    }

    public void setChecked(int num, boolean checked, boolean animated) {
        checkBoxBase.setChecked(num, checked, animated);
    }

    public void setChecked(boolean checked, boolean animated) {
        checkBoxBase.setChecked(checked, animated);
    }

    public void setNum(int num) {
        checkBoxBase.setNum(num);
    }

    public boolean isChecked() {
        return checkBoxBase.isChecked();
    }

    public void setColor(String background, String background2, String check) {
        checkBoxBase.setColor(background, background2, check);
    }

    @Override
    public void setEnabled(boolean enabled) {
        checkBoxBase.setEnabled(enabled);
        super.setEnabled(enabled);
    }

    public void setDrawBackgroundAsArc(int type) {
        checkBoxBase.setBackgroundType(type);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        checkBoxBase.onAttachedToWindow();
    }

    public void setDuration(long duration) {
        checkBoxBase.animationDuration = duration;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        checkBoxBase.onDetachedFromWindow();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        checkBoxBase.setBounds(0, 0, right - left, bottom - top);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        checkBoxBase.draw(canvas);
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(CheckBox.class.getName());
        info.setChecked(isChecked());
        info.setCheckable(true);
    }
}