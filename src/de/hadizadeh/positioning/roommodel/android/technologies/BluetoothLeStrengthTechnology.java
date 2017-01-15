package de.hadizadeh.positioning.roommodel.android.technologies;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import de.hadizadeh.positioning.model.SignalInformation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Intuitive implementation of the fingerprinting algorithm. Using signal strength of all beacons
 */
public class BluetoothLeStrengthTechnology extends BluetoothLeTechnology {

    /**
     * Creates the technology
     *
     * @param name               name of the technology
     * @param allowedBtLeDevices whitelist of allowed btle devices or null, if all devices are allowed
     * @param validityTime       delta time which describes for how long a received signal will be used
     */
    public BluetoothLeStrengthTechnology(String name, long validityTime, List<String> allowedBtLeDevices) {
        super(name, allowedBtLeDevices, validityTime);

        leScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                BluetoothLeDevice btDevice = new BluetoothLeDevice(device, scanRecord, rssi);
                if ("4c00".equals(btDevice.getCompanyId()) && (BluetoothLeStrengthTechnology.this.allowedBtLeDevices == null ||
                        BluetoothLeStrengthTechnology.this.allowedBtLeDevices.contains(btDevice.getUuid()))) {
                    if (btDevice.getRssi() > -80) {
                        btLeDevices.put(btDevice.getIdentificator(), btDevice);
                    }
                }
            }
        };
    }

    /**
     * Collects all received btle data in the defined delta time
     *
     * @return received btle data
     */
    @Override
    public Map<String, SignalInformation> getSignalData() {
        Map<String, SignalInformation> signalData = new HashMap<String, SignalInformation>();
        long currentTime = System.currentTimeMillis();
        for (Map.Entry<String, BluetoothLeDevice> btLeDevice : btLeDevices.entrySet()) {
            if (btLeDevice.getValue().getTimeStamp() + validityTime >= currentTime) {
                signalData.put(btLeDevice.getValue().getIdentificator(),
                        new SignalInformation(btLeDevice.getValue().getRssi()));
            }
        }
        return signalData;
    }
}
