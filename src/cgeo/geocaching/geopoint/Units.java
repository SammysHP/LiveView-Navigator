package cgeo.geocaching.geopoint;

import org.cgeo.utils.ImmutablePair;

public class Units {

    public static ImmutablePair<Double, String> scaleDistance(final double distanceKilometers, final boolean useMetricUnits) {
        double distance;
        String units;
        if (useMetricUnits) {
            if (distanceKilometers >= 1) {
                distance = distanceKilometers;
                units = "km";
            } else {
                distance = distanceKilometers * 1000;
                units = "m";
            }
        } else {
            distance = distanceKilometers / IConversion.MILES_TO_KILOMETER;
            if (distance >= 0.1) {
                units = "mi";
            } else {
                distance *= 5280;
                units = "ft";
            }
        }
        return new ImmutablePair<Double, String>(distance, units);
    }

    public static String getDistanceFromKilometers(final Float distanceKilometers, final boolean useMetricUnits) {
        if (distanceKilometers == null) {
            return "?";
        }

        final ImmutablePair<Double, String> scaled = scaleDistance(distanceKilometers, useMetricUnits);
        String formatString;
        if (scaled.left >= 100) {
            formatString = "%.0f";
        } else if (scaled.left >= 10) {
            formatString = "%.1f";
        } else {
            formatString = "%.2f";
        }

        return String.format(formatString + " %s", scaled.left, scaled.right);
    }

    /**
     * Get human readable elevation, depending on settings for metric units.
     * Result is rounded to full meters/feet, as the sensors don't have that precision anyway.
     *
     * @param meters
     * @return
     */
    public static String getElevation(float meters, final boolean useMetricUnits) {
        final ImmutablePair<Double, String> scaled = scaleDistance(meters / 1000f, useMetricUnits);
        return (meters >= 0 ? "↥ " : "↧ ") + String.format("%d %s", Math.abs(Math.round(scaled.left)), scaled.right);
    }

    public static String getDistanceFromMeters(float meters, final boolean useMetricUnits) {
        return getDistanceFromKilometers(meters / 1000f, useMetricUnits);
    }

    public static String getSpeed(float kilometersPerHour, final boolean useMetricUnits) {
        final String speed = getDistanceFromKilometers(kilometersPerHour, useMetricUnits);
        if (speed.endsWith("mi")) {
            return speed.substring(0, speed.length() - 2) + "mph";
        }
        return speed + (useMetricUnits ? "/h" : "ph");
    }
}
