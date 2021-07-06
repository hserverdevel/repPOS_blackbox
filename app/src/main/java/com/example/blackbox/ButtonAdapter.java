package com.example.blackbox;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.blackbox.model.ButtonLayout;

import java.util.ArrayList;



public class ButtonAdapter extends BaseAdapter{
    private Context context;
    private ArrayList<ButtonLayout> buttons;

    public ButtonAdapter(Context c, ArrayList<ButtonLayout> al){
        context = c;
        buttons = al;
    }

    @Override
    public int getCount() {
        return buttons.size();
    }

    @Override
    public Object getItem(int position) {
        return buttons.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ButtonLayout button = (ButtonLayout) getItem(position);
        View view;
        if(convertView == null || button.getID() == -11) {
                LayoutInflater inflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.element_gridview_element, null);
        }
        else view = convertView;
        if(button.getID()!= -11){ // NOT big_plus_button case

            // Title
            ((TextView) view.findViewById(R.id.title)).setText(button.getTitle());
            ((TextView) view.findViewById(R.id.title)).setLayoutParams(new LinearLayout.LayoutParams(146,18));

            //Subtitle
            ((TextView) view.findViewById(R.id.subtitle)).setText(button.getSubTitle());
            ((TextView) view.findViewById(R.id.subtitle)).setLayoutParams(new LinearLayout.LayoutParams(146,18));

            view.findViewById(R.id.button_img).setVisibility(View.GONE);
            view.findViewById(R.id.button_frame_img).setVisibility(View.VISIBLE);
            // Image
            ((ImageView) view.findViewById(R.id.button_frame_img)).setImageResource(getImageId(context,button.getImg()));
        }
        else {  // Sets plus_button params
            //view.findViewById(R.id.title).setVisibility(View.GONE);
            view.findViewById(R.id.text_container).setVisibility(View.GONE);
            view.findViewById(R.id.button_frame_img).setVisibility(View.GONE);
            view.findViewById(R.id.button_img).setVisibility(View.VISIBLE);
            view.findViewById(R.id.button_img).setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            // Image
            ((ImageView) view.findViewById(R.id.button_img)).setImageResource(getImageId(context,button.getImg()));
        }
        view.setLayoutParams(new LinearLayout.LayoutParams(160,152));

        //use a GradientDrawable with only one color set, to make it a solid color
        GradientDrawable border = new GradientDrawable();
        border.setColor(0xFF000000); //black background ->todo set background color accordingly to db data
        border.setStroke(3, 0xFFC6C5C6); //gray border with full opacity
        border.setCornerRadius(8);
        view.setBackground(border);
        view.setTag(button);
        return view;
    }

    public static int getImageId(Context context, String imageName) {
        return context.getResources().getIdentifier("drawable/" + imageName, null, context.getPackageName());
    }

}

