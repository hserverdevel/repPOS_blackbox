package com.example.blackbox.adapter;

/**
 * Created by tiziano on 9/2/17.
 */

import android.content.Context;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.blackbox.R;
import com.example.blackbox.graphics.CustomButton;
import com.example.blackbox.graphics.CustomTextView;
import com.example.blackbox.graphics.ItemTouchHelperAdapter;
import com.example.blackbox.graphics.OnSwipeTouchListener;
import com.example.blackbox.model.User;
import com.utils.db.DatabaseAdapter;

import java.util.ArrayList;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class UserAdapter extends Adapter<ViewHolder> implements ItemTouchHelperAdapter {

    private DatabaseAdapter dbA;
    private LayoutInflater inflater;
    private Context context;
    ArrayList<User> userList;
    private AdapterUserCallback mAdapterCallback;
    private View pop;
    private PopupWindow popwindow;
    private int userType;
    private int userId;
    private View popupview;
    private PopupWindow popupWindow;

    public UserAdapter(Context c , DatabaseAdapter database, int userType, int userId, View popupview, PopupWindow popupWindow){
        context = c;
        this.userType = userType;
        this.userId = userId;
        this.popupview = popupview;
        this.popupWindow = popupWindow;
        this.mAdapterCallback = ((AdapterUserCallback) context);
        dbA = database;
        inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        //sessionsList = dbA.getSessionsTime();
        userList = dbA.fetchUsersModel(userType);
    }

    public UserAdapter(Context c , DatabaseAdapter database, View popupview, PopupWindow popupWindow){
        context = c;
        //this.mAdapterCallback = ((AdapterSessionCallback) context);
        dbA = database;
        inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        //sessionsList = dbA.getSessionsTime();
        pop = popupview;
        popwindow = popupWindow;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        ViewHolder vh;
        v = inflater.inflate(R.layout.user_recycler, null);
        vh = new ButtonHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        User user= userList.get(position);
        ButtonHolder button = (ButtonHolder) holder;
        button.userNameContainer.setText(user.getName()+ " " + user.getSurname());
        String role = "";
        switch (user.getUserRole()){
            case 0 :
                role = "Admin";
                break;
            case 1:
                role = "Manager";
                break;
            case 2 :
                role = "Cashier";
                break;
            default:
                role = "NO ROLE";
                break;
        }
        button.userSpecContainer.setText(role);
        if(user.getIsDelete()){
            button.userDelete.setVisibility(View.VISIBLE);
        }else{
            button.userDelete.setVisibility(View.GONE);
        }

        button.view.setOnTouchListener(new OnSwipeTouchListener(context) {
            public void onSwipeRight() {


            }

            /**
             * show delete view
             */
            public void onSwipeLeft() {
                if(user.getUserRole()==0 || user.getId()==userId ){
                    Toast.makeText(context, R.string.this_is_not_allowed, Toast.LENGTH_SHORT).show();

                }else {
                    if(userType<user.getUserRole())
                    setToDelete(position);
                    else Toast.makeText(context, R.string.this_is_not_allowed, Toast.LENGTH_SHORT).show();
                }

            }

            public void onClick() {

                mAdapterCallback.setModifyUser(user, popupview, popupWindow);

            }
        });

        button.userDelete.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {

                        dbA.deleteUser(user.getId());
                        userList = dbA.fetchUsersModel(userType);
                        notifyDataSetChanged();

                    }
                }
        );

    }

    public void setToDelete(int position){
        for(int i=0; i<userList.size(); i++ ){
            if(i==position) {
                userList.get(i).setDelete(!userList.get(i).getIsDelete());
            }
            else userList.get(i).setDelete(false);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position){
        return position;
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        return true;
    }

    @Override
    public void onItemDismiss(int position) {

    }

    /** HOLDERS **/
    public static class ButtonHolder extends ViewHolder{
        public View view;
        public RelativeLayout linearLayout;
        public CustomTextView userNameContainer;
        public CustomTextView userSpecContainer;
        public CustomButton userDelete;

        public ButtonHolder(View itemView) {
            super(itemView);
            view = itemView;
            linearLayout = (RelativeLayout) view.findViewById(R.id.user_container);
            userNameContainer = (CustomTextView) view.findViewById(R.id.user_name_container);
            userSpecContainer = (CustomTextView) view.findViewById(R.id.user_spec_container) ;
            userDelete = (CustomButton) view.findViewById(R.id.user_delete_container);
        }
        @Override
        public String toString(){ return "ButtonHolder, Title: "+userNameContainer.getText().toString();}
    }

    public interface AdapterUserCallback {
        void setModifyUser(User user, View popupview, PopupWindow popupWindow);

    }




}
