package com.example.blackbox.graphics;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.example.blackbox.R;
import com.example.blackbox.adapter.ModifierAdapter;

public class ModifierLineSeparator extends RecyclerView.ItemDecoration {
    private final float density;
    private int offset;
    private Context context;
    private Drawable divider;

    public ModifierLineSeparator(Context c, int offset) {
        this.offset = offset;
        this.context = c;
        Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);

        density  = context.getResources().getDisplayMetrics().density;
        divider = context.getDrawable(R.drawable.divider_line);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view,
                               RecyclerView parent, RecyclerView.State state) {
        ModifierAdapter.Modifier m = (ModifierAdapter.Modifier) view.getTag();
        //parent.getChildAdapterPosition(view) == 3
        outRect.set(0,0, (int) (offset*density), (int) (offset*density));
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int left;
        int right;
        int top = 0;
        int bottom = 0;
        int marginLeft = 0;
        int marginRight = 0;
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            if(i % 4 == 0){
                top = child.getTop();
                bottom = (int) (child.getBottom()-1*density);
                marginLeft = params.getMarginStart();
                //top = child.getBottom() + 5;
                //bottom = top + divider.getIntrinsicHeight();
            }
            if(i % 4 != 3 ){
                if(i!=childCount-1){
                    left = (int) (child.getRight() + parent.getPaddingStart() +6*density);
                    right = (int) (left + (2 * density));
                    divider = context.getDrawable(R.drawable.divider_line);
                    divider.setBounds(left, top, right, bottom);
                    divider.draw(c);
                }
            }
            if(i % 4 == 0){
                marginRight = params.getMarginEnd();
                left = parent.getPaddingLeft() + marginLeft;
                right = (int) (parent.getWidth() - parent.getPaddingRight() - marginRight- offset +2*density);
                int htop = (int) (child.getBottom() +6*density);
                int hbottom = (int) (htop + 2*density);
                divider = context.getDrawable(R.drawable.divider_line_horizontal);
                divider.setBounds(left, htop, right, hbottom);
                divider.draw(c);
            }
        }
    }
}
