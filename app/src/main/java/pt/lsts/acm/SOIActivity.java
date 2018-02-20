package pt.lsts.acm;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

public class SOIActivity extends AppCompatActivity {

    class SOIInfo {
        String[] sysName = new String[32000];
        String[] plan = new String[32000];
        double[] duration = new double[32000];
        String[] eta = new String[32000];
        Location[][] waypoints = new Location[32000][1024];
        int[] waypointsSize = new int[32000];
        int SOIInfoSize;
    }

    ShowError showError = new ShowError();
    private String UrlRipplesSoi = "http://ripples.lsts.pt/soi";
    SOIInfo soiInfo = new SOIInfo();
    SOIListAdapter adapter;
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soi);
        mContext = this;

        ArrayList<SystemDetail> arrayOfSystemDetail = new ArrayList<SystemDetail>();
        adapter = new SOIListAdapter(this, arrayOfSystemDetail);
        ListView list = findViewById(R.id.listview);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                showError.showPopUp("WayPoints:", getWaypoints(i), (Activity) mContext);
            }
        });

        try {
            showError.showErrorLogcat("MEU", ParseDataRipplesSOI(new RetrieveDataRipplesSOI().execute(UrlRipplesSoi).get()));
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
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
                    soiInfo.sysName[i] = jsonobject.getString("name");
                }catch(JSONException io){
                    soiInfo.sysName[i] = "null";
                }

                objectIn = jsonobject.getJSONObject("plan");
                for(int t = 0; t < soiInfo.SOIInfoSize; t++) {
                    try {
                        soiInfo.plan[i] = objectIn.get("id").toString();
                    }catch(JSONException io){
                        io.printStackTrace();
                        soiInfo.plan[i] = "null";
                    }

                    try {
                        JSONArray arrayWaypoints = objectIn.getJSONArray("waypoints");
                        soiInfo.waypointsSize[i] = arrayWaypoints.length();
                        for(int v = 0; v < soiInfo.waypointsSize[i]; v++){
                            objectWaypoints = arrayWaypoints.getJSONObject(v);
                            try{
                                double lat = Double.parseDouble(objectWaypoints.getString("latitude"));
                                double lon = Double.parseDouble(objectWaypoints.getString("longitude"));
                                soiInfo.waypoints[i][v] = new Location(soiInfo.sysName[i]);
                                soiInfo.waypoints[i][v].setLatitude(lat);
                                soiInfo.waypoints[i][v].setLongitude(lon);
                            }catch (JSONException io){
                                soiInfo.waypoints[i][v] = new Location(soiInfo.sysName[i]);
                                soiInfo.waypoints[i][v].setLatitude(0);
                                soiInfo.waypoints[i][v].setLongitude(0);
                            }

                            try {
                                soiInfo.duration[i] = Double.parseDouble(objectWaypoints.getString("duration"));
                            }catch(JSONException io){
                                soiInfo.duration[i] = 0;
                            }

                            try {
                                soiInfo.eta[i] = parseTime(objectWaypoints.getString("eta"));
                            }catch(JSONException io){
                                soiInfo.eta[i] = "null";
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
        showData();
        return "ok";
    }

    private void showData() {
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

            String firstLine = "plan: "+soiInfo.plan[i] + " | timeOut: " + soiInfo.duration[i] + " sec\nETA: " + soiInfo.eta[i];
            //showError.showErrorLogcat("MEU", firstLine);


            SystemDetail data = new SystemDetail(soiInfo.sysName[i], firstLine);
            adapter.add(data);
        }
    }

    private String getWaypoints(int id){
        String secondLine = "";
        for(int t = 0; t < soiInfo.waypointsSize[id]; t++){
            showError.showErrorLogcat("MEU", "Point: "+t);
            showError.showErrorLogcat("MEU", "    Lat: "+soiInfo.waypoints[id][t].getLatitude());
            showError.showErrorLogcat("MEU", "    Lon: "+soiInfo.waypoints[id][t].getLongitude());
            secondLine = secondLine + "Lat: "+soiInfo.waypoints[id][t].getLatitude() + " | Lon: "+soiInfo.waypoints[id][t].getLongitude()+"\n";
        }
        return secondLine;
    }

    private String parseTime(String s) {
        long soiSec = Long.parseLong(s);
        Date date = new Date(soiSec*1000L);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT-0"));
        return sdf.format(date);
    }
}
