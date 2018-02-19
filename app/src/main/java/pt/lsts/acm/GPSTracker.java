package pt.lsts.acm;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

/**
 * Created by pedro on 2/16/18.
 * LSTS - FEUP
 */

public final class GPSTracker{
    private ShowError showError = new ShowError();
    private boolean hasNewPos = false;
    private Location locationGPS;
    private boolean isGpsLocation = false;

    GPSTracker(final Context context) {
        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                makeUseOfNewLocation(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
                showError.showInfoToast("Gps turned on ", context, true);
            }

            public void onProviderDisabled(String provider) {
                showError.showInfoToast("Gps is off, please turn on ", context, true);
            }
        };

        // Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        }
        if (locationManager != null) {
            //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, locationListener);
            try {
                if (locationManager.getProvider(LocationManager.GPS_PROVIDER) == null) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
                    isGpsLocation = false;
                }
                else {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
                    isGpsLocation = true;
                }
            }catch(Exception gps){
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
                isGpsLocation = false;
            }
        }
    }

    private void makeUseOfNewLocation(Location location) {
        //showError.showErrorLogcat("MEU", location.getLatitude()+" : "+location.getLongitude());
        if(!hasNewPos) {
            locationGPS = location;
            hasNewPos = true;
        }
    }

    public boolean LocationProviderByGPS(){
        return isGpsLocation;
    }

    public boolean HasNewPos (){
        return hasNewPos;
    }

    public Location GetLocation(){
        hasNewPos = false;
        return locationGPS;
    }
}
