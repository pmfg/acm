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
    void showErrorLogcat(String labelId, String text){
        Log.i(labelId, text);
    }

    void showInfoToast(String text, Context context){
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }
}
