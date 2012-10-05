package org.cgeo.liveview;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;

import com.sonyericsson.extras.liveview.plugins.AbstractPluginService;
import com.sonyericsson.extras.liveview.plugins.PluginConstants;

public class LiveViewNavigatorService extends AbstractPluginService {
	private NavigationThread navigationThread = null;
	private Double latitude;
	private Double longitude;

	public NavigationThread getNavigationThread() {
		return navigationThread;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		mSharedPreferences.registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener() {

			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				latitude = Double.longBitsToDouble(mSharedPreferences.getLong(PluginConstants.PREFERENCES_LATITUDE, 0));
				longitude = Double.longBitsToDouble(mSharedPreferences.getLong(PluginConstants.PREFERENCES_LONGITUDE, 0));
				if (navigationThread != null) {
					Location destination = new Location("DEST");
					destination.setLatitude(latitude);
					destination.setLongitude(longitude);

					navigationThread.setDestination(destination);
				}
			}
		});

		// ...
		// Do plugin specifics.
		// ...
	}

	@Override
	public void onCreate() {
		super.onCreate();

		// ...
		// Do plugin specifics.
		// ...
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		stopWork();
	}

	/**
	 * Plugin is sandbox.<br />
	 * This means that we cannot send notifications, but have the full display
	 * size (128x128 px) for rendering bitmaps.
	 */
	protected boolean isSandboxPlugin() {
		return true;
	}

	/**
	 * Must be implemented. Starts plugin work, if any.
	 */
	protected void startWork() {
		Log.d(PluginConstants.LOG_TAG, "startWork");
		// Check if plugin is enabled.
		if (!mSharedPreferences.getBoolean(PluginConstants.PREFERENCES_PLUGIN_ENABLED, false)) {
			return;
		}
		latitude = Double.longBitsToDouble(mSharedPreferences.getLong(PluginConstants.PREFERENCES_LATITUDE, 0));
		longitude = Double.longBitsToDouble(mSharedPreferences.getLong(PluginConstants.PREFERENCES_LONGITUDE, 0));

		// be sure that there's no worker running
		stopWork();

		// clear the display
		try {
			mLiveViewAdapter.clearDisplay(mPluginId);
		} catch (Exception e) {
			Log.e(PluginConstants.LOG_TAG, "Failed to clear display.", e);
		}

		Bitmap arrow = BitmapFactory.decodeResource(getResources(), org.cgeo.liveview.R.drawable.arrow);
		long timeout = Long.parseLong(mSharedPreferences.getString(PluginConstants.PREFERENCES_GPS_TIMEOUT, "5")) * 60000;
		navigationThread = new NavigationThread(this, mPluginId, timeout, true, arrow);
		LocationManager geoManager = (LocationManager) getApplication().getSystemService(Context.LOCATION_SERVICE);
		navigationThread.setGeoManager(geoManager);
		navigationThread.start();
		if (latitude != null && longitude != null) {
			Location destination = new Location("DEST");
			destination.setLatitude(latitude);
			destination.setLongitude(longitude);

			navigationThread.setDestination(destination);
		}
	}

	/**
	 * Must be implemented. Stops plugin work, if any.
	 */
	protected void stopWork() {
		Log.d(PluginConstants.LOG_TAG, "stopWork");
		if (navigationThread != null) {
			navigationThread.stopThread();
			try {
				navigationThread.join();
			} catch (InterruptedException e) {
				Log.d(PluginConstants.LOG_TAG, "stopWork", e);
			}
			navigationThread = null;
		}
	}

	/**
	 * Must be implemented.
	 * 
	 * PluginService has done connection and registering to the LiveView
	 * Service.
	 * 
	 * If needed, do additional actions here, e.g. starting any worker that is
	 * needed.
	 */
	protected void onServiceConnectedExtended(ComponentName className, IBinder service) {

	}

	/**
	 * Must be implemented.
	 * 
	 * PluginService has done disconnection from LiveView and service has been
	 * stopped.
	 * 
	 * Do any additional actions here.
	 */
	protected void onServiceDisconnectedExtended(ComponentName className) {

	}

	/**
	 * Must be implemented.
	 * 
	 * PluginService has checked if plugin has been enabled/disabled.
	 * 
	 * The shared preferences has been changed. Take actions needed.
	 */
	protected void onSharedPreferenceChangedExtended(SharedPreferences prefs, String key) {
		Log.v(PluginConstants.LOG_TAG, "Preferences Changed : " + key);
	}

	/**
	 * This method is called by the LiveView application to start the plugin.
	 * For sandbox plugins, this means when the user has pressed the action
	 * button to start the plugin.
	 */
	protected void startPlugin() {
		Log.d(PluginConstants.LOG_TAG, "startPlugin");
		startWork();

	}

	/**
	 * This method is called by the LiveView application to stop the plugin. For
	 * sandbox plugins, this means when the user has long-pressed the action
	 * button to stop the plugin.
	 */
	protected void stopPlugin() {
		Log.d(PluginConstants.LOG_TAG, "stopPlugin");
		stopWork();
	}

	/**
	 * Sandbox mode only. When a user presses any buttons on the LiveView
	 * device, this method will be called.
	 */
	protected void button(String buttonType, boolean doublepress, boolean longpress) {
		Log.d(PluginConstants.LOG_TAG, "button - type " + buttonType + ", doublepress " + doublepress + ", longpress " + longpress);

		if (PluginConstants.BUTTON_SELECT.equalsIgnoreCase(buttonType)) {
			// Reset the timer. If the timer already ran out, restart
			// everything.
			if (navigationThread != null && navigationThread.isAlive()) {
				navigationThread.resetTimer();
			} else {
				startWork();
			}
		}
	}

	/**
	 * Called by the LiveView application to indicate the capabilites of the
	 * LiveView device.
	 */
	protected void displayCaps(int displayWidthPx, int displayHeigthPx) {
		Log.d(PluginConstants.LOG_TAG, "displayCaps - width " + displayWidthPx + ", height " + displayHeigthPx);
	}

	/**
	 * Called by the LiveView application when the plugin has been kicked out by
	 * the framework.
	 */
	protected void onUnregistered() {
		Log.d(PluginConstants.LOG_TAG, "onUnregistered");
		stopWork();
	}

	/**
	 * When a user presses the "open in phone" button on the LiveView device,
	 * this method is called. You could e.g. open a browser and go to a specific
	 * URL, or open the music player.
	 */
	protected void openInPhone(String openInPhoneAction) {
		Log.d(PluginConstants.LOG_TAG, "openInPhone: " + openInPhoneAction);
	}

	/**
	 * Sandbox mode only. Called by the LiveView application when the screen
	 * mode has changed. 0 = screen is off, 1 = screen is on
	 */
	protected void screenMode(int mode) {
		Log.d(PluginConstants.LOG_TAG, ",screenMode: screen is now " + ((mode == 0) ? "OFF" : "ON"));

		// Don't update screen while display is off (save battery), but keep the
		// GPS on so that it's available on resume.
		// This might be a use case for the timeout: Display was turned off
		// instead of closing the plugin.
		if (mode == PluginConstants.LIVE_SCREEN_MODE_ON) {
			if (navigationThread != null) {
				navigationThread.setDisplayRefresh(true);
			}
		} else {
			if (navigationThread != null) {
				navigationThread.setDisplayRefresh(false);
			}
		}
	}
}