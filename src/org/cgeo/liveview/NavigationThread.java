package org.cgeo.liveview;

import android.util.Log;
import cgeo.geocaching.geopoint.Geopoint;

import com.sonyericsson.extras.liveview.plugins.LiveViewAdapter;
import com.sonyericsson.extras.liveview.plugins.PluginConstants;
import com.sonyericsson.extras.liveview.plugins.PluginUtils;

public class NavigationThread extends Thread {
    private static final long REFRESH_RATE = 2000;

    private final Geopoint destination;
    private volatile boolean displayRefreshEnabled = true;
    private volatile boolean shouldStop = false;
    private volatile long stopTime = 0;
    private final long timeout;
    private final boolean useMetricUnits;
    private final LiveViewAdapter liveView;
    private final int pluginId;
    
    private int counter = 0; // TODO

	private Geopoint currentLocation;

    public NavigationThread(final LiveViewAdapter liveView, final int pluginId, final Geopoint destination, final long timeout, final boolean useMetricUnits) {
        if (liveView == null) {
            throw new IllegalArgumentException("liveView must not be null");
        }
        if (destination == null) {
            throw new IllegalArgumentException("destination must not be null");
        }

        this.liveView = liveView;
        this.pluginId = pluginId;
        this.destination = destination;
        this.timeout = timeout;
        this.useMetricUnits = useMetricUnits;
    }

    @Override
    public void run() {

        resetTimer();

        while (!shouldStop && !timedOut()) {
            if (displayRefreshEnabled) {
                // TODO: Refresh display
				liveView.clearDisplay(pluginId);
				liveView.screenOn(pluginId);
				liveView.ledControl(pluginId, counter, 0, 100);
				liveView.vibrateControl(pluginId, 0, 100);
				liveView.vibrateControl(pluginId, 1000, 100);
				if (currentLocation != null) {
					PluginUtils.sendTextBitmap(liveView, pluginId, currentLocation.toString(), 128, 6);
				} else {
					PluginUtils.sendTextBitmap(liveView, pluginId, "No Location", 128, 10);
				}
            }
            
            counter++; // TODO

            try {
                sleep(REFRESH_RATE);
            } catch (InterruptedException e) {
                Log.d(PluginConstants.LOG_TAG, "NavigationThread: thread interrupted");
            }
        }

        // TODO: Stop GPS
    }

    /**
     * The thread runs longer than the given timeout. You can reset this timer with {@link #resetTimer()}.
     * 
     * @return If timout reached or not.
     */
    private boolean timedOut() {
        return System.currentTimeMillis() > stopTime;
    }

    /**
     * Reset the timer.
     */
    public void resetTimer() {
        stopTime = System.currentTimeMillis() + timeout;
    }

    /**
     * Stop the thread with the next cycle. There is no way to enable it again.
     */
    public void stopThread() {
        shouldStop = true;
    }
    
    /**
     * If the display should be refreshed or not each cycle.
     * 
     * @param enabled
     */
    public void setDisplayRefresh(final boolean enabled) {
        displayRefreshEnabled = enabled;
    }

	public void setCurrentLocation(Geopoint geopoint) {
		currentLocation = geopoint;
	}
}
