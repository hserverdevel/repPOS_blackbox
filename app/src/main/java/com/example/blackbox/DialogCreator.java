package com.example.blackbox;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;



public class DialogCreator
{

    public static void error(Context context, String msg)
    {
        // TODO use R.style.AlertDialogCustom
        new AlertDialog.Builder(context)
                .setTitle(R.string.error)
                .setMessage(msg)

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which)
                        { dialog.dismiss(); }
                })
                .show();
    }



    public static void error(Context context, int stringMsg)
    {
        // TODO use R.style.AlertDialogCustom
        new AlertDialog.Builder(context)
                .setTitle(R.string.error)
                .setMessage(context.getString(stringMsg))

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which)
                    { dialog.dismiss(); }
                })
                .show();
    }


    public static void message(Context context, String msg)
    {
        new AlertDialog.Builder(context)
                .setTitle("Notice")
                .setMessage(msg)

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which)
                    { dialog.dismiss(); }
                })
                .show();
    }

    public static void message(Context context, int stringMsg)
    {
        new AlertDialog.Builder(context)
                .setTitle("Notice")
                .setMessage(context.getString(stringMsg))

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which)
                    { dialog.dismiss(); }
                })
                .show();
    }



}
