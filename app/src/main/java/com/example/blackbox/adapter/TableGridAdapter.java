package com.example.blackbox.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.blackbox.R;
import com.example.blackbox.activities.TableActivity;
import com.example.blackbox.graphics.CustomTextView;
import com.example.blackbox.graphics.ItemTouchHelperAdapter;
import com.utils.db.DatabaseAdapter;

import java.text.DecimalFormat;
import java.util.ArrayList;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class TableGridAdapter extends Adapter<ViewHolder> implements ItemTouchHelperAdapter {
    private float density;
    private float dpHeight;
    private float dpWidth;
    private Context context;
    private DatabaseAdapter dbA;
    private LayoutInflater inflater;
    private int tableNumbers = 0;
    private int billId;
    private int orderNumber;
    private ArrayList<Button> buttons = new ArrayList<Button>();
    TableActivity ta;
    Intent intent;

    public TableGridAdapter(Intent in, int numberOfTable, Context c, int bId, int oNumber, DatabaseAdapter database, ArrayList<Button> buttonsNumber){
        intent = in;
        buttons = buttonsNumber;
        billId= bId;
        orderNumber = oNumber;
        context = c;
        this.dbA = database;
        tableNumbers = numberOfTable;
        inflater = (LayoutInflater) context
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.activity_table, null);
        /**DISPLAY METRICS:  used to center the window in the screen**/
        Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);
        density  = context.getResources().getDisplayMetrics().density;
        dpHeight = outMetrics.heightPixels;// / density;
        dpWidth  = outMetrics.widthPixels;// / density;


     }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        ViewHolder vh;
        v = inflater.inflate(R.layout.table_gridview, null);
        vh = new ButtonHolder(v);
        return vh;
    }

    @Override
    public int getItemViewType(int position){
        return position;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ButtonHolder button = (ButtonHolder) holder;

        if(buttons.get(position).getSet()){
            //occupato
            String formattedPosition = new DecimalFormat("00").format((position + 1));
            button.tablePosition.setText(formattedPosition);
            int color = Color.parseColor("#cd0046");
            button.view.setBackgroundColor(color);
        }else{
            if(buttons.get(position).getCleaning()){
                //in pulizia
                String formattedPosition = new DecimalFormat("00").format((position + 1));
                button.tablePosition.setText(formattedPosition);
                int color = Color.parseColor("#5cae32");
                button.view.setBackgroundColor(color);
            }else{
                //libero
                int color = Color.parseColor("#444444");
                button.view.findViewById(R.id.table_text_container).setBackgroundColor(color);

                final int pos = position;
                String formattedPosition = new DecimalFormat("00").format((position + 1));
                button.tablePosition.setText(formattedPosition);
                button.view.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                setTable(view, pos);

                            }
                        }
                );
            }
        }


    }



    public void setTable(View view, int position) {
        //int color = Color.parseColor("#cd0046");
        //view.findViewById(R.id.table_text_container).setBackgroundColor(color);

        if(billId!=-1){
            //non dovrei più arrivarci perchè salvo prima di entrare in questa pagina
            String query = "SELECT * FROM temp_table WHERE total_bill_id="+billId+";";
            int oldPosition = dbA.getOldTablePosition(query);
            if(oldPosition!=-1){
                dbA.saveNewTempTable(query, position, billId);
                buttons.get((position)).setSet(true);
                buttons.get(oldPosition).setSet(false);

                this.notifyDataSetChanged();

            }else{
                //inserisco nuovo
                dbA.insertIntoTempTable(position, billId);
                buttons.get((position)).setSet(true);
                this.notifyDataSetChanged();
            }


        }else{
            //da creare bill id e table

        }
        intent.putExtra("tableNumber", position);
        //ta.setTableNumber(position);



    }

    @Override
    public int getItemCount() {
        return buttons.size();
    }

    /**
     * Here is defined the method which handles the swapping movement performed thanks
     * to the ItemTouchHelperAdapter/Callback system
     */
    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {

        return true;
    }

    /**
     * Method to Override. Gets called when element gets swiped?
     */
    @Override
    public void onItemDismiss(int position) {}

    /** HOLDERS **/
    public static class ButtonHolder extends ViewHolder{
        public View view;
        public RelativeLayout txtcontainer;
        public TextView text;
        public TextView tablePosition;
        public ButtonHolder(View itemView) {
            super(itemView);
            view = itemView;
            txtcontainer = (RelativeLayout) view.findViewById(R.id.table_text_container);
            text = (CustomTextView)view.findViewById(R.id.table_text);
            tablePosition = (CustomTextView)view.findViewById(R.id.table_text_position);
        }
        @Override
        public String toString(){ return "ButtonHolder, Title: "+text.getText().toString();}
    }

    public static class Button{
        private int position;
        private Boolean set;
        private Boolean cleaning;

        public Button(Integer pos, Boolean b, Boolean cleaning){

            this.position= pos;
            this.set = b;
            this.cleaning = cleaning;
        }

        public void setPosition(int p){position = p;}
        public void setSet(Boolean b){set = b;}
        public void setCleaning(Boolean b){cleaning= b;}

        public int getPosition(){return position;}
        public Boolean getSet(){return set;}
        public Boolean getCleaning(){return cleaning;}
    }


}

