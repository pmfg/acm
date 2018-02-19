package pt.lsts.acm;

import android.content.Context;
import android.location.Location;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by pedro on 2/19/18.
 * LSTS - FEUP
 */

class RipplesPosition {

    class SystemInfo {
        String[] imcid = new String[32000];
        String[] sysName = new String[32000];
        String[] update_at = new String[32000];
        String[] created_at = new String[32000];
        Location[] coordinates = new Location[32000];
        Double[] lonCoord = new Double[32000];
        int systemSize;
    }

    private ShowError showError = new ShowError();
    private SystemInfo systemInfo = new SystemInfo();
    private Context mContext;
    private String UrlPath;
    private boolean updateBuffer = true;

    RipplesPosition(Context context, String urlRipples) {
        mContext = context;
        UrlPath = urlRipples;
    }

    Boolean PullData() {
        //String dataPull = "none";
        try {
            //dataPull = new RetrieveDataRipples().execute(UrlPath).get();
            return ParseDataRipples(new RetrieveDataRipples().execute(UrlPath).get());
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private boolean ParseDataRipples(String dataPull) {
        JSONArray array;
        if(updateBuffer) {
            try {
                array = new JSONArray(dataPull);
                systemInfo.systemSize = array.length();
                //showError.showErrorLogcat("MEU", "SIZE: " + sizeSystemInPullRipples);
                for (int i = 0; i < systemInfo.systemSize; i++) {
                    JSONObject jsonobject = array.getJSONObject(i);
                    systemInfo.imcid[i] = jsonobject.getString("imcid");
                    systemInfo.sysName[i] = jsonobject.getString("name");
                    systemInfo.update_at[i] = jsonobject.getString("updated_at");
                    //showError.showErrorLogcat("MEU",systemInfo.update_at[i]);
                    systemInfo.created_at[i] = jsonobject.getString("created_at");
                    String[] separatedLocText = jsonobject.getString("coordinates").replace("[", "").replace("]", "")
                            .split(",");
                    //showError.showErrorLogcat("MEU", "LAT: "+separatedLocText[0]+ " - LON: "+separatedLocText[1]);
                    systemInfo.coordinates[i] = new Location("Ripples:"+systemInfo.sysName[i]);
                    systemInfo.coordinates[i].setLatitude(Double.parseDouble(separatedLocText[0]));
                    systemInfo.coordinates[i].setLongitude(Double.parseDouble(separatedLocText[1]));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                updateBuffer = true;
                return false;
            }
            updateBuffer = false;
            return true;
        }
        return false;
    }

    public int GetNumberSystemRipples(){
        return systemInfo.systemSize;
    }

    public SystemInfo GetSystemInfoRipples(){
        if(!updateBuffer){
            updateBuffer = true;
            return systemInfo;
        }
        return null;
    }

    public void ResetBuffer(){
        updateBuffer = true;
    }
}
