package pt.lsts.acm;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by pedro on 2/20/18.
 * LSTS - FEUP
 */

class SOIListAdapter extends ArrayAdapter<SystemDetail> {

    public SOIListAdapter(Context context, ArrayList<SystemDetail> systemDetail) {
        super(context, 0, systemDetail);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        SystemDetail user = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_view_soi, parent, false);
        }
        // Lookup view for data population
        //ImageView image = convertView.findViewById((R.id.icon));
        TextView nameSys = (TextView) convertView.findViewById(R.id.firstLine);
        TextView soiInfo = (TextView) convertView.findViewById(R.id.secondLine);
        // Populate the data into the template view using the data object
        //image.setImageDrawable(R.drawable.ico_auv);
        nameSys.setText(user.sysName);
        soiInfo.setText(user.soiInfo);
        // Return the completed view to render on screen
        return convertView;
    }
}
