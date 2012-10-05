package org.cgeo.liveview;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import cgeo.geocaching.geopoint.Geopoint;
import cgeo.geocaching.geopoint.GeopointFormatter;
import cgeo.geocaching.geopoint.GeopointFormatter.Format;

import com.sonyericsson.extras.liveview.plugins.PluginConstants;

/**
 * This receiver updates the cached destination.<br />
 * When it receives a suitable intent it updates the destination (coordinates, description, ...). There are two
 * possibilities to proceed:
 * <ul>
 * <li>The plugin is in use at the LiveView. Then the new destination is used for the next update cycle.</li>
 * <li>The plugin is not in use. Then the new destination will be used for the next update cycle, but the plugin will
 * not be opened at the LiveView.</li>
 * </ul>
 */
public class UpdateReceiver extends BroadcastReceiver {

	@Override
    public void onReceive(Context context, Intent intent) {

		try {
			Double latitude = (Double) intent.getExtras().get("latitude");
			Double longitude = (Double) intent.getExtras().get("longitude");
			setDestination(context, latitude, longitude);
			Log.d(PluginConstants.LOG_TAG,
					"LiveView received new Coords " + GeopointFormatter.format(Format.LAT_LON_DECMINUTE_RAW, new Geopoint(latitude, longitude)));
			// context.getSharedPreferences(name, mode);
		} catch (Exception e) {
			Log.e(PluginConstants.LOG_TAG, "Error in UpdateReceiver", e);
		}
    }

	private void setDestination(Context context, Double latitude, Double longitude) {
		SharedPreferences prefs = context.getSharedPreferences("org.cgeo.liveview_preferences", Context.MODE_WORLD_WRITEABLE);
		Editor e = prefs.edit();
		e.putLong("latitude", Double.doubleToLongBits(latitude));
		e.putLong("longitude", Double.doubleToLongBits(longitude));
		e.commit();
	}


}
