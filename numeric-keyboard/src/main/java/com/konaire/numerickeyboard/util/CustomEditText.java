package com.konaire.numerickeyboard.util;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;

public class CustomEditText extends AppCompatEditText {
    private Context ctx;
    public CustomEditText(Context context) {
        super(context);
        ctx=context;
        init();
    }

    public CustomEditText(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        ctx=context;
        init();
    }

    public CustomEditText(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ctx=context;
        init();
    }

    public void init()
    {
            Typeface face=Typeface.createFromAsset(ctx.getAssets(), "fonts/avenir_medium.ttf");
            setTypeface(face);
    }

}
