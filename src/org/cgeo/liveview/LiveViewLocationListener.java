package org.cgeo.liveview;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import cgeo.geocaching.geopoint.Geopoint;

public class LiveViewLocationListener implements LocationListener {

	NavigationThread navi = null;

	public LiveViewLocationListener(NavigationThread navi) {
		this.navi = navi;
	}

	@Override
	public void onLocationChanged(Location location) {
		if (navi != null) {
			navi.setCurrentLocation(new Geopoint(location.getLatitude(), location.getLongitude()));
		}
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}
}
