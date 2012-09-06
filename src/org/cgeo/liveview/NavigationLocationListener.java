package org.cgeo.liveview;

import cgeo.geocaching.geopoint.Geopoint;
import cgeo.geocaching.geopoint.Units;

import com.sonyericsson.extras.liveview.plugins.LiveViewAdapter;
import com.sonyericsson.extras.liveview.plugins.PluginConstants;
import com.sonyericsson.extras.liveview.plugins.PluginUtils;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;

public class NavigationLocationListener implements LocationListener {
    private final Geopoint destination;
    private final long timeout;
    private final boolean useMetricUnits;
    private final LiveViewAdapter liveViewAdapter;
    private final int pluginId;
    private boolean displayRefreshEnabled = true;
    
    public NavigationLocationListener(
            final LiveViewAdapter liveView,
            final int pluginId,
            final Geopoint destination,
            final long timeout,
            final boolean useMetricUnits) {
        this.liveViewAdapter = liveView;
        this.pluginId = pluginId;
        this.destination = destination;
        this.timeout = timeout;
        this.useMetricUnits = useMetricUnits;
    }
    
    @Override
    public void onLocationChanged(Location location) {
        if (displayRefreshEnabled) {
            final Geopoint currentLocation = new Geopoint(location.getLatitude(), location.getLongitude());
            final String text = currentLocation.bearingTo(destination)
                    + "\n"
                    + Units.getDistanceFromKilometers(currentLocation.distanceTo(destination), useMetricUnits)
                    + "\n±"
                    + Units.getDistanceFromMeters(Math.round(location.getAccuracy()), useMetricUnits);
            
            PluginUtils.sendTextBitmap(liveViewAdapter, pluginId, text, 128, 10);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        if (LocationManager.GPS_PROVIDER.equals(provider)) {
            // clear the display
            try {
                liveViewAdapter.clearDisplay(pluginId);
            } catch (Exception e) {
                Log.e(PluginConstants.LOG_TAG, "Failed to clear display.");
            }
            
            if (status != LocationProvider.AVAILABLE) {
                PluginUtils.sendTextBitmap(liveViewAdapter, pluginId, "No Fix", 128, 20);
            }
        }
    }
    
    /**
     * If the display should be refreshed or not each cycle.
     * 
     * @param enabled
     */
    public void setDisplayRefresh(final boolean enabled) {
        displayRefreshEnabled = enabled;
    }
}
