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
import com.example.blackbox.model.ButtonLayout;

public class IconSeparator extends RecyclerView.ItemDecoration {
    private final float density;
    private int offset;
    private Context context;
    private Drawable divider;

    public IconSeparator(Context c, int offset) {
        this.offset = offset;
        this.context = c;
        divider = context.getDrawable(R.drawable.divider_line);

        Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);

        density  = context.getResources().getDisplayMetrics().density;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view,
                               RecyclerView parent, RecyclerView.State state) {
        ButtonLayout b = (ButtonLayout) view.getTag();
        //parent.getChildAdapterPosition(view) == 3
        if(parent.getChildAdapterPosition(view)<=7)
            outRect.set(0,0, (int) (offset*density), (int) (offset*density));
        else
            outRect.set(0, (int) (6*density),0,0);
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
            if(i % 8 == 0){
                top = child.getTop();
                bottom = (int) (child.getBottom()-6*density);

                marginLeft = params.getMarginStart();
                //top = child.getBottom() + 5;
                //bottom = top + divider.getIntrinsicHeight();
            }
            if(i % 8 != 7 ){
                if(i!=childCount-1){
                    left = child.getRight() + parent.getPaddingLeft();
                    right = (int) (left + 2*density);
                    divider = context.getDrawable(R.drawable.divider_line);
                    divider.setBounds(left, top, right, bottom);
                    divider.draw(c);
                }
            }
            if(i%8 == 0 && i != 0){
                marginRight = params.getMarginEnd();
                left = parent.getPaddingLeft() + marginLeft;
                right = (int) (parent.getWidth() - parent.getPaddingRight() - marginRight- offset +2*density);
                int htop = (int) (child.getTop()-8*density);
                int hbottom = (int) (htop + 2*density);
                divider = context.getDrawable(R.drawable.divider_line_horizontal);
                divider.setBounds(left, htop, right, hbottom);
                divider.draw(c);
            }
        }
    }
}
