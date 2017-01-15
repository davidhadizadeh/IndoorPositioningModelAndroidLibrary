package de.hadizadeh.positioning.roommodel.android.technologies;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import de.hadizadeh.positioning.model.SignalInformation;

import java.util.*;

/**
 * Technology for handling btle beacons and just using the closest beacon
 */
public class BluetoothLeProximityTechnology extends BluetoothLeTechnology {

    /**
     * Creates the technology
     *
     * @param name               name of the technology
     * @param allowedBtLeDevices whitelist of allowed btle devices or null, if all devices are allowed
     * @param validityTime       delta time which describes for how long a received signal will be used
     */
    public BluetoothLeProximityTechnology(String name, List<String> allowedBtLeDevices, long validityTime) {
        super(name, allowedBtLeDevices, validityTime);

        leScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                BluetoothLeDevice btDevice = new BluetoothLeDevice(device, scanRecord, rssi);
                if ("Ind.Positioning".equals(btDevice.getUuidText())) {
                    //if (rssi > -75 && "4c00".equals(btDevice.getCompanyId()) && allowedBtLeDevices.contains(btDevice.getUuid())) {
                    //if (btDevice.getRssi() > -70) {
                    btLeDevices.put(btDevice.getIdentificator(), btDevice);
                    //}
                    //}
                }
            }
        };
    }

    /**
     * Removes all beacons of higher range and uses just the closest beacon
     *
     * @return closest beacon
     */
    @Override
    public Map<String, SignalInformation> getSignalData() {
        Map<String, SignalInformation> signalData = new HashMap<String, SignalInformation>();
        LinkedList<BluetoothLeDevice> availableDevices = new LinkedList<BluetoothLeDevice>();
        long currentTime = System.currentTimeMillis();
        for (Map.Entry<String, BluetoothLeDevice> btLeDevice : btLeDevices.entrySet()) {
            if (btLeDevice.getValue().getTimeStamp() + validityTime >= currentTime) {
                availableDevices.add(btLeDevice.getValue());
                //signalData.put(btLeDevice.getValue().getAddress(), new SignalInformation(btLeDevice.getValue().getRssi()));
            }
        }
        if (availableDevices.size() > 0) {
            BluetoothLeDevice bestDevice = Collections.max(availableDevices);
            signalData.put(bestDevice.getIdentificator(), new SignalInformation(1.0));
            return signalData;
        }
        return null;
    }
}