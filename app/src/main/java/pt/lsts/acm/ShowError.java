package pt.lsts.acm;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by pedro on 2/16/18.
 * LSTS - FEUP
 */

class ShowError {

    void showErrorPopUp(String text, final boolean exitApp, Activity context){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle("Error !!!");
        alertDialogBuilder.setMessage(text)
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(exitApp)
                         System.exit(0);
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    void showErrorPopUpCloseActivity(String text, final Activity context){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle("Error !!!");
        alertDialogBuilder.setMessage(text)
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        context.finish();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    void showPopUp(String title, String text, Activity context){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setMessage(text)
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    void showErrorLogcat(String labelId, String text){
        Log.i(labelId, text);
    }

    void showInfoToast(String text, Context context, boolean longShow){
        if(longShow)
            Toast.makeText(context, text, Toast.LENGTH_LONG).show();
        else
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }
}
