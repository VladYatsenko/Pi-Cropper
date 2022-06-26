package com.yatsenko.imagepicker.widgets.checkbox;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextPaint;
import android.view.View;

import androidx.annotation.Keep;
import androidx.core.content.ContextCompat;

import com.yatsenko.imagepicker.R;
import com.yatsenko.imagepicker.utils.AndroidUtilities;
import com.yatsenko.imagepicker.utils.extensions.ViewKt;

public class CheckBoxBase {

    private View parentView;
    private Rect bounds = new Rect();

    private static Paint paint;
    private static Paint eraser;
    private Paint checkPaint;
    private Paint backgroundPaint;
    private TextPaint textPaint;

    private Path path = new Path();

    private Bitmap drawBitmap;
    private Canvas bitmapCanvas;

    private boolean enabled = true;

    private boolean attachedToWindow;

    private float backgroundAlpha = 1.0f;

    private float progress;
    private ObjectAnimator checkAnimator;

    private boolean isChecked;

    private String checkColorKey = "checkColorKey";
    private String backgroundColorKey = "backgroundColorKey";
    private String background2ColorKey = "background2ColorKey";

    private boolean useDefaultCheck;

    private boolean drawUnchecked = true;
    private int backgroundType;

    private float size;

    private String checkedText;

    private ProgressDelegate progressDelegate;

    public interface ProgressDelegate {
        void setProgress(float progress);
    }

    public CheckBoxBase(View parent, int sz) {
        parentView = parent;
        size = sz;
        if (paint == null) {
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);

            eraser = new Paint(Paint.ANTI_ALIAS_FLAG);
            eraser.setColor(0);
            eraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        }
        checkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        checkPaint.setStrokeCap(Paint.Cap.ROUND);
        checkPaint.setStyle(Paint.Style.STROKE);
        checkPaint.setStrokeJoin(Paint.Join.ROUND);
        checkPaint.setStrokeWidth(ViewKt.dpToPxInt(1.9f));

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(ViewKt.dpToPxInt(1.2f));

