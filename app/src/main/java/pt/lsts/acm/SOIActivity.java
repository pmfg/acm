package pt.lsts.acm;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

import pl.droidsonroids.gif.GifImageView;

public class SOIActivity extends AppCompatActivity {

    class SOIInfo {
        ArrayList<String> sysName = new ArrayList<>();
        ArrayList<String> plan = new ArrayList<>();
        ArrayList<Double> duration = new ArrayList<>();
        ArrayList<String> eta = new ArrayList<>();
        Location[][] waypoints = new Location[32000][128];
        ArrayList<Integer> waypointsSize = new ArrayList<>();
        int SOIInfoSize;
    }

    ShowError showError = new ShowError();
    private String UrlRipplesSoi = "http://ripples.lsts.pt/soi";
    private SOIInfo soiInfo;
    SOIListAdapter adapter;
    Context mContext;
    SharedPreferences prefs;
    ListView list;
    GifImageView refreshGif;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.soi_bar, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soi);
        list = findViewById(R.id.listview);
        refreshGif = findViewById(R.id.imageViewRefreshGif);
        refreshGif.setVisibility(View.VISIBLE);
        mContext = this;
        prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        soiInfo = new SOIInfo();
        getInfoOfPlanSoi();
    }

    private void getInfoOfPlanSoi(){
        String result = "";
        try {
            String urlSoi = prefs.getString("url_soi", UrlRipplesSoi);
            result = ParseDataRipplesSOI(new RetrieveDataRipplesSOI().execute(urlSoi).get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        if(result.equals("null")){
            showError.showErrorPopUpCloseActivity("No valid data from SOI Url!", (Activity) mContext);
        }
    }

    private String ParseDataRipplesSOI(String dataPull) {
        JSONArray array;
        JSONObject objectIn;
        JSONObject objectWaypoints;
        try {
            array = new JSONArray(dataPull);
            soiInfo.SOIInfoSize = array.length();
            for (int i = 0; i < soiInfo.SOIInfoSize; i++) {
                JSONObject jsonobject = array.getJSONObject(i);
                try {
                    soiInfo.sysName.add(i, jsonobject.getString("name"));
                }catch(JSONException io){
                    soiInfo.sysName.add(i, jsonobject.getString("null"));
                }

                objectIn = jsonobject.getJSONObject("plan");
                for(int t = 0; t < soiInfo.SOIInfoSize; t++) {
                    try {
                        soiInfo.plan.add(i, objectIn.get("id").toString());
                    }catch(JSONException io){
                        io.printStackTrace();
                        soiInfo.plan.add(i, "null");
                    }

                    try {
                        JSONArray arrayWaypoints = objectIn.getJSONArray("waypoints");
                        soiInfo.waypointsSize.add(i, arrayWaypoints.length());
                        for(int v = 0; v < soiInfo.waypointsSize.get(i); v++){
                            objectWaypoints = arrayWaypoints.getJSONObject(v);
                            try{
                                double lat = Double.parseDouble(objectWaypoints.getString("latitude"));
                                double lon = Double.parseDouble(objectWaypoints.getString("longitude"));
                                soiInfo.waypoints[i][v] = new Location(soiInfo.sysName.get(i));
                                soiInfo.waypoints[i][v].setLatitude(lat);
                                soiInfo.waypoints[i][v].setLongitude(lon);
                            }catch (JSONException io){
                                soiInfo.waypoints[i][v] = new Location(soiInfo.sysName.get(i));
                                soiInfo.waypoints[i][v].setLatitude(0);
                                soiInfo.waypoints[i][v].setLongitude(0);
                            }

                            try {
                                soiInfo.duration.add(i, Double.parseDouble(objectWaypoints.getString("duration")));
                            }catch(JSONException io){
                                soiInfo.duration.add(i, 0.0);
                            }

                            try {
                                soiInfo.eta.add(i, parseTime(objectWaypoints.getString("eta")));
                            }catch(JSONException io){
                                soiInfo.eta.add(i, "null");
                            }
                        }
                    }catch(JSONException io){
                        io.printStackTrace();
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return "null";
        }
        if(soiInfo.SOIInfoSize > 0)
            showData();
        else {
            showError.showPopUp("SOI Plans", "No SOI Plans!!!", (Activity) mContext);
            new CountDownTimer(4000, 100) {
                public void onFinish() {
                    Runtime.getRuntime().gc();
                    finish();
                }
                public void onTick(long millisUntilFinished) {
                }
            }.start();
        }
        return "ok";
    }

    private void showData() {
        ArrayList<SystemDetail> arrayOfSystemDetail = new ArrayList<SystemDetail>();
        adapter = new SOIListAdapter(this, arrayOfSystemDetail);
        refreshGif.setVisibility(View.GONE);
        list.setVisibility(View.VISIBLE);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                showError.showPopUp("WayPoints:", getWaypoints(i), (Activity) mContext);
            }
        });

        for(int i = 0; i < soiInfo.SOIInfoSize; i++){
            /*showError.showErrorLogcat("MEU", "SysName: "+soiInfo.sysName[i]);
            showError.showErrorLogcat("MEU", "Plan SOI: "+soiInfo.plan[i]);
            showError.showErrorLogcat("MEU", "Waypoints:");
            String secondLine = "";
            for(int t = 0; t < soiInfo.waypointsSize[i]; t++){
                showError.showErrorLogcat("MEU", "Point: "+t);
                showError.showErrorLogcat("MEU", "    Lat: "+soiInfo.waypoints[i][t].getLatitude());
                showError.showErrorLogcat("MEU", "    Lon: "+soiInfo.waypoints[i][t].getLongitude());
                secondLine = secondLine + "Lat: "+soiInfo.waypoints[i][t].getLatitude() + " | Lon: "+soiInfo.waypoints[i][t].getLongitude()+"\n";
            }
            showError.showErrorLogcat("MEU", "Duration: "+soiInfo.duration[i]);
            showError.showErrorLogcat("MEU", "ETA: "+soiInfo.eta[i]);
            showError.showErrorLogcat("MEU", " ");*/

            String firstLine = "plan: "+soiInfo.plan.get(i) + " | timeOut: " + soiInfo.duration.get(i) + " sec\nETA: " + soiInfo.eta.get(i);
            //showError.showErrorLogcat("MEU", firstLine);


            SystemDetail data = new SystemDetail(soiInfo.sysName.get(i), firstLine);
            adapter.add(data);
        }
    }

    private String getWaypoints(int id){
        String wayPointsInfo = "";
        for(int t = 0; t < soiInfo.waypointsSize.get(id); t++){
            wayPointsInfo = wayPointsInfo + "Lat: "+soiInfo.waypoints[id][t].getLatitude() + " | Lon: "+soiInfo.waypoints[id][t].getLongitude()+"\n";
        }
        return wayPointsInfo;
    }

    private String parseTime(String s) {
        long soiSec = Long.parseLong(s);
        Date date = new Date(soiSec*1000L);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT-0"));
        return sdf.format(date);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Runtime.getRuntime().gc();
            finish();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                list.setVisibility(View.GONE);
                refreshGif.setVisibility(View.VISIBLE);
                new CountDownTimer(4000, 100) {
                    public void onFinish() {
                        getInfoOfPlanSoi();
                    }
                    public void onTick(long millisUntilFinished) {
                    }
                }.start();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }
}
