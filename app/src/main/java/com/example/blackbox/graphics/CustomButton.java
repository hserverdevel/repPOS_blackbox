package com.example.blackbox.graphics;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;

import com.example.blackbox.R;

public class CustomButton extends android.support.v7.widget.AppCompatButton {
    /**
     * Defining Constants to identify Fonts and styles
     */
    private final static int FRANGD_CP = 0;
    private final static int FRANGH = 1;
    private final static int ROBOTO_BLACK = 2;
    private final static int ROBOTO_BOLD = 3;
    private final static int ROBOTO_ITALIC = 4;
    private final static int ROBOTO_REGULAR = 5;
    private final static int FRANG_DEMI = 10;
    private final static int FRANLIN_GOTHIC_BOOK_CD =11;
    private final static int FRANLIN_GOTHIC_BOOK_ITALIC =12;
    private final float spacing = (float)0.09;
    private Context context;

    public CustomButton(Context context){
        super(context);
        this.context = context;
    }

    public CustomButton(Context context, AttributeSet attrs){
        super(context,attrs);
        setStateListAnimator(null);
        parseAttributes(context,attrs);
        setPaintFlags(getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        this.context = context;
    }

    public CustomButton(Context context, AttributeSet attrs, int style){
        super(context, attrs, style);
        setStateListAnimator(null);
        parseAttributes(context,attrs);
        setPaintFlags(getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        this.context = context;
    }

    private void parseAttributes(Context context, AttributeSet attrs) {
        TypedArray values = context.obtainStyledAttributes(attrs, R.styleable.CustomButton);

        int typeface = values.getInt(R.styleable.CustomButton_typeface, ROBOTO_REGULAR);
        float lSpacing = values.getFloat(R.styleable.CustomTextView_letterSpacing, spacing);
        switch(typeface){
            case FRANGD_CP:
                setTypeface(Typeface.createFromAsset(context.getAssets(),"fonts/FRANGDCP.TTF"));
                break;
            case FRANGH:
                setTypeface(Typeface.createFromAsset(context.getAssets(),"fonts/FRANGH__.ttf"));
                break;
            case ROBOTO_BLACK:
                setTypeface(Typeface.createFromAsset(context.getAssets(),"fonts/Roboto_Black.ttf"));
                break;
            case ROBOTO_BOLD:
                setTypeface(Typeface.createFromAsset(context.getAssets(),"fonts/Roboto_Bold.ttf"));
                break;
            case ROBOTO_REGULAR:
                setTypeface(Typeface.createFromAsset(context.getAssets(),"fonts/Roboto_Regular.ttf"));
                break;
            case FRANG_DEMI:
                setTypeface(Typeface.createFromAsset(context.getAssets(),"fonts/FRANGD__.ttf"));
                break;
            case FRANLIN_GOTHIC_BOOK_CD:
                setTypeface(Typeface.createFromAsset(context.getAssets(),"fonts/FRANGWC_.TTF"));
                break;
            case FRANLIN_GOTHIC_BOOK_ITALIC:
                setTypeface(Typeface.createFromAsset(context.getAssets(),"fonts/FRANGBI_.TTF"));
                break;
            default:
                setTypeface(Typeface.createFromAsset(context.getAssets(),"fonts/Roboto_Regular.ttf"));
        }

        setLetterSpacing(lSpacing);
        values.recycle();
    }


}