        drawBitmap = Bitmap.createBitmap(ViewKt.dpToPxInt(size), ViewKt.dpToPxInt(size), Bitmap.Config.ARGB_4444);
        bitmapCanvas = new Canvas(drawBitmap);
    }

    public void onAttachedToWindow() {
        attachedToWindow = true;
    }

    public void onDetachedFromWindow() {
        attachedToWindow = false;
    }

    public void setBounds(int x, int y, int width, int height) {
        bounds.left = x;
        bounds.top = y;
        bounds.right = x + width;
        bounds.bottom = y + height;
    }

    public void setDrawUnchecked(boolean value) {
        drawUnchecked = value;
    }

    @Keep
    public void setProgress(float value) {
        if (progress == value) {
            return;
        }
        progress = value;
        invalidate();
        if (progressDelegate != null) {
            progressDelegate.setProgress(value);
        }
    }

    private void invalidate() {
        if (parentView.getParent() != null) {
            View parent = (View) parentView.getParent();
            parent.invalidate();
        }
        parentView.invalidate();
    }

    public void setProgressDelegate(ProgressDelegate delegate) {
        progressDelegate = delegate;
    }

    @Keep
    public float getProgress() {
        return progress;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setEnabled(boolean value) {
        enabled = value;
    }

    public void setBackgroundType(int type) {
        backgroundType = type;
        backgroundPaint.setStrokeWidth(ViewKt.dpToPxInt(1.5f));
    }

    private void cancelCheckAnimator() {
        if (checkAnimator != null) {
            checkAnimator.cancel();
            checkAnimator = null;
        }
    }

    public long animationDuration = 200;

    private void animateToCheckedState(boolean newCheckedState) {
        checkAnimator = ObjectAnimator.ofFloat(this, "progress", newCheckedState ? 1 : 0);
        checkAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (animation.equals(checkAnimator)) {
                    checkAnimator = null;
                }
                if (!isChecked) {
                    checkedText = null;
                }
            }
        });
        checkAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT);
        checkAnimator.setDuration(animationDuration);
        checkAnimator.start();
    }

    public void setColor(String background, String background2, String check) {
        backgroundColorKey = background;
        background2ColorKey = background2;
        checkColorKey = check;
        invalidate();
    }

    public void setNum(int num) {
        if (num >= 0) {
            checkedText = "" + (num + 1);
        } else if (checkAnimator == null) {
            checkedText = null;
        }
        invalidate();
    }

    public void setChecked(boolean checked, boolean animated) {
        setChecked(-1, checked, animated);
    }

    public void setChecked(int num, boolean checked, boolean animated) {
        if (num >= 0) {
            checkedText = "" + (num + 1);
            invalidate();
        }
        if (checked == isChecked) {
            return;
        }
        isChecked = checked;

        if (attachedToWindow && animated) {
            animateToCheckedState(checked);
        } else {
            cancelCheckAnimator();
            setProgress(checked ? 1.0f : 0.0f);
        }
    }

    public void draw(Canvas canvas) {
        if (drawBitmap == null) {
            return;
        }

        drawBitmap.eraseColor(0);
        float rad = ViewKt.dpToPxInt(size / 2);

        float roundProgress = progress >= 0.5f ? 1.0f : progress / 0.5f;

        int cx = bounds.centerX();
        int cy = bounds.centerY();

        if (backgroundColorKey != null) {
            if (drawUnchecked) {
                paint.setColor(getThemedColor("key_transparent_bg")); //(Theme.getServiceMessageColor() & 0x00ffffff) | 0x28000000
                String color = isChecked ? "Theme.key_chat_serviceBackground;" : checkColorKey;
                backgroundPaint.setColor(getThemedColor(color));
            } else {
                backgroundPaint.setColor(AndroidUtilities.getOffsetColor(0x00ffffff, getThemedColor(background2ColorKey != null ? background2ColorKey : checkColorKey), progress, backgroundAlpha));
            }
        } else {
            if (drawUnchecked) {
                paint.setColor(Color.argb((int) (25 * backgroundAlpha), 0, 0, 0));
                backgroundPaint.setColor(AndroidUtilities.getOffsetColor(0xffffffff, getThemedColor(checkColorKey), progress, backgroundAlpha));
            } else {
                backgroundPaint.setColor(AndroidUtilities.getOffsetColor(0x00ffffff, getThemedColor(background2ColorKey != null ? background2ColorKey : checkColorKey), progress, backgroundAlpha));
            }
        }

        if (drawUnchecked && backgroundType >= 0) {
            canvas.drawCircle(cx, cy, rad, paint);
        }
        paint.setColor(getThemedColor(checkColorKey));
        canvas.drawCircle(cx, cy, rad, backgroundPaint);

        if (roundProgress > 0) {
            float checkProgress = progress < 0.5f ? 0.0f : (progress - 0.5f) / 0.5f;
            if (backgroundType == 11 || backgroundType == 6 || backgroundType == 7 || backgroundType == 10 || !drawUnchecked && backgroundColorKey != null) {
                paint.setColor(getThemedColor(backgroundColorKey));
            }
            if (!useDefaultCheck && checkColorKey != null) {
                checkPaint.setColor(getThemedColor(checkColorKey));
            } else {
                checkPaint.setColor(getThemedColor("Theme.key_checkboxCheck"));
            }

            rad -= ViewKt.dpToPxInt(0.5f);
            bitmapCanvas.drawCircle(drawBitmap.getWidth() / 2, drawBitmap.getHeight() / 2, rad, paint);
            bitmapCanvas.drawCircle(drawBitmap.getWidth() / 2, drawBitmap.getHeight() / 2, rad * (1.0f - roundProgress), eraser);
            canvas.drawBitmap(drawBitmap, cx - drawBitmap.getWidth() / 2, cy - drawBitmap.getHeight() / 2, null);

            if (checkProgress != 0) {
                if (checkedText != null) {
                    if (textPaint == null) {
                        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
                        textPaint.setTypeface(AndroidUtilities.getTypeface(parentView.getContext(), "fonts/rmedium.ttf"));
                    }
                    final float textSize, y;
                    switch (checkedText.length()) {
                        case 0:
                        case 1:
                        case 2:
                            textSize = 14f;
                            y = 18f;
                            break;
                        case 3:
                            textSize = 10f;
                            y = 16.5f;
                            break;
                        default:
                            textSize = 8f;
                            y = 15.75f;
                    }
                    textPaint.setTextSize(ViewKt.dpToPxInt(textSize));
                    textPaint.setColor(getThemedColor(checkColorKey));
                    canvas.save();
                    canvas.scale(checkProgress, 1.0f, cx, cy);
                    canvas.drawText(checkedText, cx - textPaint.measureText(checkedText) / 2f, ViewKt.dpToPxInt(y), textPaint);
                    canvas.restore();
                } else {
                    path.reset();
                    float scale = 1f;
                    if (backgroundType == -1) {
                        scale = 1.4f;
                    } else if (backgroundType == 5) {
                        scale = 0.8f;
                    }
                    float checkSide = ViewKt.dpToPxInt(9 * scale) * checkProgress;
                    float smallCheckSide = ViewKt.dpToPxInt(4 * scale) * checkProgress;
                    int x = cx - ViewKt.dpToPxInt(1.5f);
                    int y = cy + ViewKt.dpToPxInt(4);
                    float side = (float) Math.sqrt(smallCheckSide * smallCheckSide / 2.0f);
                    path.moveTo(x - side, y - side);
                    path.lineTo(x, y);
                    side = (float) Math.sqrt(checkSide * checkSide / 2.0f);
                    path.lineTo(x + side, y - side);
                    canvas.drawPath(path, checkPaint);
                }
            }
        }
    }

    private int getThemedColor(String key) {
        int res;
        switch (key) {
            case ("checkColorKey"):
                res = android.R.color.white;
                break;
            case ("backgroundColorKey"):
                res = R.color.cerulean;
                break;
            case ("Theme.key_chat_serviceBackground;"):
                res = R.color.pickerColorPrimary;
                break;
            case ("background2ColorKey"):
                res = android.R.color.transparent;
                break;
            case ("Theme.key_checkboxCheck"):
                res = R.color.default_corner_color;
                break;
            case ("key_transparent_bg"):
                res = R.color.transparent_gray;
                break;
            default:
                res = android.R.color.white;
                break;
        }
        return ContextCompat.getColor(parentView.getContext(), res);
    }
}
