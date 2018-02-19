package pt.lsts.acm;

import android.os.AsyncTask;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Created by pedro on 2/19/18.
 * LSTS - FEUP
 */

public class RetrieveDataRipples extends AsyncTask<String, Void, String> {
    //ShowError showError = new ShowError();

    @Override
    protected String doInBackground(String... urls) {
        String response_str;
        try{
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(urls[0]);
            // Get the response
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            response_str = client.execute(request, responseHandler);
            //showError.showErrorLogcat("MEU", response_str);
        } catch (Exception e) {
            e.printStackTrace();
            return "none";
        }
        return response_str;
    }
}
