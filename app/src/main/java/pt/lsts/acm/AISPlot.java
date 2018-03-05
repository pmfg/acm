package pt.lsts.acm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * Created by pedro on 2/27/18.
 * LSTS - FEUP
 */

class AISPlot {

    class SystemInfoAIS {
        ArrayList<String> shipName = new ArrayList<>();
        ArrayList<Location> shipLocation = new ArrayList<>();
        ArrayList<Long> lastUpdateAisShip = new ArrayList<>();
        ArrayList<Double> headingAisShip = new ArrayList<>();
        ArrayList<Double> speedAisShip = new ArrayList<>();
        int systemSizeAIS;
    }

    private SystemInfoAIS systemInfoAIS;
    private Context mContext;
    private ShowError showError = new ShowError();
    private FirebaseAuth mAuth;
    private Firebase myFirebaseRef;
    private String URlPath = "https://neptus.firebaseio.com/";
    String message = "ships";

    public AISPlot(Context context) {
        mContext = context;
        Firebase.setAndroidContext(context);
        myFirebaseRef = new Firebase(URlPath);
        systemInfoAIS = new SystemInfoAIS();
        systemInfoAIS.systemSizeAIS = 0;
    }

    public void getAISInfo(){
        //showError.showErrorLogcat("MEU", "AIS");
        myFirebaseRef.child(message).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //showError.showErrorLogcat("MEU","added: "+dataSnapshot);
                parseInfoAIS(dataSnapshot.getKey(), dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                //showError.showErrorLogcat("MEU","changed: "+dataSnapshot.getKey());
                parseInfoAIS(dataSnapshot.getKey(), dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                //showError.showErrorLogcat("MEU","removed: "+dataSnapshot.getKey());
                parseInfoAIS(dataSnapshot.getKey(), dataSnapshot);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void parseInfoAIS(String shipName, DataSnapshot dataSnapshot) {
        try {
            if (!shipName.equals("position") && !shipName.equals("type") && !shipName.equals("updated_at")) {
                if (systemInfoAIS.systemSizeAIS == 0) {
                    systemInfoAIS.shipName.add(systemInfoAIS.systemSizeAIS, shipName);
                    //showError.showErrorLogcat("MEU","added: "+shipName);
                    getInfoOfShip(shipName, systemInfoAIS.systemSizeAIS, dataSnapshot, true);
                    systemInfoAIS.systemSizeAIS++;
                } else {
                    int idMatch = 0;
                    boolean haveName = false;
                    for (int i = 0; i < systemInfoAIS.systemSizeAIS; i++) {
                        if (systemInfoAIS.shipName.get(i).equals(shipName)) {
                            haveName = true;
                            idMatch = i;
                            break;
                        }
                    }

                    if (!haveName) {
                        systemInfoAIS.shipName.add(systemInfoAIS.systemSizeAIS, shipName);
                        //showError.showErrorLogcat("MEU","added: "+shipName);
                        getInfoOfShip(shipName, systemInfoAIS.systemSizeAIS, dataSnapshot, true);
                        systemInfoAIS.systemSizeAIS++;
                    }
                    else{
                        getInfoOfShip(shipName, idMatch, dataSnapshot, false);
                    }
                }
            }
        }catch (Exception io){
            io.printStackTrace();
        }
    }

    private void getInfoOfShip(String shipName, int id, DataSnapshot dataSnapshot, boolean newLocation) {
        //ZEZERE, value = {updated_at=1519841484000, type=69, position={latitude=38.66656, heading=511.0, speed=0.4, cog=323.3, mmsi=263047004, longitude=-9.14569}} }
        Map<String, Object> result = (Map<String, Object>) dataSnapshot.getValue();
        Map<String, Object> result2 = (Map<String, Object>) result.get("position");
        //showError.showErrorLogcat("MEU", "info: " + result.get("updated_at") + " lat: " + result2.get("latitude") + " lon: " + result2.get("longitude")+" ID: "+id);
        systemInfoAIS.lastUpdateAisShip.add(id, (Long) result.get("updated_at"));
        systemInfoAIS.headingAisShip.add(id, (Double) result2.get("heading"));
        systemInfoAIS.speedAisShip.add(id, (Double) result2.get("speed"));
        if(newLocation){
            Location back = new Location("AIS: "+shipName);
            back.setLatitude(Double.parseDouble(result2.get("latitude").toString()));
            back.setLongitude(Double.parseDouble(result2.get("longitude").toString()));
            systemInfoAIS.shipLocation.add(id, back);
        }else{
            Location back = systemInfoAIS.shipLocation.get(id);
            back.setLatitude(Double.parseDouble(result2.get("latitude").toString()));
            back.setLongitude(Double.parseDouble(result2.get("longitude").toString()));
            systemInfoAIS.shipLocation.add(id, back);
        }
    }

    public int GetNumberShipsAIS(){
        return systemInfoAIS.systemSizeAIS;
    }

    public double getLatitudeShip(int id){
        return systemInfoAIS.shipLocation.get(id).getLatitude();
    }

    public double getLongitudeShip(int id){
        return systemInfoAIS.shipLocation.get(id).getLongitude();
    }

    public String getNameShipId(int id){
        return systemInfoAIS.shipName.get(id);
    }

    public String getHeadingShipId(int id) {
        if(systemInfoAIS.headingAisShip.get(id) == 511)
            return null;
        else
            return String.valueOf(systemInfoAIS.headingAisShip.get(id));
    }

    public Double getSpeedShipId(int id) { return systemInfoAIS.speedAisShip.get(id); }

    public Long getLastUpShipId(int id){
        return systemInfoAIS.lastUpdateAisShip.get(id);
    }


    @SuppressLint("DefaultLocale")
    public String parseTime(Long unixTime) {
        Date today = Calendar.getInstance().getTime();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDateTimeString = formatter.format(today);
        Date date = new Date(unixTime);
        String dateTimeString = formatter.format(date);

        Date AISTime;
        Date androidTime;
        try {
            AISTime = formatter.parse(dateTimeString);
            androidTime = formatter.parse(currentDateTimeString);
            long diffSeconds = Math.abs(androidTime.getTime() - AISTime.getTime()) / 1000;
            return "Last Up: " + String.format("%02dh %02dm %02ds", (diffSeconds/3600), (diffSeconds % 3600) / 60, diffSeconds % 60);
        } catch (ParseException e) {
            e.printStackTrace();
            return "null";
        }

    }
}
