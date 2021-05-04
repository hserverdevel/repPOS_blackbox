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

public class LineSeparator extends RecyclerView.ItemDecoration {
    private int offset;
    private Context context;
    private Drawable divider;
    private float density;

    public LineSeparator(Context c, int offset) {
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
        ButtonLayout b = (ButtonLayout) view.getTag();
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
            ButtonLayout b = (ButtonLayout) child.getTag();
            if(b.getTitle()!="!!FAKE0701!!") {
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                if (i % 4 == 0) {
                    if(i!=childCount-1) {
                        View nextChild = parent.getChildAt(i + 1);
                        ButtonLayout nb = (ButtonLayout) nextChild.getTag();
                        if (nb.getTitle() != "!!FAKE0701!!") {
                            top = child.getTop();
                            bottom = (int) (child.getBottom() - 1*density);
                            marginLeft = params.getMarginStart();
                        }
                    }
                    //top = child.getBottom() + 5;
                    //bottom = top + divider.getIntrinsicHeight();
                }
                if (i % 4 != 3) {
                    if (i != childCount - 1) {
                        View nextChild = parent.getChildAt(i+1);
                        ButtonLayout nb = (ButtonLayout) nextChild.getTag();
                        if(nb.getTitle()!="!!FAKE0701!!") {
                            left = (int) (child.getRight() + parent.getPaddingLeft() - 9*density);
                            right = (int) (left + 2*density);
                            divider = context.getDrawable(R.drawable.divider_line);
                            divider.setBounds(left, top, right, bottom);
                            divider.draw(c);
                        }
                    }
                }
                if (i % 4 == 0 && i != 0) {
                    marginRight = params.getMarginEnd();
                    left = parent.getPaddingLeft() + marginLeft;
                    //dimensioni modificate per garantire la giusta lunghezza del divisore
                    right = (int) (parent.getWidth()
                            + parent.getPaddingRight()
                            + marginRight
                            //+ offset
                            + 6*density
                    );
                    int htop = (int) (child.getTop() - 8*density);
                    int hbottom = (int) (htop + 2*density);
                    divider = context.getDrawable(R.drawable.divider_line_horizontal);
                    divider.setBounds(left, htop, right, hbottom);
                    divider.draw(c);
                    /*if(i!=childCount-1) {
                        View nextChild = parent.getChildAt(i+1);
                        ButtonLayout nb = (ButtonLayout) nextChild.getTag();
                        if(nb.getTitle()!="!!FAKE0701!!") {
                            marginRight = params.getMarginEnd();
                            left = parent.getPaddingLeft() + marginLeft;
                            right = parent.getWidth() - parent.getPaddingRight() - marginRight - offset + 2;
                            int htop = child.getTop() - 8;
                            int hbottom = htop + 2;
                            divider = context.getDrawable(R.drawable.divider_line_horizontal);
                            divider.setBounds(left, htop, right, hbottom);
                            divider.draw(c);
                        }
                    }else{

                    }*/
                }
            }
        }
    }
}
