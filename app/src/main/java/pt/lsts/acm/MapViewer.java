package pt.lsts.acm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class MapViewer extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    MapView map = null;
    ShowError showError = new ShowError();
    GPSTracker gpsLoc;
    private Context mContext;
    private boolean firstBack = true;
    private Handler customHandler;
    private GeoPoint startPoint;
    private Marker startMarker;
    private IMapController mapController;
    private boolean firstLockDisplay = true;
    private TextView textGpsLoc;
    private boolean flagControlColorGps = false;

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

        textGpsLoc = findViewById(R.id.textViewGpsLoc);
        ImageButton preferences = findViewById(R.id.imageButtonPref);
        preferences.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                showError.showInfoToast("Preferences!", mContext);
                Preferences();
            }
        });
        gpsLoc = new GPSTracker(this);

        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        map.setUseDataConnection(true);
        map.setClickable(true);
        map.setHapticFeedbackEnabled(true);
        mapController = map.getController();
        mapController.setZoom(7);
        startPoint = new GeoPoint(41.178035883, -8.59593006);
        map.getController().animateTo(startPoint);
        mapController.setCenter(startPoint);
        customHandler = new android.os.Handler();
        customHandler.postDelayed(updateTimerThread, 100);
        startMarker = new Marker(map);
    }

    public void Preferences() {
        //Toast.makeText(this, "Long Press", Toast.LENGTH_SHORT).show();
        View v = new View(mContext);
        v.setBottom(150);
        v.setScrollContainer(true);
        PopupMenu popup = new PopupMenu(mContext, v, Gravity.CENTER);
        popup.setOnMenuItemClickListener(MapViewer.this);
        popup.inflate(R.menu.popup_menu_map);
        popup.show();
    }

    //Run task periodically
    private Runnable updateTimerThread = new Runnable() {
        @SuppressLint("SetTextI18n")
        public void run() {
            customHandler.postDelayed(this, 1100);
            if(gpsLoc.HasNewPos()){
                updateMapLoc(gpsLoc.GetLocation());
                textGpsLoc.setText(" GPS Lock ");
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
                textGpsLoc.setText(" GPS Not Lock ");
                if(flagControlColorGps) {
                    textGpsLoc.setTextColor(Color.rgb(255,0,0));
                    flagControlColorGps = false;
                }
                else{
                    flagControlColorGps = true;
                    textGpsLoc.setTextColor(Color.rgb(130,18,18));
                }
            }
        }
    };

    private void updateMapLoc(Location location) {
        if(firstLockDisplay) {
            mapController.setZoom(18);
            GeoPoint center_pos = new GeoPoint(location.getLatitude(), location.getLongitude());
            startPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
            map.getController().animateTo(center_pos);
            firstLockDisplay = false;
        }
        //map.removeAllViewsInLayout();
        startPoint.setLatitude(location.getLatitude());
        startPoint.setLongitude(location.getLongitude());
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setIcon(getResources().getDrawable(R.drawable.ico_unknown));
        startMarker.setTitle("My position\nLat: "+location.getLatitude()+"\nLon: "+location.getLongitude());
        map.getOverlays().clear();
        map.getOverlays().add(startMarker);
        map.invalidate();
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
        Toast.makeText(this, "Selected Item: " +menuItem.getTitle(), Toast.LENGTH_SHORT).show();
        switch (menuItem.getItemId()) {
            case R.id.search_item:
                // do your code
                return true;
            case R.id.upload_item:
                // do your code
                return true;
            case R.id.copy_item:
                // do your code
                return true;
            case R.id.print_item:
                // do your code
                return true;
            case R.id.share_item:
                // do your code
                return true;
            case R.id.bookmark_item:
                // do your code
                return true;
            default:
                return false;
        }
    }
}
