package de.hadizadeh.positioning.roommodel.android.technologies;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import de.hadizadeh.positioning.controller.ExclusionTechnology;
import de.hadizadeh.positioning.model.SignalInformation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Exclusion Compass technology
 */
public class CompassTechnology extends ExclusionTechnology {

    private float bearing;
    private Context context;
    //private TextView compassTv;

    /**
     * Creates the compass technology
     *
     * @param context      avtivity context
     * @param name         name of the technology
     * @param allowedDelta allowed angle (positions which are not in this angle will be excluded)
     */
    public CompassTechnology(Context context, String name, double allowedDelta) {
        super(name, allowedDelta / 2);
        this.context = context;
        //compassTv = (TextView) ((Activity) context).findViewById(R.id.compassTv);
        if (context != null) {
            SensorManager mySensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            List<Sensor> mySensors = mySensorManager.getSensorList(Sensor.TYPE_ORIENTATION);
            if (mySensors.size() > 0) {
                mySensorManager.registerListener(mySensorEventListener, mySensors.get(0), SensorManager.SENSOR_DELAY_UI);
            }
        }
    }

    /**
     * Returns the current angle
     *
     * @return current angle
     */
    @Override
    public Map<String, SignalInformation> getSignalData() {
        Map<String, SignalInformation> signalData = new HashMap<String, SignalInformation>();
        signalData.put("compassSignal", new SignalInformation(bearing));
        return signalData;
    }

    private SensorEventListener mySensorEventListener = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            float compassBearing = (float) event.values[0];
            if (Math.abs(bearing - compassBearing) > 2) {
                //compassTv.setText(String.valueOf(compassBearing));
            }
            bearing = compassBearing;

        }
    };

    /**
     * Calculates if signal data are in the angle of the current viewing point
     *
     * @param signalData     signal data
     * @param persistedValue persisted fingerprints
     * @return true, if the value is not in the range, else false
     */
    @Override
    protected boolean isValueOutOfExclusionRange(Map<String, SignalInformation> signalData, double persistedValue) {
        boolean inRange = true;
        for (Map.Entry<String, SignalInformation> data : signalData.entrySet()) {
            double currentValue = data.getValue().getStrength();
            double min = persistedValue - allowedDelta;
            double max = persistedValue + allowedDelta;
            if (max >= 360) {
                max -= 360;
            }
            if (min <= 0) {
                min = 360 - min;
            }

            if (min > max) {
                if (!(currentValue >= min || currentValue <= max)) {
                    inRange = false;
                }
            } else {
                if (!(currentValue >= min && currentValue <= max)) {
                    inRange = false;
                }
            }
        }
        return inRange;
    }
}
