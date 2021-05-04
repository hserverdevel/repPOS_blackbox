package com.example.blackbox.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.blackbox.R;
import com.example.blackbox.graphics.CustomButton;
import com.example.blackbox.graphics.CustomEditText;
import com.example.blackbox.model.TimerManager;
import com.example.blackbox.server.Server;
import com.utils.db.DatabaseAdapter;

import java.util.Arrays;
import java.util.List;

/**
 * Created by tiziano on 9/19/17.
 */

public class PinpadActivity extends AppCompatActivity {

    private String passcode = "";

    // the views that show how many number has
    // been inputed in the pinpad
    private List<View> sixInputCounterViews;

    private View container1;
    private View container2;
    private View container3;
    private View container4;
    private View container5;
    private View container6;

    private String username;
    private int isAdmin;

    Resources resources;
    Animation shake;

    private final int red = Color.parseColor("#cd0046");
    private final int black = Color.parseColor("#DD000000");


    public PinpadActivity() { }

    public static PinpadActivity newInstance() {
        return new PinpadActivity();
    }

    @Override
    public void onBackPressed() { /* do nothing */ }


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        TimerManager.stopPinpadAlert();

        Intent intent = this.getIntent();
        username  = intent.getStringExtra("username");
        isAdmin  = intent.getIntExtra("isAdmin", -1);
        resources = getResources();

        getSupportActionBar().hide();
        setContentView(R.layout.activity_pinpad);

        setupDigits();

        PinpadActivity pinpadActivity = this;

        sixInputCounterViews = Arrays.asList(
            findViewById(R.id.first_d),
            findViewById(R.id.second_d),
            findViewById(R.id.third_d),
            findViewById(R.id.fourth_d),
            findViewById(R.id.fifth_d),
            findViewById(R.id.sixth_d));

        shake = AnimationUtils.loadAnimation(this, R.anim.shake);

        Server server =  Server.getInstance();
        server.killAll();

        findViewById(R.id.kill).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                if(passcode.length() <= 6 && passcode.length() > 0)
                {
                    int position = passcode.length();
                    passcode  = new StringBuilder(passcode).deleteCharAt(position-1).toString();
                    sixInputCounterViews.get(position - 1).setBackgroundColor(black);

                }
            }
        });


        findViewById(R.id.ok).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (passcode.length() == 6)
                {
                    DatabaseAdapter dbA = new DatabaseAdapter(getApplicationContext());
                    if(dbA.checkUserPasscode(username, passcode))
                    {
                        Intent intent = new Intent(getApplicationContext(), Operative.class);
                        intent.putExtra("isAdmin", isAdmin);
                        intent.putExtra("username", username);
                        startActivity(intent);
                        TimerManager.startPinpadAlert(1);
                        finish();
                    }

                    else
                    {
                        findViewById(R.id.LoginWindow).startAnimation(shake);
                        Toast.makeText(PinpadActivity.this, resources.getString(R.string.sorry__wrong_passcode, username), Toast.LENGTH_SHORT).show();
                    }

                }

                else
                {
                    findViewById(R.id.LoginWindow).startAnimation(shake);
                    Toast.makeText(PinpadActivity.this, R.string.please_fill_passcode, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    public void onUserInteraction() {
        // TODO Auto-generated method stub
        super.onUserInteraction();
//        TimerManager.startLogoutAlert();
    }



    private void setupDigits()
    {
        RelativeLayout digitContainer = findViewById(R.id.digits_container);
        View v;

        for (int i = 0; i < digitContainer.getChildCount(); i++)
        {
            v = digitContainer.getChildAt(i);
            v.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    char digit = (((CustomButton)v).getText().charAt(0));
                    if (passcode.length() < 6)
                    {
                        passcode += digit;
                        sixInputCounterViews.get(passcode.length()-1).setBackgroundColor(red);
                        if (passcode.length() == 6) { checkLogin(); }
                    }

                    else if (passcode.length() == 6)
                       { checkLogin(); }
                }
            });
        }
    }


    private void resetFields()
    {
        for (View c : sixInputCounterViews)
            { c.setBackgroundColor(black); }

        passcode = "";
    }


    private void checkLogin()
    {
        if (passcode.equals("000000"))
        {
            Intent intent = new Intent(getApplicationContext(), LogcatActivity.class);
            startActivity(intent);
            finish();
        }

        else
        {
            DatabaseAdapter dbA = new DatabaseAdapter(getApplicationContext());
            Cursor c = dbA.fetchUserDataByPasscode(passcode);

            String username = "";

            if (c.getCount() > 0)
            {
                if (c.moveToNext())
                {
                    isAdmin = c.getInt(c.getColumnIndex("userType"));
                    username = c.getString(c.getColumnIndex("email"));
                }

                TimerManager.stopPinpadAlert();
                Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage( getBaseContext().getPackageName() );
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.putExtra("isAdmin", isAdmin);
                i.putExtra("username", username);
                i.putExtra("status", "pinpad");
                startActivity(i);
                finish();
            }

            else
            {
                findViewById(R.id.LoginWindow).startAnimation(shake);
                passcode = new String();
                resetFields();
                Toast.makeText(PinpadActivity.this, resources.getString(R.string.sorry__wrong_passcode, username), Toast.LENGTH_SHORT).show();
            }
        }


    }

    
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof CustomEditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }

    String barcode="";
    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if(e.getAction()==KeyEvent.ACTION_DOWN){
            char pressedKey = (char) e.getUnicodeChar();
            barcode += pressedKey;
        }
        if (e.getAction()==KeyEvent.ACTION_DOWN && e.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
            barcode="";
        }
        return super.dispatchKeyEvent(e);
    }

}
