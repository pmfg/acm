package pt.lsts.acm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.ScaleBarOverlay;

import java.util.List;

public class MapViewer extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener, SensorEventListener {

    MapView map = null;
    ShowError showError = new ShowError();
    GPSTracker gpsLoc;
    RipplesPosition ripples;
    private Context mContext;
    private boolean firstBack = true;
    private Handler customHandler;
    private Handler customHandlerAIS;
    private Handler customHandlerGarbagde;
    private Handler customHandlerRipples;
    private GeoPoint startPoint;
    private Marker startMarker;
    private Marker startMarkerRipples[];
    private Marker startMarkerAIS[];
    private TextView textGpsLoc;
    private boolean flagControlColorGps = false;
    private ImageView compassImage;
    private float currentDegree = 0f;
    private String UrlRipples = "http://ripples.lsts.pt/api/v1/systems/active";
    private boolean newRipplesData = false;
    private RipplesPosition.SystemInfo systemInfo;
    private RipplesPosition.SystemInfo backSystemInfo;
    private GeoPoint systemPosRipples;
    private GeoPoint systemPosAIS;
    private boolean haveGpsLoc = false;
    private boolean firstRunRipplesPull = true;
    private int timeoutRipplesPull = 10;
    private int timeoutAISPull = 10;
    SharedPreferences prefs;
    Location myLocation;
    GPSConvert gpsConvert = new GPSConvert();
    ScaleBarOverlay scaleBarOverlay;
    AISPlot ais;
    private int countAisTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            MainACM.mainActivity.finish();
        } catch (Exception ignored) {
        }

        super.onCreate(savedInstanceState);
        mContext = this;
        Context ctx = getApplicationContext();
        //important! set your user agent to prevent getting banned from the osm servers
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_map_viewer);

        ais = new AISPlot(mContext);

        prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        compassImage = findViewById(R.id.compass);
        try {
            SensorManager mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            assert mSensorManager != null;
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);
        }catch(Exception ioCompass){
            showError.showErrorPopUp("No compass", false, this);
            compassImage.setVisibility(View.INVISIBLE);
        }

        textGpsLoc = findViewById(R.id.textViewGpsLoc);
        ImageButton preferences = findViewById(R.id.imageButtonPref);
        preferences.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Preferences();
            }
        });
        gpsLoc = new GPSTracker(this);
        ripples = new RipplesPosition(this, UrlRipples);
        systemPosRipples = new GeoPoint(0,0);
        systemPosAIS = new GeoPoint(0,0);;
        SetMapOsmdroid();

        ais.getAISInfo();
    }

    private void SetMapOsmdroid() {
        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        map.setUseDataConnection(true);
        map.setClickable(true);
        map.setHapticFeedbackEnabled(true);

        IMapController mapController = map.getController();
        mapController.setZoom(8);
        startPoint = new GeoPoint(41.178035883, -8.59593006);
        map.getController().animateTo(startPoint);
        mapController.setCenter(startPoint);
        customHandler = new android.os.Handler();
        customHandler.postDelayed(updateTimerThread, 100);
        customHandlerGarbagde = new Handler();
        customHandlerGarbagde.postDelayed(updateTimerThreadGarbagde, 100);
        customHandlerRipples = new Handler();
        customHandlerRipples.postDelayed(updateTimerThreadRipples, 100);
        customHandlerAIS = new Handler();
        customHandlerAIS.postDelayed(updateTimerThreadAIS, 2000);
        startMarker = new Marker(map);
        startMarkerRipples = new Marker[2048];
        for(int i = 0; i < 2048; i++)
            startMarkerRipples[i] = new Marker(map);

        startMarkerAIS = new Marker[10024];
        for(int i = 0; i < 10024; i++)
            startMarkerAIS[i] = new Marker(map);

        scaleBarOverlay = new ScaleBarOverlay(map);
        List<Overlay> overlays = map.getOverlays();
        overlays.add(scaleBarOverlay);
    }

    public void Preferences() {
        View v = new View(mContext);
        v.setBottom(150);
        v.setScrollContainer(true);
        PopupMenu popup = new PopupMenu(mContext, v, Gravity.CENTER | Gravity.CENTER_HORIZONTAL);
        popup.setOnMenuItemClickListener(MapViewer.this);
        popup.inflate(R.menu.popup_menu_map);
        popup.show();
    }

    //Run task periodically
    private Runnable updateTimerThread = new Runnable() {
        @SuppressLint("SetTextI18n")
        public void run() {
            customHandler.postDelayed(this, 1200);
            if(gpsLoc.HasNewPos()){
                haveGpsLoc = true;
                //updateMapLoc(gpsLoc.GetLocation());
                if( gpsLoc.LocationProviderByGPS())
                    textGpsLoc.setText(" GPS Lock ");
                else
                    textGpsLoc.setText(" GPRS Lock ");
                if(flagControlColorGps) {
                    textGpsLoc.setTextColor(Color.GREEN);
                    flagControlColorGps = false;
                }
                else{
                    flagControlColorGps = true;
                    textGpsLoc.setTextColor(Color.rgb(18,130,18));
                }
            }
            else{
                if( gpsLoc.LocationProviderByGPS())
                    textGpsLoc.setText(" GPS Not Lock ");
                else
                    textGpsLoc.setText(" GPRS Not Lock ");
                if(flagControlColorGps) {
                    textGpsLoc.setTextColor(Color.rgb(255,0,0));
                    flagControlColorGps = false;
                }
                else{
                    flagControlColorGps = true;
                    textGpsLoc.setTextColor(Color.rgb(130,18,18));
                }
            }
            updateMapLoc();
        }
    };

    //Run task periodically - Ripples
    private Runnable updateTimerThreadRipples = new Runnable() {
        @SuppressLint("SetTextI18n")
        public void run() {
            customHandlerRipples.postDelayed(this, timeoutRipplesPull * 1000);
            if(timeoutRipplesPull != 1) {
                if (ripples.PullData(UrlRipples)) {
                    systemInfo = ripples.GetSystemInfoRipples();
                    showError.showInfoToast("New Pull Ripples: " + systemInfo.systemSize, mContext, false);
                    newRipplesData = true;
                }
                else{
                    showError.showInfoToast("Error: Pull Ripples!", mContext, false);
                }
            }
            timeoutRipplesPull = Integer.parseInt(prefs.getString("sync_frequency_ripples", "12"));
            UrlRipples = prefs.getString("url_ripples", UrlRipples);
            //showError.showErrorLogcat("MEU", prefs.getString("url_ripples", UrlRipples) + " - "+ UrlRipples);
        }
    };

    //Run task periodically - AIS
    private Runnable updateTimerThreadAIS = new Runnable() {
        @SuppressLint("SetTextI18n")
        public void run() {
            customHandlerAIS.postDelayed(this, timeoutAISPull * 1000);
            //if(timeoutAISPull != 1) {
            //    showError.showErrorLogcat("MEU", "size ais: "+ais.GetNumberShipsAIS());
            //}
            timeoutAISPull = Integer.parseInt(prefs.getString("sync_frequency_ais", "12"));
        }
    };

    //Run task periodically - garbage collection
    private Runnable updateTimerThreadGarbagde = new Runnable() {
        public void run() {
            customHandlerGarbagde.postDelayed(this, 20000);
            System.gc();
            Runtime.getRuntime().gc();
        }
    };

    private void updateMapLoc() {
        map.getOverlays().clear();
        //GPS
        if(haveGpsLoc) {
            haveGpsLoc = false;
            myLocation = gpsLoc.GetLocation();
            startPoint.setLatitude(myLocation.getLatitude());
            startPoint.setLongitude(myLocation.getLongitude());
            startMarker.setPosition(startPoint);
            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            startMarker.setIcon(getResources().getDrawable(R.drawable.ico_my_pos));
            startMarker.setTitle("My position\n" + gpsConvert.latLonToDM(myLocation.getLatitude(), myLocation.getLongitude()));
            map.getOverlays().add(startMarker);
        }
        else{
            if(myLocation != null){
                startPoint.setLatitude(myLocation.getLatitude());
                startPoint.setLongitude(myLocation.getLongitude());
                startMarker.setPosition(startPoint);
                startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                startMarker.setIcon(getResources().getDrawable(R.drawable.ico_my_pos));
                startMarker.setTitle("My last position\n" + gpsConvert.latLonToDM(myLocation.getLatitude(), myLocation.getLongitude()));
                map.getOverlays().add(startMarker);
            }
        }

        //RIPPLES
        if(newRipplesData){
            newRipplesData = false;
            firstRunRipplesPull = false;
            backSystemInfo = systemInfo;
            for(int i = 0; i < systemInfo.systemSize; i++){
                systemPosRipples.setCoords(systemInfo.coordinates[i].getLatitude(), systemInfo.coordinates[i].getLongitude());
                startMarkerRipples[i].setPosition(systemPosRipples);
                startMarkerRipples[i].setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                if(systemInfo.sysName[i].contains("lauv"))
                    startMarkerRipples[i].setIcon(getResources().getDrawable(R.drawable.ico_auv));
                else if(systemInfo.sysName[i].contains("ccu"))
                    startMarkerRipples[i].setIcon(getResources().getDrawable(R.drawable.ico_ccu));
                else if(systemInfo.sysName[i].contains("manta"))
                    startMarkerRipples[i].setIcon(getResources().getDrawable(R.drawable.ico_manta));
                else if(systemInfo.sysName[i].contains("spot"))
                    startMarkerRipples[i].setIcon(getResources().getDrawable(R.drawable.spot_icon));
                else
                    startMarkerRipples[i].setIcon(getResources().getDrawable(R.drawable.ico_unknown));

                startMarkerRipples[i].setTitle(systemInfo.sysName[i]+"\n"+systemInfo.last_update[i]+"\n"+
                        gpsConvert.latLonToDM(systemInfo.coordinates[i].getLatitude(), systemInfo.coordinates[i].getLongitude()));
                map.getOverlays().add(startMarkerRipples[i]);
            }
        }
        else if(!newRipplesData && !firstRunRipplesPull){
            for(int i = 0; i < backSystemInfo.systemSize; i++){
                systemPosRipples.setCoords(backSystemInfo.coordinates[i].getLatitude(), backSystemInfo.coordinates[i].getLongitude());
                startMarkerRipples[i].setPosition(systemPosRipples);
                startMarkerRipples[i].setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                if(backSystemInfo.sysName[i].contains("lauv"))
                    startMarkerRipples[i].setIcon(getResources().getDrawable(R.drawable.ico_auv));
                else if(backSystemInfo.sysName[i].contains("ccu"))
                    startMarkerRipples[i].setIcon(getResources().getDrawable(R.drawable.ico_ccu));
                else if(backSystemInfo.sysName[i].contains("manta"))
                    startMarkerRipples[i].setIcon(getResources().getDrawable(R.drawable.ico_manta));
                else if(backSystemInfo.sysName[i].contains("spot"))
                    startMarkerRipples[i].setIcon(getResources().getDrawable(R.drawable.spot_icon));
                else
                    startMarkerRipples[i].setIcon(getResources().getDrawable(R.drawable.ico_unknown));

                startMarkerRipples[i].setTitle(backSystemInfo.sysName[i]+"\n"+backSystemInfo.last_update[i]+"\n"+
                        gpsConvert.latLonToDM(backSystemInfo.coordinates[i].getLatitude(), backSystemInfo.coordinates[i].getLongitude()));
                map.getOverlays().add(startMarkerRipples[i]);
            }
        }

        if(countAisTime >= 2) {
            AISPlot.SystemInfoAIS mAIS = ais.GetDataAIS();
            if (mAIS.systemSizeAIS > 0) {
                for (int i = 0; i < mAIS.systemSizeAIS; i++) {
                    if(((System.currentTimeMillis() / 1000L) - (mAIS.lastUpdateAisShip.get(i)/ 1000L)) < 3600) {
                        //showError.showErrorLogcat("MEU", mAIS.shipName.get(i) + " | " + (System.currentTimeMillis() / 1000L) + " - " + (mAIS.lastUpdateAisShip.get(i) / 1000L) + " = " + ((System.currentTimeMillis() / 1000L) - (mAIS.lastUpdateAisShip.get(i) / 1000L)));
                        systemPosAIS.setCoords(mAIS.shipLocation.get(i).getLatitude(), mAIS.shipLocation.get(i).getLongitude());
                        startMarkerAIS[i].remove(map);
                        startMarkerAIS[i].setPosition(systemPosAIS);
                        startMarkerAIS[i].setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                        startMarkerAIS[i].setIcon(getResources().getDrawable(R.drawable.ship_icon));
                        startMarkerAIS[i].setTitle(mAIS.shipName.get(i) + "\n" + gpsConvert.latLonToDM(mAIS.shipLocation.get(i).getLatitude(), mAIS.shipLocation.get(i).getLongitude()) +
                                "\n" + ais.parseTime(mAIS.lastUpdateAisShip.get(i)) + "\nHeading: " + mAIS.headingAisShip.get(i) + " | Speed: " + mAIS.speedAisShip.get(i) + " m/s");
                        map.getOverlays().add(startMarkerAIS[i]);
                    }
                }
            }
            countAisTime = -1;
        }
        else{
            for (int i = 0; i < ais.GetNumberShipsAIS(); i++)
                map.getOverlays().add(startMarkerAIS[i]);
        }
        countAisTime++;

        drawScaleBar(map);
        map.invalidate();
    }

    private void drawScaleBar(MapView map) {
        if (scaleBarOverlay != null) {
            scaleBarOverlay.setAlignRight(true);
            scaleBarOverlay.setEnableAdjustLength(true);
            scaleBarOverlay.setScaleBarOffset(10, 90);
            map.getOverlays().add(scaleBarOverlay);
        } else {
            scaleBarOverlay = new ScaleBarOverlay(map);
            scaleBarOverlay.setAlignRight(true);
            scaleBarOverlay.setEnableAdjustLength(true);
            scaleBarOverlay.setScaleBarOffset(10, 90);
            map.getOverlays().add(scaleBarOverlay);
        }
    }

    public void onResume(){
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Configuration.getInstance().save(this, prefs);
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        //map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }
    public void onPause(){
        super.onPause();
        //map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
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

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        //showError.showInfoToast("Selected Item: " +menuItem.getTitle(), this, false);
        firstBack = true;
        switch (menuItem.getItemId()) {
            case R.id.soi_item:
                Intent intent = new Intent(mContext, SOIActivity.class);
                startActivity(intent);
                return true;
            case R.id.settings_item:
                Intent intentSettings = new Intent(mContext, SettingsActivity.class);
                startActivity(intentSettings);
                return true;
            case R.id.exit_item:
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show();
                this.finish();
                System.exit(0);
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // get the angle around the z-axis rotated
        float degree = Math.round(sensorEvent.values[0]);
        //Log.i(TAG, "Heading: " + Float.toString(degree) + " degrees : "+sensorEvent.sensor.getName() );
        // create a rotation animation (reverse turn degree degrees)
        RotateAnimation ra = new RotateAnimation(
                currentDegree,
                -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);

        // how long the animation will take place
        ra.setDuration(210);
        // set the animation after the end of the reservation status
        ra.setFillAfter(true);
        // Start the animation
        compassImage.startAnimation(ra);
        currentDegree = -degree;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}
}
