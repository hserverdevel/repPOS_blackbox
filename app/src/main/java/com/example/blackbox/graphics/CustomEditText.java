package com.example.blackbox.graphics;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatEditText;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.AttributeSet;
import android.widget.Toast;

import com.example.blackbox.R;


public class CustomEditText extends AppCompatEditText {

    /**
     * Defining Constants to identify Fonts and styles
     */
    private final static int FRANGD_CP = 0;
    private final static int FRANGH = 1;
    private final static int ROBOTO_BLACK = 2;
    private final static int ROBOTO_BOLD = 3;
    private final static int ROBOTO_ITALIC = 4;
    private final static int ROBOTO_REGULAR = 5;
    private final static int ROBOTO_LIGHT_ITALIC = 6;
    private final static int ROBOTO_CONDENSED_BOLD = 7;
    private final static int ROBOTO_CONDENSED_REGULAR = 8;
    private final static int ROBOTO_LIGHT = 9;
    private final static int FRANG_DEMI = 10;
    private final static int FRANLIN_GOTHIC_BOOK_CD =11;
    private final static int FRANLIN_GOTHIC_BOOK_ITALIC =12;
    private final float spacing = (float)0.00;
    private Context context;

    public CustomEditText(Context context){
        super(context);
        this.context = context;
    }

    public CustomEditText(Context context, AttributeSet attrs){
        super(context,attrs);
        parseAttributes(context,attrs);
        setPaintFlags(getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
    }

    public CustomEditText(Context context, AttributeSet attrs, int style){
        super(context, attrs, style);
        parseAttributes(context,attrs);
        setPaintFlags(getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
    }

    private void parseAttributes(final Context context, AttributeSet attrs) {
        InputFilter filter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (source.charAt(i) == '"') {
                        Toast.makeText(context, "Character \" is not allowed", Toast.LENGTH_SHORT).show();
                        return "";
                    }
                }
                return null;
            }

        };
        setFilters(new InputFilter[] { filter });
        TypedArray values = context.obtainStyledAttributes(attrs, R.styleable.CustomTextView);
        this.context = context;
        int typeface = values.getInt(R.styleable.CustomTextView_typeface, ROBOTO_BOLD);
        float letterSpacing = values.getFloat(R.styleable.CustomTextView_letterSpacing, spacing);
        setTypeface(typeface);
        setLetterSpacing(letterSpacing);
        values.recycle();
    }

    public void setTypeface(int typeface) {
        switch (typeface) {
            case FRANGD_CP:
                setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/FRANGDCP.TTF"));
                break;
            case FRANGH:
                setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/FRANGH__.ttf"));
                break;
            case ROBOTO_BLACK:
                setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/Roboto_Black.ttf"));
                break;
            case ROBOTO_BOLD:
                setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/Roboto_Bold.ttf"));
                break;
            case ROBOTO_ITALIC:
                setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/Roboto_Italic.ttf"));
                break;
            case ROBOTO_REGULAR:
                setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/Roboto_Regular.ttf"));
                break;
            case ROBOTO_LIGHT_ITALIC:
                setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/Roboto_LightItalic.ttf"));
                break;
            case ROBOTO_CONDENSED_BOLD:
                setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/RobotoCondensed_Bold.ttf"));
                break;
            case ROBOTO_CONDENSED_REGULAR:
                setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/RobotoCondensed_Regular.ttf"));
                break;
            case ROBOTO_LIGHT:
                setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/Roboto_Light.ttf"));
                break;
            case FRANG_DEMI:
                setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/FRANGD__.ttf"));
                break;
            case FRANLIN_GOTHIC_BOOK_CD:
                setTypeface(Typeface.createFromAsset(context.getAssets(),"fonts/FRANGWC_.TTF"));
                break;
            case FRANLIN_GOTHIC_BOOK_ITALIC:
                setTypeface(Typeface.createFromAsset(context.getAssets(),"fonts/FRANGBI_.TTF"));
                break;
            default:
                setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/Roboto_Bold.ttf"));
        }
    }
}
