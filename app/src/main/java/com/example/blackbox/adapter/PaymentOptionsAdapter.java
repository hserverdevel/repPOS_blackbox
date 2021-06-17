package com.example.blackbox.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.blackbox.R;
import com.example.blackbox.activities.PaymentActivity;
import com.example.blackbox.fragments.CalculatorFragment;
import com.example.blackbox.fragments.OrderFragment;
import com.example.blackbox.fragments.PaymentActivityCommunicator;
import com.example.blackbox.graphics.CustomButton;
import com.example.blackbox.model.CashButtonLayout;
import com.example.blackbox.model.Fidelity;
import com.example.blackbox.model.LeftPayment;
import com.example.blackbox.model.PaymentButton;
import com.utils.db.DatabaseAdapter;

import java.util.ArrayList;

import static com.example.blackbox.activities.PaymentActivity.CALCULATOR_ACTIVATION;
import static com.example.blackbox.activities.PaymentActivity.CALCULATOR_ACTIVATION_FOR_CREDIT;
import static com.example.blackbox.activities.PaymentActivity.CALCULATOR_ACTIVATION_FIDELITY;
import static com.example.blackbox.activities.PaymentActivity.CALCULATOR_ACTIVATION_TICKET;
import static com.example.blackbox.activities.PaymentActivity.CALCULATOR_INSERT_PARTIAL;
import static com.example.blackbox.activities.PaymentActivity.PAY_PARTIAL_BILL;
import static com.example.blackbox.activities.PaymentActivity.PAY_TOTAL_BILL;
import static com.example.blackbox.adapter.OrderListAdapter.INSERT_CREDIT_MODE;
import static com.example.blackbox.adapter.OrderListAdapter.INSERT_FIDELITY_MODE;
import static com.example.blackbox.adapter.OrderListAdapter.PAY_PARTIAL_MODE;
import static com.example.blackbox.adapter.OrderListAdapter.PAY_TICKET_MODE;
import static com.example.blackbox.adapter.OrderListAdapter.PAY_TOTAL_MODE;

/**
 * Created by DavideLiberato on 11/07/2017.
 */

public class PaymentOptionsAdapter extends RecyclerView.Adapter
{

    private final float density;
    private ArrayList<PaymentButton> buttons;
    private ArrayList<PaymentButton> previous_buttons;
    private DatabaseAdapter dbA;
    private Context context;
    private PaymentActivityCommunicator communicator;
    private LayoutInflater inflater;
    private RecyclerView parent;
    private boolean isActive = true;
    private int previous_parent; // which means the parent of the current buttons' parent
    private PaymentButton paymentButton = null;
    private boolean isCard;
    private boolean isPartial;
    private boolean isPartialSet = false;

    public void setIsPartial(Boolean b) {isPartial = b;}

    public boolean getIsCar()
    {
        return isCard;
    }

    public void setIsCar(boolean b)
    {
        isCard = b;
    }

    public int setPaymentButtonTitle(String title)
    {
        switch (title)
        {
            case "CASH":
                return R.string.cash;
            case "CREDIT CARD":
                return R.string.credit_card;
            case "BANK CARD":
                return R.string.bank_card;
            case "TICKETS":
                return R.string.tickets;
            case "CREDIT":
                return R.string.credit;
            case "PARTIAL":
                return R.string.partial;
            default:
                break;
        }
        return -1;
    }

