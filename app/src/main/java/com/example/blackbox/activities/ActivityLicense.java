package com.example.blackbox.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.blackbox.R;
import com.example.blackbox.graphics.CustomButton;
import com.example.blackbox.graphics.CustomEditText;
import com.example.blackbox.graphics.CustomTextView;
import com.example.blackbox.model.TwoString;
import com.utils.db.DatabaseAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

/**
 * Created by tiziano on 2/25/19.
 */

public class ActivityLicense extends AppCompatActivity {

    public float density;
    public float dpHeight;
    public float dpWidth;
    private DatabaseAdapter dbA;
    private String licenseString;
    private CustomButton codeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license);

        Objects.requireNonNull(getSupportActionBar()).hide();

        dbA = new DatabaseAdapter(this);

        TwoString registration = dbA.selectCodeDuration();

        int duration = dbA.getCodeDuration(registration.getFirstString());

        Resources resources = getResources();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        //my actual date
        Calendar cal = Calendar.getInstance();

        try
        {
            cal.setTime(dateFormat.parse(registration.getSecondString()));// all done
            int month = cal.get(Calendar.MONTH)+1;

            //end date
            Calendar cal2 = Calendar.getInstance();
            cal2.add(Calendar.DATE, -15);
            //cal2.setTime(dateFormat.parse("10/02/2019"));// all done

            //minus 15
            Calendar cal3 = Calendar.getInstance();
            //cal3.setTime(dateFormat.parse("01/03/2019"));
            cal3.add(Calendar.MONTH, duration);
            //cal3.add(Calendar.DATE, -15);

            long difference = Math.abs(cal.getTimeInMillis()- cal3.getTimeInMillis());
            long differenceDates = difference / (24 * 60 * 60 * 1000);
            if(differenceDates<0) difference=0;

            Log.i("DIFFERENCE", ""+differenceDates);

            ((CustomTextView) findViewById(R.id.expiration_text_window)).setText(resources.getString(R.string.license_expiration__days, differenceDates));


            CustomButton ok = (CustomButton) findViewById(R.id.ok_button);
            //licenseButton.setText(licenseString);

            ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(cal.compareTo(cal3)<=0) {
                        Intent i = new Intent(ActivityLicense.this, Login.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        finish();
                    }
                }
            });

            codeButton = (CustomButton) findViewById(R.id.update_code_button);
            //licenseButton.setText(licenseString);

            codeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    licenseString = fireSingleInputDialogPopup();
                }
            });




                            /*if(cal2.compareTo(cal3)>0) {
                                Intent i = new Intent(SplashScreen.this, ActivityLicense.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(i);
                                finish();
                            }else{
                                Intent i = new Intent(SplashScreen.this, Login.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(i);
                                finish();
                            }*/

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }


    public String fireSingleInputDialogPopup()
    {
        Display display = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);

        density  = getResources().getDisplayMetrics().density;
        dpHeight = outMetrics.heightPixels;/// density;
        dpWidth  = outMetrics.widthPixels;/// density;

        LayoutInflater layoutInflater = (LayoutInflater) this
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.single_input_dialog,null);
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);

        popupView.post(new Runnable()
        {
            @Override
            public void run()
            {
               /* @SuppressLint("WrongViewCast") RelativeLayout.LayoutParams rlp1 =
                        (RelativeLayout.LayoutParams) popupView.findViewById(R.id.single_input_window).getLayoutParams();
                int top1 = (int) (dpHeight - 52) / 2 - rlp1.height / 2;
                rlp1.topMargin = top1;
                popupView.findViewById(R.id.single_input_window).setLayoutParams(rlp1);*/
            }
        });

        CustomEditText license_input = (CustomEditText)popupView.findViewById(R.id.single_input);
        license_input.setHint(R.string.insert_license_code);
        //license_input.setInputType(InputType.TYPE_NUMBER_VARIATION_NORMAL);

        ImageButton okButton = (ImageButton)popupView.findViewById(R.id.ok);
        okButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (!Objects.requireNonNull(license_input.getText()).toString().equals(""))
                {
                    licenseString = license_input.getText().toString();
                    if (dbA.checkIfStaticCodeIsUsed(licenseString))
                    {
                        Date c = Calendar.getInstance().getTime();
                        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
                        String formattedDate = df.format(c);
                        dbA.execOnDb("UPDATE registered_activation_code SET code='"+licenseString+"' , SET registration='"+formattedDate+"'");
                        dbA.execOnDb("UPDATE static_activation_code SET used=1 where code='"+licenseString+"'");
                        Toast.makeText(getApplicationContext(), R.string.license_updated, Toast.LENGTH_SHORT).show();
                        license_input.setText("");

                        codeButton .setText(licenseString);

                        popupWindow.dismiss();
                        Intent i = new Intent(ActivityLicense.this, Login.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        finish();
                    }

                    else
                        { Toast.makeText(getApplicationContext(), R.string.cant_update_license_code, Toast.LENGTH_SHORT).show(); }


                }

                else
                {
                    license_input.setText("");
                    Toast.makeText(getApplicationContext(), R.string.insert_a_valid_license_key, Toast.LENGTH_SHORT).show();
                }
            }
        });

        ImageButton killButton = (ImageButton)popupView.findViewById(R.id.kill);
        killButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });

        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(findViewById(R.id.main), 0, 0, 0);

        return licenseString;
    }
}
