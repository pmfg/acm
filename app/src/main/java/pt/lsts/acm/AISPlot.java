package pt.lsts.acm;

import android.content.Context;
import android.location.Location;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

/**
 * Created by pedro on 2/27/18.
 * LSTS - FEUP
 */

class AISPlot {

    class SystemInfoAIS {
        ArrayList<String> shipName = new ArrayList<>();
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
        showError.showErrorLogcat("MEU", "AIS");
        myFirebaseRef.child(message).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //showError.showErrorLogcat("MEU","added: "+dataSnapshot.getKey());
                parseInfoAIS(dataSnapshot.getKey());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                //showError.showErrorLogcat("MEU","changed: "+dataSnapshot.getKey());
                parseInfoAIS(dataSnapshot.getKey());
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                //showError.showErrorLogcat("MEU","removed: "+dataSnapshot.getKey());
                parseInfoAIS(dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void parseInfoAIS(String shipName) {
        if(!shipName.equals("position") && !shipName.equals("type") && !shipName.equals("updated_at")){
            if(systemInfoAIS.systemSizeAIS == 0){
                systemInfoAIS.shipName.add(systemInfoAIS.systemSizeAIS, shipName);
                systemInfoAIS.systemSizeAIS++;
            }else{
                boolean haveName = false;
                for(int i = 0; i < systemInfoAIS.systemSizeAIS; i++){
                    if(systemInfoAIS.shipName.get(i).equals(shipName)) {
                        haveName = true;
                        break;
                    }
                }

                if(!haveName){
                    systemInfoAIS.shipName.add(systemInfoAIS.systemSizeAIS, shipName);
                    systemInfoAIS.systemSizeAIS++;
                }
            }
        }
    }

    public int GetNumberShipsAIS(){
        return systemInfoAIS.systemSizeAIS;
    }
}
