package com.yatsenko.imagepicker.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;

public class AndroidUtilities {

    public static Typeface getTypeface(Context context, String assetPath) {
        try {
            Typeface t;
            if (Build.VERSION.SDK_INT >= 26) {
                Typeface.Builder builder = new Typeface.Builder(context.getAssets(), assetPath);
                if (assetPath.contains("medium")) {
                    builder.setWeight(700);
                }
                if (assetPath.contains("italic")) {
                    builder.setItalic(true);
                }
                t = builder.build();
            } else {
                t = Typeface.createFromAsset(context.getAssets(), assetPath);
            }
            return t;
        } catch (Exception e) {
            return null;
        }
    }

    public static int getOffsetColor(int color1, int color2, float offset, float alpha) {
        int rF = Color.red(color2);
        int gF = Color.green(color2);
        int bF = Color.blue(color2);
        int aF = Color.alpha(color2);
        int rS = Color.red(color1);
        int gS = Color.green(color1);
        int bS = Color.blue(color1);
        int aS = Color.alpha(color1);
        return Color.argb((int) ((aS + (aF - aS) * offset) * alpha), (int) (rS + (rF - rS) * offset), (int) (gS + (gF - gS) * offset), (int) (bS + (bF - bS) * offset));
    }

}
