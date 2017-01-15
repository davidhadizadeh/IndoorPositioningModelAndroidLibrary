package de.hadizadeh.positioning.roommodel.android.technologies;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import de.hadizadeh.positioning.model.SignalInformation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Technology for handling btle beacons analog to the iphone system. Different categories for distances
 */
public class BluetoothLeProximityCategoryTechnology extends BluetoothLeTechnology {
    /**
     * Creates the technology
     *
     * @param name               name of the technology
     * @param allowedBtLeDevices whitelist of allowed btle devices or null, if all devices are allowed
     * @param validityTime       delta time which describes for how long a received signal will be used
     */
    public BluetoothLeProximityCategoryTechnology(String name, List<String> allowedBtLeDevices, long validityTime) {
        super(name, allowedBtLeDevices, validityTime);

        leScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                BluetoothLeDevice btLeDevice = new BluetoothLeDevice(device, scanRecord, rssi);
                //System.out.println("DEVICE: " + btDevice.toString());
                if ("Ind.Positioning".equals(btLeDevice.getUuidText())) {
                    btLeDevices.put(btLeDevice.getIdentificator(), btLeDevice);
                }
            }
        };
    }

    /**
     * Collects the received beacon data and organizes them into distance groups
     *
     * @return organized signal data
     */
    @Override
    public Map<String, SignalInformation> getSignalData() {
        Map<String, SignalInformation> signalData = new HashMap<String, SignalInformation>();
        long currentTime = System.currentTimeMillis();
        for (Map.Entry<String, BluetoothLeDevice> btLeDevice : btLeDevices.entrySet()) {
            if (btLeDevice.getValue().getTimeStamp() + validityTime >= currentTime) {
                if (btLeDevice.getValue().getDistanceCategory() == BluetoothLeDevice.DistanceCategory.IMMEDIATE) {
                    signalData.put(btLeDevice.getValue().getIdentificator(), new SignalInformation(btLeDevice.getValue().getDistanceCategory().getValue()));
                }
            }
        }
        return signalData;
    }
}