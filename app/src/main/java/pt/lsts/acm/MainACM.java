package pt.lsts.acm;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.Toast;

public class MainACM extends AppCompatActivity {

    private boolean firstBack = true;
    protected static Activity mainActivity;
    ShowError showError = new ShowError();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivity = this;
        if(Build.VERSION.SDK_INT <= 19){
            checkConnections();
        }
        else {
            requestForSpecificPermission();
        }
    }

    private boolean isWifiAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo wifi = null;
        if (cm != null) {
            wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        }
        return wifi != null && wifi.isConnected();

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = null;
        if (connectivityManager != null) {
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void requestForSpecificPermission() {
        int PERMISSION_ALL = 101;
        String[] PERMISSIONS = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                //Manifest.permission.SEND_SMS,
                //Manifest.permission.READ_CONTACTS,
                //Manifest.permission.CAMERA,
                Manifest.permission.VIBRATE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                //Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.INTERNET};

        hasPermissions(this, PERMISSIONS);
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
    }

    public void hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                ActivityCompat.checkSelfPermission(context, permission);
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        boolean result_permission = true;
        for (int grantResult : grantResults) {
            if (grantResult == -1)
                result_permission = false;
        }
        if (!result_permission) {
            showError.showErrorPopUp("Please accept the permissions!!!\nIt is necessary to accept the permissions to run da app!!!", true, this);
        } else {
            checkConnections();
        }
    }

    private void checkConnections(){
        if (isWifiAvailable() || isNetworkAvailable()) {
            Toast.makeText(this, "ready to start", Toast.LENGTH_SHORT).show();
            new CountDownTimer(5000, 1000) {
                public void onFinish() {
                    // When timer is finished
                    // Execute your code here
                    Intent intent = new Intent(MainACM.this, MapViewer.class);
                    startActivity(intent);
                }
                public void onTick(long millisUntilFinished) {
                }
            }.start();
        }
        else {
            showError.showErrorPopUp("No network available...\nPlease turn on Wifi/3G", true, this);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(firstBack){
                firstBack = false;
                Toast.makeText(this, "Press back again to exit", Toast.LENGTH_LONG).show();
            }
            else{
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show();
                this.finish();
                System.exit(0);
            }
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
}
