package de.hadizadeh.positioning.roommodel.android.technologies;


import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import de.hadizadeh.positioning.controller.Technology;
import de.hadizadeh.positioning.model.PositionInformation;
import de.hadizadeh.positioning.model.SignalInformation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GPS technology for the fingerprinting, defines areas connected to latitude and longitude
 */
public class GpsTechnology extends Technology implements LocationListener {
    private static final String LAT_TEXT = "lat";
    private static final String LNG_TEXT = "lng";
    private static final int GPS_TIME_DELTA = 10000;
    private LocationManager locationManager;
    private Location location;
    private float maxDistance;

    /**
     * Creates a gps technology
     *
     * @param context           activity context
     * @param name              name of the technology
     * @param maxDistance       maximum distance where the area ends
     * @param minUpdateInterval minimum update interval of getting gps data
     * @param minDistanceChange minimum distance change when the gps data will update
     */
    public GpsTechnology(Context context, String name, float maxDistance, int minUpdateInterval, float minDistanceChange) {
        super(name);
        this.maxDistance = maxDistance;
        if(context != null) {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minUpdateInterval, minDistanceChange, this);
        }
    }

    /**
     * Returns the current longitude and latitude
     *
     * @return longitude and latitude
     */
    @Override
    public Map<String, SignalInformation> getSignalData() {
        Map<String, SignalInformation> signalData = new HashMap<String, SignalInformation>();
        if (location != null) {
            signalData.put(LAT_TEXT, new SignalInformation(location.getLatitude()));
            signalData.put(LNG_TEXT, new SignalInformation(location.getLongitude()));
        }
        return signalData;
    }

    /**
     * Updates the current location
     *
     * @param location current location
     */
    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
    }

    /**
     * Clears the location if the system changes
     *
     * @param provider provider
     * @param status   status
     * @param extras   extras
     */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        this.location = null;
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    /**
     * Sets the location for manipulation if there is no gps signal
     *
     * @param location location
     */
    public void setLocation(Location location) {
        this.location = location;
    }

    /**
     * Clears the current location if the provider is disabled
     *
     * @param provider provider
     */
    @Override
    public void onProviderDisabled(String provider) {
        this.location = null;
    }

    private double calculateDistance(Location location, double lat, double lng) {
        Location compareLocation = new Location("");
        compareLocation.setLatitude(lat);
        compareLocation.setLongitude(lng);
        Log.d("GPS-Distance", String.valueOf(location.distanceTo(compareLocation)));
        return location.distanceTo(compareLocation);
    }

    /**
     * Matches areas around the current longitude and latitude and returns the closest positions connected with the distance
     *
     * @param persistedPositions persisted fingerprints
     * @return closest positions
     */
    @Override
    public Map<PositionInformation, Double> match(List<PositionInformation> persistedPositions) {
        Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (lastLocation == null) {
            location = null;
        } else if (lastLocation.getTime() + GPS_TIME_DELTA < System.currentTimeMillis()) {
            location = null;
        }

        if (location != null) {
            Map<PositionInformation, Double> nearestPositions = new HashMap<PositionInformation, Double>();
            for (PositionInformation positionInformation : persistedPositions) {
                Map<String, SignalInformation> signalInformation = positionInformation.getSignalInformation();
                double distance = calculateDistance(location, signalInformation.get(LAT_TEXT).getStrength(), signalInformation.get(LNG_TEXT)
                        .getStrength());
                if (distance <= maxDistance) {
                    nearestPositions.put(positionInformation, distance);
                }
            }
            return nearestPositions;
        } else {
            return null;
        }
    }
}
