package com.alper.samsun.component;

import android.content.Context;
import android.support.v7.internal.widget.TintTypedArray;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

public class TintAutoComplete extends AutoCompleteTextView {

    private static final int[] TINT_ATTRS = {
            android.R.attr.background
    };

    public TintAutoComplete(Context context) {
        this(context, null);
    }

    public TintAutoComplete(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public TintAutoComplete(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TintTypedArray a = TintTypedArray.obtainStyledAttributes(context, attrs, TINT_ATTRS,
                defStyleAttr, 0);
        setBackgroundDrawable(a.getDrawable(0));
        a.recycle();
    }
}