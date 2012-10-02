package org.cgeo.liveview;

import java.text.ParseException;

import android.util.Log;

import com.sonyericsson.extras.liveview.plugins.PluginConstants;

public class NMEASatParser {

	/** The number of satellites that are visible. */
	private int visibleSatCount;
	/** The number of satellites that have a fix. */
	private int fixedSats;

	public int getVisibleSatCount() {
		return visibleSatCount;
	}

	public int getFixedSats() {
		return fixedSats;
	}

	public class Sat {

		private int num;
		private int azimuth;
		private int signalstrength;
		private int elevation;

		public Sat(int num, int elevation, int azimuth, int signalstrength) {
			this.num = num;
			this.elevation = elevation;
			this.azimuth = azimuth;
			this.signalstrength = signalstrength;
		}

	}

	private void parseGSA(String nmea) throws ParseException {
		if (!nmea.startsWith("$GPGSA")) {
			throw new ParseException("Can't parse " + nmea, 0);
		}
		String[] msg = nmea.split(",");
		// A Automatic 2D,3D
		String mode = msg[1];
		// 1=no Fix, 2=2D Fix, 3=3D Fix
		fixedSats = 0;
		int fix = Integer.parseInt(msg[2]);
		for (int i = 3; i < msg.length - 3; i++) {
			String sat = msg[i];

			if (sat.trim().length() > 0) {
				fixedSats++;
			}
			System.out.println(sat);
		}
	}

	/**
	 * Parses the GPGSV according to
	 * http://www.hemispheregps.com/gpstechinfo/GPGSV.htm
	 * $GPGSV,4,4,15,27,00,028,,28,10,330,,32,13,208,*40
	 * 
	 * @param nmea
	 * @throws ParseException
	 */

	private void parseGSV(String nmea) throws ParseException {
		if (!nmea.startsWith("$GPGSV")) {
			throw new ParseException("Can't parse " + nmea, 0);
		}
		try {
			String[] msg = nmea.split(",");
			int msgCount = Integer.parseInt(msg[1]);
			int msgNumber = Integer.parseInt(msg[2]);
			visibleSatCount = Integer.parseInt(msg[3]);
			for (int i = 0; i < 4 && i * 4 < msg.length; i++) {
				int blockOffset = i * 4 + 4;
				if (blockOffset >= msg.length) {
					break;
				}
				int num = msg[blockOffset].indexOf("*") >= 0 || msg[blockOffset].length() <= 0 ? 0 : Integer.parseInt(msg[blockOffset]);
				if (++blockOffset >= msg.length) {
					break;
				}
				int elevation = msg[blockOffset].indexOf("*") >= 0 || msg[blockOffset].length() <= 0 ? 0 : Integer.parseInt(msg[blockOffset]);
				if (++blockOffset >= msg.length) {
					break;
				}
				int azimuth = msg[blockOffset].indexOf("*") >= 0 || msg[blockOffset].length() <= 0 ? 0 : Integer.parseInt(msg[blockOffset]);
				if (++blockOffset >= msg.length) {
					break;
				}
				int signalstrength = msg[blockOffset].indexOf("*") >= 0 || msg[blockOffset].length() <= 0 ? 0 : Integer.parseInt(msg[blockOffset]);
				new Sat(num, elevation, azimuth, signalstrength);
			}
		} catch (Exception e) {
			Log.e(PluginConstants.LOG_TAG, e.toString(), e);
			throw new ParseException("Can't parse " + nmea, 0);
		}
	}

	public void parse(long timestamp, String nmea) {
		try {
			if (nmea.startsWith("$GPGSV")) {
				parseGSV(nmea);
			} else if (nmea.startsWith("$GPGSA")) {
				parseGSA(nmea);
			}
		} catch (ParseException e) {
			Log.e(PluginConstants.LOG_TAG, e.getMessage(), e);
		}
	}
}
