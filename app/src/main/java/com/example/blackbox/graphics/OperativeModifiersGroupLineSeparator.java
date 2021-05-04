package com.example.blackbox.graphics;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.example.blackbox.R;
import com.example.blackbox.adapter.OModifierGroupAdapter;

public class OperativeModifiersGroupLineSeparator extends RecyclerView.ItemDecoration {
    private final float density;
    private int offset;
    private Context context;
    private Drawable divider;

    public OperativeModifiersGroupLineSeparator(Context c, int offset) {
        this.offset = offset;
        this.context = c;
        divider = context.getDrawable(R.drawable.divider_line);
        density  = context.getResources().getDisplayMetrics().density;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view,
                               RecyclerView parent, RecyclerView.State state) {
        OModifierGroupAdapter.OModifiersGroup g = (OModifierGroupAdapter.OModifiersGroup) view.getTag();
        //parent.getChildAdapterPosition(view) == 3
        outRect.set(0,0,(int)(offset*density),(int)(offset*density));
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
            if(i % 6 == 0){
                top = child.getTop();
                bottom = child.getBottom()-(int)(1*density);
                marginLeft = params.getMarginStart();
                //top = child.getBottom() + 5;
                //bottom = top + divider.getIntrinsicHeight();
            }
            if(i % 6 != 5 ){
                if(i!=childCount-1){
                    left = child.getRight() + parent.getPaddingStart() +(int)(6*density);
                    right = left + (int)(2*density);
                    divider = context.getDrawable(R.drawable.divider_line);

                    divider.setBounds(left, top, right, bottom);
                    divider.draw(c);
                }
            }
            if(i%6 == 0){
                //setta linee verticalie
                marginRight = params.getMarginEnd();
                left = parent.getPaddingLeft() + marginLeft;
                right = parent.getWidth() - parent.getPaddingRight() - marginRight- offset +(int)(2*density);
                int htop = child.getBottom() +(int)(6*density);
                int hbottom = htop + (int)(2*density);
                divider = context.getDrawable(R.drawable.divider_line_horizontal);
                divider.setBounds(left, htop, right, hbottom);
                divider.draw(c);
            }
        }
    }
}
