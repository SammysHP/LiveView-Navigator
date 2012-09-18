package org.cgeo.liveview;

import java.text.NumberFormat;

import android.graphics.Bitmap;
import android.location.Location;
import android.util.Log;

import com.sonyericsson.extras.liveview.plugins.LiveViewAdapter;
import com.sonyericsson.extras.liveview.plugins.PluginConstants;
import com.sonyericsson.extras.liveview.plugins.PluginUtils;

public class NavigationThread extends Thread {
	NumberFormat nf = NumberFormat.getNumberInstance();
    private static final long REFRESH_RATE = 2000;

	private Location lastPosition;
	private Location destination;
    private volatile boolean displayRefreshEnabled = true;
    private volatile boolean shouldStop = false;
    private volatile long stopTime = 0;
    private final long timeout;
    private final boolean useMetricUnits;
    private final LiveViewAdapter liveView;
    private final int pluginId;
	private int direction;
    
	private Location currentLocation;

	private Bitmap arrow;

	public NavigationThread(final LiveViewAdapter liveView, final int pluginId, final long timeout, final boolean useMetricUnits,
			final Bitmap arrow) {
        if (liveView == null) {
            throw new IllegalArgumentException("liveView must not be null");
        }

        this.liveView = liveView;
        this.pluginId = pluginId;
        this.timeout = timeout;
        this.useMetricUnits = useMetricUnits;
		// this.arrow = PluginUtils.convertToRGB565(arrow);
		this.arrow = arrow;
		nf.setMaximumFractionDigits(2);
    }

    @Override
    public void run() {

        try {
			resetTimer();

			while (!shouldStop && !timedOut()) {
			    if (displayRefreshEnabled) {
			        // TODO: Refresh display
					// liveView.clearDisplay(pluginId);
					if (currentLocation != null && destination != null) {
						if (!currentLocation.equals(lastPosition)) {
							float direction = currentLocation.bearingTo(destination) - currentLocation.bearingTo(lastPosition);
							float distance = currentLocation.distanceTo(destination);
							liveView.vibrateControl(pluginId, 0, 100);
							PluginUtils.drawAndSendScreen(liveView, pluginId, arrow, formatDistance(distance), (int) direction, currentLocation.getProvider());
							lastPosition = currentLocation;
						}
					} else {
						PluginUtils.sendTextBitmap(liveView, pluginId, "No Location", PluginConstants.LIVEVIEW_SCREEN_X, 12);
						lastPosition = null;
					}
			    }
			    
			    try {
			        sleep(REFRESH_RATE);
			    } catch (InterruptedException e) {
			        Log.d(PluginConstants.LOG_TAG, "NavigationThread: thread interrupted");
			    }
			}
		} catch (Exception e) {
			Log.d(PluginConstants.LOG_TAG, "NavigationThread Crashed ", e);
		}
    }

	/**
	 * Formats the given distance
	 * 
	 * @param d
	 *            distance in kilometers
	 * @return
	 */

	private String formatDistance(float d) {
		if (d < 1000) {
			return nf.format(d) + " m";
		} else {
			return nf.format(d / 1000) + " km";
		}
	}

	/**
	 * The thread runs longer than the given timeout. You can reset this timer
	 * with {@link #resetTimer()}.
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
		if (liveView != null) {
			liveView.vibrateControl(pluginId, 0, 100);
			liveView.vibrateControl(pluginId, 0, 100);
		}
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

	public void setCurrentLocation(Location location) {
		Log.d(PluginConstants.LOG_TAG, "Received new Location " + location.toString());
		currentLocation = location;
	}

	public void setDestination(Location l) {
		destination = l;
		// Force Update
		lastPosition = null;
	}

}
