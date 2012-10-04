package org.cgeo.liveview;

import java.text.NumberFormat;

import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
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
	/** The number of satellites that are visible. */
	private int visibleSatCount = -1;
	/** The number of satellites that have a fix. */
	private int fixedSats = -1;

	private LocationManager geoManager;
	private LiveViewLocationListener listener;

	private Location currentLocation;

	private Bitmap arrow;
	private boolean satsDirty = true;
	private LiveViewNavigatorService liveViewNavigatorService;

	Handler registerListener = new Handler(new Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			geoManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener);
			geoManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
			geoManager.addNmeaListener(listener);
			return true;
		}
	});

	/**
	 * CT
	 * 
	 * @param liveViewNavigatorService
	 * @param pluginId
	 * @param timeout
	 * @param useMetricUnits
	 * @param arrow
	 */

	public NavigationThread(final LiveViewNavigatorService liveViewNavigatorService, final int pluginId, final long timeout, final boolean useMetricUnits,
			final Bitmap arrow) {
		if (liveViewNavigatorService == null) {
			throw new IllegalArgumentException("liveView must not be null");
		}

		this.liveView = liveViewNavigatorService.getmLiveViewAdapter();
		this.liveViewNavigatorService = liveViewNavigatorService;
		this.pluginId = pluginId;
		this.timeout = timeout;
		this.useMetricUnits = useMetricUnits;
		// this.arrow = PluginUtils.convertToRGB565(arrow);
		this.arrow = arrow;
		nf.setMaximumFractionDigits(2);
		listener = new LiveViewLocationListener(this);
	}

	public void setGeoManager(LocationManager geoManager) {
		this.geoManager = geoManager;
		Location lastKnownLocation = geoManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		if (lastKnownLocation != null) {
			setCurrentLocation(lastKnownLocation);
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

	public int getFixedSats() {
		return fixedSats;
	}

	public int getVisibleSatCount() {
		return visibleSatCount;
	}

	/**
	 * Reset the timer.
	 */
	public void resetTimer() {
		stopTime = System.currentTimeMillis() + timeout;
	}

	@Override
	public void run() {

		try {
			resetTimer();
			registerListener.sendEmptyMessage(0);

			while (!shouldStop && !timedOut()) {
				if (displayRefreshEnabled) {
					// TODO: Refresh display
					// liveView.clearDisplay(pluginId);
					if (currentLocation != null && destination != null) {
						if (!currentLocation.equals(lastPosition)) {
							// float direction =
							// currentLocation.bearingTo(destination) -
							// (lastPosition != null ?
							// currentLocation.bearingTo(lastPosition) : 0);
							float direction = currentLocation.bearingTo(destination) - currentLocation.getBearing();
							float distance = currentLocation.distanceTo(destination);
							liveView.vibrateControl(pluginId, 0, 100);
							PluginUtils.drawAndSendScreen(liveView, pluginId, arrow, formatDistance(distance), (int) direction, currentLocation.getProvider());

							lastPosition = currentLocation;
						}
					} else {
						PluginUtils.sendTextBitmap(liveView, pluginId, "No Location", PluginConstants.LIVEVIEW_SCREEN_X, 12);
						lastPosition = null;
					}
					if (satsDirty) {
						PluginUtils.sendTextBitmap(liveView, pluginId, "" + fixedSats + "/" + visibleSatCount, 30, 12, 4, 110);
						satsDirty = false;
					}
				}

				try {
					sleep(REFRESH_RATE);
				} catch (InterruptedException e) {
					Log.d(PluginConstants.LOG_TAG, "NavigationThread: thread interrupted");
				}
			}
		} catch (Exception e) {
			Log.e(PluginConstants.LOG_TAG, "NavigationThread Crashed ", e);
		} finally {
			if (geoManager != null && listener != null) {
				geoManager.removeUpdates(listener);
			}

		}
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

	/**
	 * If the display should be refreshed or not each cycle.
	 * 
	 * @param enabled
	 */
	public void setDisplayRefresh(final boolean enabled) {
		displayRefreshEnabled = enabled;
	}

	public void setFixedSats(int fixedSats) {
		satsDirty |= (fixedSats != this.fixedSats);
		this.fixedSats = fixedSats;
	}

	public void setVisibleSatCount(int visibleSatCount) {
		satsDirty |= this.visibleSatCount != visibleSatCount;
		this.visibleSatCount = visibleSatCount;
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
	 * The thread runs longer than the given timeout. You can reset this timer
	 * with {@link #resetTimer()}.
	 * 
	 * @return If timout reached or not.
	 */
	private boolean timedOut() {
		return System.currentTimeMillis() > stopTime;
	}

}