    public PaymentOptionsAdapter(DatabaseAdapter dbA, Context context)
    {
        this.dbA = dbA;
        this.context = context;

        communicator = (PaymentActivityCommunicator) context;
        inflater = ((Activity) context).getLayoutInflater();

        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        density = context.getResources().getDisplayMetrics().density;
        buttons = new ArrayList<>();

        loadButtons(0);
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View itemView = inflater.inflate(R.layout.payment_button, null);
        return new ButtonHolder(itemView);
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {
        PaymentButton b = buttons.get(position);
        ButtonHolder bh = (ButtonHolder) holder;

        bh.button.setLayoutParams(new ViewGroup.LayoutParams((int) (322 * density), (int) (46 * density)));

        if (setPaymentButtonTitle(b.getTitle()) != -1)
        {
            bh.button.setText(setPaymentButtonTitle(b.getTitle()));
        }
        else
        {
            bh.button.setText(b.getTitle());
        }

        bh.button.setTag(position);

        if (isActive)
        {
            bh.button.setAlpha(1.0f);
            if (b.getFocused())
            {
                bh.button.setBackground(context.getDrawable(R.drawable.button_border_and_green));
            }
            else
            {
                bh.button.setBackground(context.getDrawable(R.color.green_2));
            }

            bh.button.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    doSomething(v);
                }
            });

        }
        else
        {
            bh.button.setAlpha(0.15f);
        }
    }

    @Override
    public int getItemViewType(int position)
    {
        return buttons.get(position).getButton_type();
    }

    @Override
    public int getItemCount()
    {
        return buttons != null
               ? buttons.size()
               : 0;
    }


    public void doSomething(View v)
    {
        int button_type = buttons.get((int) v.getTag()).getButton_type();

        PaymentButton processButton;
        PaymentButton totalButton;
        PaymentButton partialButton;

        OrderFragment of = ((OrderFragment) ((FragmentActivity) context).getSupportFragmentManager()
                                                                        .findFragmentByTag("order"));

        boolean greenButton = ((PaymentActivity) context).getGreenButton();

        // If bill was split and orderList is currently showing the total_bill then payment must not be allowed unless all bill
        // splittings are removed.
        if (((PaymentActivity) context).checkIfOtherSplitBillArePaid())
        {
            if (isActive && !greenButton)
            {
                switch (button_type)
                {
                    case 1:
                        if (!isPartial)
                        {
                            // buttons.get((int)v.getTag()).setFocused(true);
                            ((CalculatorFragment) ((FragmentActivity) context).getSupportFragmentManager()
                                                                              .findFragmentByTag("calc"))
                                    .setMode(PAY_TOTAL_MODE);
                            communicator.activateFunction(CALCULATOR_ACTIVATION, null, 0.0f);
                            isActive = false;
                            turnOnOffButtons((int) v.getTag(), false);
                            //VEDIAMO SE NON SERVE
                            //setActive(false);
                            ((PaymentActivity) context).setPaymentType(1);
                            ((Activity) context).findViewById(R.id.kill)
                                                .setOnClickListener(new View.OnClickListener()
                                                {
                                                    @Override
                                                    public void onClick(View v)
                                                    {
                                                        ArrayList<LeftPayment> leftPayments = ((CalculatorFragment) ((FragmentActivity) context)
                                                                .getSupportFragmentManager()
                                                                .findFragmentByTag("calc")).getLeftPayment();
                                                        if (leftPayments.size() > 0)
                                                        {
                                                            ((CalculatorFragment) ((FragmentActivity) context)
                                                                    .getSupportFragmentManager()
                                                                    .findFragmentByTag("calc")).setLastLeftPayment();
                                                        }

                                                        ((CalculatorFragment) ((FragmentActivity) context)
                                                                .getSupportFragmentManager()
                                                                .findFragmentByTag("calc")).turnOnOffCalculator();
                                                        ((PaymentActivity) context).activatePaymentButtonsSpec();

                                                        ((PaymentActivity) context).setNormalKillOkButton();

                                                        isActive = true;
                                                        paymentButton = null;

                                                        ((PaymentActivity) context).setKillForSplitOnCalculator();
                                                        ((PaymentActivity) context).showSubdivisionItem();
                                                    }
                                                });
                        }
                        else
                        {
                            for (PaymentButton b : buttons)
                            {
                                b.setFocused(false);
                            }
                            buttons.get((int) v.getTag()).setFocused(true);
                            notifyDataSetChanged();
                            ((CalculatorFragment) ((FragmentActivity) context).getSupportFragmentManager()
                                                                              .findFragmentByTag("calc"))
                                    .setMode(PAY_PARTIAL_MODE);

                            ((CalculatorFragment) ((FragmentActivity) context).getSupportFragmentManager()
                                                                              .findFragmentByTag("calc"))
                                    .setPayementShortcut();
                            if (!isPartialSet)
                            {
                                ((CalculatorFragment) ((FragmentActivity) context).getSupportFragmentManager()
                                                                                  .findFragmentByTag("calc"))
                                        .turnOnOffCalculator();
                            }
                            isPartialSet = true;
                            isActive = false;
                            turnOnOffButtons((int) v.getTag(), false);

                            ((CalculatorFragment) ((FragmentActivity) context).getSupportFragmentManager()
                                                                              .findFragmentByTag("calc"))
                                    .payFromPartialButton();
                            ((Activity) context).findViewById(R.id.kill)
                                                .setOnClickListener(new View.OnClickListener()
                                                {
                                                    @Override

                                                    public void onClick(View v)
                                                    {


                                                        ((CalculatorFragment) ((FragmentActivity) context)
                                                                .getSupportFragmentManager()
                                                                .findFragmentByTag("calc")).turnOnOffCalculator();
                                                        ((PaymentActivity) context).activatePaymentButtonsSpec();
                                                        //((PaymentActivity) context).activatePaymentButtons();
                                                        //loadButtons(0);
                                                        ((PaymentActivity) context).resetOpacityForSlplittButton();
                                                        ((PaymentActivity) context).resetOpacityForSplit();
                                                        ((PaymentActivity) context).setNormalKillOkButton();
                                                        isPartial = false;
                                                        isActive = true;
                                                        isPartialSet = false;
                                                        paymentButton = null;
                                                    }
                                                });


                        }
                        break;

                    case 2:
                        if (!isPartial)
                        {
                            ((PaymentActivity) context).setPaymentType(4);
                            ((CalculatorFragment) ((FragmentActivity) context).getSupportFragmentManager()
                                                                              .findFragmentByTag("calc"))
                                    .setMode(PAY_TOTAL_MODE);
                            communicator.activateFunction(CALCULATOR_ACTIVATION, null, 0.0f);
                            isActive = false;
                            turnOnOffButtons((int) v.getTag(), false);

                            /**
                             * this is the old part for credit card
                             * that open the accept credit card
                             */


                            /*if (((OrderFragment) ((FragmentActivity) context).getSupportFragmentManager().findFragmentByTag("order")).getOrderListAdapter().getSubdivisionItem() == null) {

                                ((CalculatorFragment) ((FragmentActivity) context).getSupportFragmentManager().findFragmentByTag("calc"))
                                        .setMode(PAY_TOTAL_MODE);
                                ((PaymentActivity) context).setPay_mode(PAY_TOTAL_BILL);
                                ((PaymentActivity) context).openProcessCardPopup();
                            } else {
                                ((CalculatorFragment) ((FragmentActivity) context).getSupportFragmentManager().findFragmentByTag("calc"))
                                        .setMode(PAY_PARTIAL_MODE);
                                ((PaymentActivity) context).setPay_mode(PAY_PARTIAL_BILL);
                                ((PaymentActivity) context).openProcessCardPopup();
                            }*/
                        }
                        else
                        {

                            ((CalculatorFragment) ((FragmentActivity) context).getSupportFragmentManager()
                                                                              .findFragmentByTag("calc"))
                                    .setPayementShortcut();
                            for (PaymentButton b : buttons)
                            {
                                b.setFocused(false);
                            }
                            buttons.get((int) v.getTag()).setFocused(true);
                            notifyDataSetChanged();
                            ((CalculatorFragment) ((FragmentActivity) context).getSupportFragmentManager()
                                                                              .findFragmentByTag("calc"))
                                    .setMode(PAY_PARTIAL_MODE);
                            ((PaymentActivity) context).setPay_mode(PAY_PARTIAL_BILL);
                            if (!isPartialSet)
                            {
                                ((CalculatorFragment) ((FragmentActivity) context).getSupportFragmentManager()
                                                                                  .findFragmentByTag("calc"))
                                        .turnOnOffCalculator();
                            }
                            isPartialSet = true;
                            isActive = false;
                            setIsCar(true);
                            turnOnOffButtons((int) v.getTag(), false);
                            //isPartial = false;
                            ((Activity) context).findViewById(R.id.kill)
                                                .setOnClickListener(new View.OnClickListener()
                                                {
                                                    @Override
                                                    public void onClick(View v)
                                                    {


                                                        ((CalculatorFragment) ((FragmentActivity) context)
                                                                .getSupportFragmentManager()
                                                                .findFragmentByTag("calc")).turnOnOffCalculator();
                                                        ((PaymentActivity) context).activatePaymentButtonsSpec();
                                                        ((PaymentActivity) context).resetOpacityForSplit();
                                                        //loadButtons(0);
                                                        isPartial = false;
                                                        isActive = true;
                                                        setIsCar(false);
                                                        //((PaymentActivity) context).resetOpacityForSlplittButton();
                                                        isPartialSet = false;
                                                        ((PaymentActivity) context).setNormalKillOkButton();
                                                        paymentButton = null;
                                                        ((PaymentActivity) context).showSubdivisionItem();
                                                    }
                                                });
                            //((PaymentActivity) context).openProcessCardPopup();
                            //isPartial = false;


                        }
                        break;

                    case 4:
                        // process bank card
                        // credit card container button - update button array with credit card type accepted (db)
                        if (!isPartial)
                        {
                            ((PaymentActivity) context).setPaymentType(4);
                            ((CalculatorFragment) ((FragmentActivity) context).getSupportFragmentManager()
                                                                              .findFragmentByTag("calc"))
                                    .setMode(PAY_TOTAL_MODE);
                            communicator.activateFunction(CALCULATOR_ACTIVATION, null, 0.0f);
                            isActive = false;
                            turnOnOffButtons((int) v.getTag(), false);

                            /*if (((OrderFragment) ((FragmentActivity) context).getSupportFragmentManager().findFragmentByTag("order")).getOrderListAdapter().getSubdivisionItem() == null) {

                                ((CalculatorFragment) ((FragmentActivity) context).getSupportFragmentManager().findFragmentByTag("calc"))
                                        .setMode(PAY_TOTAL_MODE);
                                ((PaymentActivity) context).setPay_mode(PAY_TOTAL_BILL);
                                ((PaymentActivity) context).openProcessCardPopup();
                            } else {
                                ((CalculatorFragment) ((FragmentActivity) context).getSupportFragmentManager().findFragmentByTag("calc"))
                                        .setMode(PAY_PARTIAL_MODE);
                                ((PaymentActivity) context).setPay_mode(PAY_PARTIAL_BILL);
                                ((PaymentActivity) context).openProcessCardPopup();
                            }*/
                        }
                        else
                        {

                            ((CalculatorFragment) ((FragmentActivity) context).getSupportFragmentManager()
                                                                              .findFragmentByTag("calc"))
                                    .setPayementShortcut();
                            for (PaymentButton b : buttons)
                            {
                                b.setFocused(false);
                            }
                            buttons.get((int) v.getTag()).setFocused(true);
                            notifyDataSetChanged();
                            ((CalculatorFragment) ((FragmentActivity) context).getSupportFragmentManager()
                                                                              .findFragmentByTag("calc"))
                                    .setMode(PAY_PARTIAL_MODE);
                            ((PaymentActivity) context).setPay_mode(PAY_PARTIAL_BILL);
                            if (!isPartialSet)
                            {
                                ((CalculatorFragment) ((FragmentActivity) context).getSupportFragmentManager()
                                                                                  .findFragmentByTag("calc"))
                                        .turnOnOffCalculator();
                            }
                            isPartialSet = true;
                            isActive = false;
                            setIsCar(true);
                            turnOnOffButtons((int) v.getTag(), false);
                            //isPartial = false;
                            ((Activity) context).findViewById(R.id.kill)
                                                .setOnClickListener(new View.OnClickListener()
                                                {
                                                    @Override
                                                    public void onClick(View v)
                                                    {


                                                        ((CalculatorFragment) ((FragmentActivity) context)
                                                                .getSupportFragmentManager()
                                                                .findFragmentByTag("calc")).turnOnOffCalculator();
                                                        ((PaymentActivity) context).activatePaymentButtonsSpec();
                                                        //loadButtons(0);
                                                        isPartial = false;
                                                        isActive = true;
                                                        setIsCar(false);
                                                        ((PaymentActivity) context).resetOpacityForSplit();
                                                        //((PaymentActivity) context).resetOpacityForSlplittButton();
                                                        isPartialSet = false;
                                                        ((PaymentActivity) context).setNormalKillOkButton();
                                                        paymentButton = null;
                                                        ((PaymentActivity) context).showSubdivisionItem();
                                                    }
                                                });
                            //((PaymentActivity) context).openProcessCardPopup();
                            //isPartial = false;


                        }
                        break;


                    case 3:
                        // actual credit card -> process card
                        processButton = new PaymentButton();
                        processButton.setTitle("PROCESS " + buttons.get((int) v.getTag())
                                                                   .getTitle() + " CARD");
                        //                        processButton.setButton_type(10);
                        processButton.setButton_type(13);
                        buttons.clear();
                        buttons.add(processButton);
                        notifyDataSetChanged();
                        break;

                    case 5:
                        ((PaymentActivity) context).setUpCalculatorShortcut();
                        paymentButton = buttons.get((int) v.getTag());
                        totalButton = new PaymentButton();
                        totalButton.setButton_type(14);
                        totalButton.setTitle("TICKET RESTAURANT");
                        totalButton.setFocused(false);
                        buttons.clear();
                        buttons.add(totalButton);

                        partialButton = new PaymentButton();
                        partialButton.setButton_type(14);
                        partialButton.setTitle("PELLEGRINI");
                        partialButton.setFocused(false);
                        buttons.add(partialButton);

                        ((PaymentActivity) context).hideSplitButton();
                        //loadButtons(0);
                        ((PaymentActivity) context).setOpacityForSplitButton();


                        notifyDataSetChanged();
                        ((Activity) context).findViewById(R.id.kill)
                                            .setOnClickListener(new View.OnClickListener()
                                            {
                                                @Override
                                                public void onClick(View v)
                                                {


                                                    ((PaymentActivity) context).activatePaymentButtonsSpec();
                                                    //loadButtons(0);
                                                    ((PaymentActivity) context).resetOpacityForSlplittButton();
                                                    ((PaymentActivity) context).setNormalKillOkButton();
                                                    ((PaymentActivity) context).showSplitButton();
                                                    paymentButton = null;
                                                    ((PaymentActivity) context).showSubdivisionItem();
                                                }
                                            });

                        break;

                    case 6:
                        //CREDIT BUTTON ON PAYMENTS OPTION
                        ((CalculatorFragment) ((FragmentActivity) context).getSupportFragmentManager()
                                                                          .findFragmentByTag("calc"))
                                .setMode(INSERT_CREDIT_MODE);
                        communicator.activateFunction(CALCULATOR_ACTIVATION_FOR_CREDIT, null, 0.0f);

                        turnOnOffButtons((int) v.getTag(), false);

                        ((Activity) context).findViewById(R.id.kill)
                                            .setOnClickListener(new View.OnClickListener()
                                            {
                                                @Override
                                                public void onClick(View v)
                                                {
                                                    ((CalculatorFragment) ((FragmentActivity) context)
                                                            .getSupportFragmentManager()
                                                            .findFragmentByTag("calc")).turnOnOffCalculator();
                                                    ((PaymentActivity) context).activatePaymentButtonsSpec();
                                                    // loadButtons(0);
                                                    isPartial = false;
                                                    //((PaymentActivity) context).resetOpacityForSlplittButton();
                                                    isPartialSet = false;
                                                    ((PaymentActivity) context).setNormalKillOkButton();
                                                    paymentButton = null;
                                                    ((PaymentActivity) context).showSubdivisionItem();
                                                }
                                            });

                        break;

                    case 7:
                        //PARTIAL PAYMENT
                        if (!isPartial)
                        {
                            loadButtonsPartial();
                            ((PaymentActivity) context).setOpacityForSplitButton();
                            ((PaymentActivity) context).buttonOpacitySetting1();
                            isPartial = true;
                            isPartialSet = false;

                            ((Activity) context).findViewById(R.id.kill)
                                                .setOnClickListener(new View.OnClickListener()
                                                {
                                                    @Override
                                                    public void onClick(View v)
                                                    {


                                                        ((PaymentActivity) context).activatePaymentButtonsSpec();
                                                        ((PaymentActivity) context).resetOtherButtons();
                                                        //loadButtons(0);
                                                        isPartial = false;
                                                        isActive = true;
                                                        //((PaymentActivity) context).resetOpacityForSlplittButton();

                                                        ((PaymentActivity) context).setNormalKillOkButton();
                                                        paymentButton = null;
                                                    }
                                                });


                        }
                        break;

                    // pay with fidelity credit option
                    case 8:

                        ((CalculatorFragment) ((FragmentActivity) context).getSupportFragmentManager()
                                                                          .findFragmentByTag("calc"))
                                .setMode(INSERT_FIDELITY_MODE);

                        communicator.activateFunction(CALCULATOR_ACTIVATION_FIDELITY, null, 0.0f);

                        // TODO
                        turnOnOffButtons((int) v.getTag(), false);

                        ((Activity) context).findViewById(R.id.kill)
                                            .setOnClickListener(new View.OnClickListener()
                                            {
                                                @Override
                                                public void onClick(View v)
                                                {
                                                    ((CalculatorFragment) ((FragmentActivity) context)
                                                            .getSupportFragmentManager()
                                                            .findFragmentByTag("calc")).turnOnOffCalculator();
                                                    ((PaymentActivity) context).activatePaymentButtonsSpec();
                                                    ((PaymentActivity) context).setNormalKillOkButton();
                                                    ((PaymentActivity) context).showSubdivisionItem();
                                                    isPartial = false;
                                                    isPartialSet = false;
                                                    paymentButton = null;
                                                }
                                            });


                    case 10: // process case
                        break;

                    /**
                     * Possible cases:
                     * - Bill was split into some sub-bills and the remaining of the bill is to be paid:
                     *      . pay total: pays total remaining, if everything is paid, close all, if not insert new
                     *                  bill split as "paid"
                     *      . pay partial: pays a certain amount of the remaining amount of the bill and puts it into
                     *                  bill splits as a "paid percentage"
                     * - Bill was not yet split into any sub-bill and everything is to be paid:
                     *      . pay total: simply has everything paid, as usual;
                     *      . pay partial: pays a certain amount, specified in calculator, which gets inserted as a new split
                     */
                    case 11: // 'pay total' case
                        if (isCard)
                        {
                            ((PaymentActivity) context).setPay_mode(PAY_TOTAL_BILL);
                            ((PaymentActivity) context).openProcessCardPopup();
                        }
                        else
                        {
                            ((CalculatorFragment) ((FragmentActivity) context).getSupportFragmentManager()
                                                                              .findFragmentByTag("calc"))
                                    .setMode(PAY_TOTAL_MODE);
                            communicator.activateFunction(CALCULATOR_ACTIVATION, null, 0.0f);


                            turnOnOffButtons((int) v.getTag(), false);
                            //VEDIAMO SE NON SERVE
                            //setActive(false);
                            ((Activity) context).findViewById(R.id.kill)
                                                .setOnClickListener(new View.OnClickListener()
                                                {
                                                    @Override
                                                    public void onClick(View v)
                                                    {


                                                        ((CalculatorFragment) ((FragmentActivity) context)
                                                                .getSupportFragmentManager()
                                                                .findFragmentByTag("calc")).turnOnOffCalculator();
                                                        ((PaymentActivity) context).activatePaymentButtonsSpec();
                                                        //loadButtons(0);
                                                        ((PaymentActivity) context).resetOpacityForSlplittButton();
                                                        ((PaymentActivity) context).setNormalKillOkButton();

                                                        paymentButton = null;
                                                    }
                                                });
                        }
                        break;

                    case 12: // 'pay partial' case
                        if (isCard)
                        {
                            communicator.activateFunction(CALCULATOR_INSERT_PARTIAL, null, 0.0f);
                            ((CalculatorFragment) ((FragmentActivity) context).getSupportFragmentManager()
                                                                              .findFragmentByTag("calc"))
                                    .setMode(PAY_PARTIAL_MODE);
                            turnOnOffButtons((int) v.getTag(), false);
                            //setActive(false);
                            ((Activity) context).findViewById(R.id.kill)
                                                .setOnClickListener(new View.OnClickListener()
                                                {
                                                    @Override
                                                    public void onClick(View v)
                                                    {


                                                        ((CalculatorFragment) ((FragmentActivity) context)
                                                                .getSupportFragmentManager()
                                                                .findFragmentByTag("calc")).turnOnOffCalculator();
                                                        ((PaymentActivity) context).activatePaymentButtonsSpec();
                                                        //loadButtons(0);
                                                        ((PaymentActivity) context).resetOpacityForSlplittButton();
                                                        ((PaymentActivity) context).setNormalKillOkButton();
                                                        paymentButton = null;
                                                    }
                                                });
                        }
                        else
                        {
                            communicator.activateFunction(CALCULATOR_INSERT_PARTIAL, null, 0.0f);
                            ((CalculatorFragment) ((FragmentActivity) context).getSupportFragmentManager()
                                                                              .findFragmentByTag("calc"))
                                    .setMode(PAY_PARTIAL_MODE);
                            turnOnOffButtons((int) v.getTag(), false);
                            //setActive(false);
                            ((Activity) context).findViewById(R.id.kill)
                                                .setOnClickListener(new View.OnClickListener()
                                                {
                                                    @Override
                                                    public void onClick(View v)
                                                    {


                                                        ((CalculatorFragment) ((FragmentActivity) context)
                                                                .getSupportFragmentManager()
                                                                .findFragmentByTag("calc")).turnOnOffCalculator();
                                                        ((PaymentActivity) context).activatePaymentButtonsSpec();
                                                        //loadButtons(0);
                                                        ((PaymentActivity) context).resetOpacityForSlplittButton();
                                                        ((PaymentActivity) context).setNormalKillOkButton();
                                                        paymentButton = null;
                                                        ((PaymentActivity) context).showSubdivisionItem();
                                                    }
                                                });
                        }
                        ((PaymentActivity) context).setOpacityForSplitButton();
                        break;

                    case 13:
                        ((PaymentActivity) context).openProcessCardPopup();
                        break;

                    case 14:
                        if (!paymentButton.getFocused())
                        {
                            paymentButton.setFocused(true);
                            ((CalculatorFragment) ((FragmentActivity) context).getSupportFragmentManager()
                                                                              .findFragmentByTag("calc"))
                                    .setMode(PAY_TICKET_MODE);
                            communicator.activateFunction(CALCULATOR_ACTIVATION_TICKET, null, 0.0f);

                            turnOnOffButtons((int) v.getTag(), false);
                            //VEDIAMO SE NON SERVE
                            //setActive(false);
                            ((Activity) context).findViewById(R.id.kill)
                                                .setOnClickListener(new View.OnClickListener()
                                                {
                                                    @Override
                                                    public void onClick(View view)
                                                    {


                                                        ((CalculatorFragment) ((FragmentActivity) context)
                                                                .getSupportFragmentManager()
                                                                .findFragmentByTag("calc")).turnOnOffCalculator();
                                                        ((PaymentActivity) context).activatePaymentButtonsSpec();
                                                        //loadButtons(0);
                                                        ((PaymentActivity) context).resetOpacityForSlplittButton();
                                                        ((PaymentActivity) context).setNormalKillOkButton();
                                                        paymentButton = null;
                                                        ((PaymentActivity) context).resetPaymentLeft();
                                                        //                 v.setActivated(false);
                                                        ((PaymentActivity) context).showSubdivisionItem();
                                                    }
                                                });
                        }

                        break;


                    default:
                        //  ((PaymentActivity) context).setOpacityForSlplittButton();
                        // default + missing sub-cases
                        // for all: setup the "go back button"
                }
                //}
                //((PaymentActivity) context).setOpacityForSlplittButton();
            }
        }
        else
        {
            if (!greenButton)
            {
                ((PaymentActivity) context).openPayOtherPopup();
            }
            //Toast.makeText(context, "Please pay other split bill first", Toast.LENGTH_SHORT).show();
        }
    }


    public void turnOnOffButtons(int exceptPosition, boolean on)
    {
        if (exceptPosition >= 0)
        {
            previous_buttons = new ArrayList<>(buttons);
            buttons.clear();
            if (!on)
            {
                buttons.add(previous_buttons.get(exceptPosition));
                buttons.get(0).setFocused(true);
            }
            else
            {
                for (int i = 0; i < previous_buttons.size(); i++)
                {
                    if (i != exceptPosition)
                    { buttons.add(previous_buttons.get(i)); }
                }
            }
        }
        else if (on)
        {
            if (previous_buttons == null)
            { loadButtons(0); }
            else
            { buttons = previous_buttons; }
            for (int i = 0; i < buttons.size(); i++)
            {
                buttons.get(i).setFocused(false);
            }
        }
        else
        {
            previous_buttons = new ArrayList<>(buttons);
            buttons.clear();
        }
        notifyDataSetChanged();
    }


    public void setPaymentButtonOpacityOnOff(boolean on)
    {
        if (on)
        {
            isActive = true;
            notifyDataSetChanged();
        }
        else
        {
            isActive = false;
            notifyDataSetChanged();
        }

    }


    public void loadButtons(int parent_id)
    {
        isPartial = false;

        buttons = dbA.getPaymentButtons(parent_id);

        notifyDataSetChanged();
    }


    public void loadButtonsPartial()
    {

        buttons = dbA.getPaymentButtonsForPartial();
        notifyDataSetChanged();
        int a = 1;
    }


    public void loadButtonsPaymentOnly()
    {

        buttons = dbA.getOnlyPaymentButtons();
        notifyDataSetChanged();
        int a = 1;
    }


    public void loadButtonsBuyFidelity()
    {
        buttons = dbA.getPaymentButtonsBuyFidelity();

        notifyDataSetChanged();
    }


    public void setActive(boolean active)
    {
        isActive = active;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView)
    {
        super.onAttachedToRecyclerView(recyclerView);
        parent = recyclerView;
    }

    public static class ButtonHolder extends RecyclerView.ViewHolder
    {
        public CustomButton button;

        public ButtonHolder(View itemView)
        {
            super(itemView);
            button = (CustomButton) itemView;
        }
    }


}
