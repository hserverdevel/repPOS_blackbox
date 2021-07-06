package com.example.blackbox.fragments;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.blackbox.R;
import com.example.blackbox.activities.ClientsActivity;
import com.example.blackbox.activities.PaymentActivity;
import com.example.blackbox.adapter.PaymentOptionsAdapter;
import com.utils.db.DatabaseAdapter;

/**
 * Created by DavideLiberato on 07/07/2017.
 */

public class OptionsFragment extends Fragment {

    private View myself;
    private PaymentOptionsAdapter rv_adapter;

    public boolean onlyPrint = false;
    public boolean onlyDiscount = false;
    public boolean invoice = true;
    public boolean homage = true;
    public boolean round = true;
    public boolean print = true;
    public boolean discount = true;
    public boolean mailButton = true;
    public void setOnlyDiscount(boolean b){ this.onlyDiscount= b;}
    private String email = "";
    public void setEmail(String mail){email = mail;}
    private boolean moreThanOneMail = false;
    public void setMoreThanOneMail(boolean value){moreThanOneMail = value;}

    public void setOnlyPrint(boolean b){ this.onlyPrint = b;}

    public OptionsFragment(){
        super();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        myself = inflater.inflate(R.layout.fragment_options, container, false);
        DatabaseAdapter dbA = new DatabaseAdapter(getContext());

        Display display = ((WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);

        float density = getContext().getResources().getDisplayMetrics().density;
        int top = (int) (6* density);

        RecyclerView paymentTypes_rv = (RecyclerView) myself.findViewById(R.id.payment_types_rv);
        paymentTypes_rv.setLayoutManager(new GridLayoutManager(getContext(),1));//(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        paymentTypes_rv.addItemDecoration(new RecyclerView.ItemDecoration()
        {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state)
            {
                if ((int)view.getTag() != 0)
                    { outRect.set(0, top,0,0); }
            }
        });

        rv_adapter = new PaymentOptionsAdapter(dbA, getContext());
        paymentTypes_rv.setAdapter(rv_adapter);
        setupReceiptOptionsButtons();
        return myself;
    }

    //this is when the split bille left total bill to zero
    public void setButtonPermissionForTotalEnd(){
        invoice = false;
        homage = false;
        round = false;
        print = false;
        discount = false;
        mailButton = false;
    }

    //for every case execpt number
    public void setButtonPermission(){
        invoice = true;
        homage = true;
        round = true;
        print = true;
        discount = true;
        mailButton = true;
    }

    public void setButtonPermissionExSplit(){
        invoice = true;
        homage = false;
        round = true;
        print = true;
        discount = true;
        mailButton = true;
    }

    public void setButtonPermissionForTotalHmage(){
        invoice = false;
        homage = false;
        round = false;
        print = true;
        discount = true;
        mailButton = true;
    }

    //configuration for number split, only pront invoice and email
    public void setButtonPermissionForNumber(){
        invoice = false;
        homage = false;
        round = false;
        print = true;
        discount = false;
        mailButton = true;
    }

    public void setButtonPermissionForInvoice(){
        invoice = true;
        homage = false;
        round = false;
        print = false;
        discount = false;
        mailButton = false;
    }

    public void setButtonPermissionForPrintOnly(){
        invoice = false;
        homage = false;
        round = false;
        print = true;
        discount = false;
        mailButton = false;
    }

    public void setButtonPermissionForSplitElementItem(){
        invoice = false;
        homage = true;
        round = false;
        print = false;
        discount = false;
        mailButton = false;
    }

    public void setButtonPermissionBuyFidelity(){
        invoice = false;
        homage = true;
        round = false;
        print = false;
        discount = false;
        mailButton = false;
    }


    public void activatePayments(){
        myself.findViewById(R.id.option_payment_header).setAlpha(1.0f);
        rv_adapter.turnOnOffButtons(-1,true);
        rv_adapter.setActive(true);
    }

    public void deactivatePayments(){
        myself.findViewById(R.id.option_payment_header).setAlpha(0.15f);
        rv_adapter.setPaymentButtonOpacityOnOff(false);

    }

    public void reactivatePayments(){
        myself.findViewById(R.id.option_payment_header).setAlpha(1.0f);
        rv_adapter.loadButtons(0);
        rv_adapter.setPaymentButtonOpacityOnOff(true);

    }

    public void activateOnlyPayment(){
        myself.findViewById(R.id.option_payment_header).setAlpha(1.0f);
        rv_adapter.loadButtonsPaymentOnly();
    }

    public PaymentOptionsAdapter getAdapter() {return rv_adapter;}

    private void setupReceiptOptionsButtons()
    {
        myself.findViewById(R.id.print_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(print) {
                    myself.findViewById(R.id.print_button).setEnabled(false);
                    ((PaymentActivity)getContext()).printNonFiscal();
                    new Handler().postDelayed(new Runnable()
                                              {
                                                  public void run()
                                                  {
                                                      myself.findViewById(R.id.print_button).setEnabled(true);
                                                  }
                                              }, 5000    //Specific time in milliseconds
                    );
                }
            }
        });

        myself.findViewById(R.id.invoice_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(invoice){
                    ((PaymentActivity) getContext()).openClientPopup();
                }
            }
        });

        myself.findViewById(R.id.homage_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(homage) {

                    ((PaymentActivity) getContext()).setSplitElementItem();
                }
            }
        });

        myself.findViewById(R.id.discount_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(discount) {

                    setButtonPermissionForTotalEnd();
                    // if(!onlyPrint && !onlyDiscount)
                    ((PaymentActivity) getContext()).openPinpad(2);
                }
            }
        });

        myself.findViewById(R.id.discount_button).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(discount) {
                    ((PaymentActivity) getContext()).openRemoveAllDiscount();
                }
                return false;
            }
        });

        myself.findViewById(R.id.round_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(round) ((PaymentActivity)getContext()).setRoundDiscount();
            }
        });

        myself.findViewById(R.id.email_it_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(email == null){
                    Toast.makeText(getContext(), R.string.error, Toast.LENGTH_SHORT).show();
                }
                //client already registered and selected
                else if(mailButton) {
                    if (!email.isEmpty() && !moreThanOneMail) {
                        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                        String mailto = "mailto:" + email +
                                "?subject=" + Uri.encode("Scontrino Burgheria") +
                                "&body=" + Uri.encode("corpo del messaggio contenente lo scontrino o fattura");
                        emailIntent.setData(Uri.parse(mailto));

                        try {
                            startActivity(emailIntent);
                        } catch (ActivityNotFoundException e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), R.string.no_application_found_for_sending_emails, Toast.LENGTH_LONG).show();
                        }
                    }
                    //more than one client selected
                    else if (moreThanOneMail) {
                        PaymentActivity pa = (PaymentActivity) getActivity();
                        Intent newIntent = new Intent(getActivity(), ClientsActivity.class);
                        newIntent.setAction("selectEmail");
                        newIntent.putExtra("billId", pa.getBillId());
                        startActivity(newIntent);
                    }
                    //client not yet selected, or not registered
                    else if (email.isEmpty()) {
                        PaymentActivity pa = (PaymentActivity) getActivity();
                        Intent newIntent = new Intent(getActivity(), ClientsActivity.class);
                        newIntent.setAction("newEmail");
                        newIntent.putExtra("billId", pa.getBillId());
                        newIntent.putExtra("orderNumber", pa.getOrderNumber());
                        newIntent.putExtra("tableNumber", pa.getTableNumber());
                        startActivity(newIntent);
                    }
                }
            }
        });
    }

}
