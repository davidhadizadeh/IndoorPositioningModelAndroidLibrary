package de.hadizadeh.positioning.roommodel.android.technologies;


import android.bluetooth.BluetoothAdapter;
import de.hadizadeh.positioning.controller.Technology;
import de.hadizadeh.positioning.roommodel.android.BalanceCachingManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class for defining the structure for different btle technologies
 */
public abstract class BluetoothLeTechnology extends Technology {
    protected BluetoothAdapter bluetoothAdapter;
    protected int cacheSize = 5;
    protected Map<String, BluetoothLeDevice> btLeDevices;
    protected long validityTime;
    protected List<String> allowedBtLeDevices;
    protected boolean scanning;

    protected BluetoothAdapter.LeScanCallback leScanCallback;

    /**
     * Creates the technology
     *
     * @param name               name of the technology
     * @param allowedBtLeDevices whitelist of allowed btle devices or null, if all devices are allowed
     * @param validityTime       delta time which describes for how long a received signal will be used
     */
    public BluetoothLeTechnology(String name, List<String> allowedBtLeDevices, long validityTime) {
        super(name, null);
        this.validityTime = validityTime;
        this.allowedBtLeDevices = allowedBtLeDevices;
        this.btLeDevices = new HashMap<String, BluetoothLeDevice>();
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        setCachingManager(new BalanceCachingManager(cacheSize));
    }

    /**
     * Starts the scan process
     */
    @Override
    public void startScanning() {
        super.startScanning();
        bluetoothAdapter.startLeScan(leScanCallback);
    }

    /**
     * Removes all collected btle data
     */
    public void resetBtData() {
        btLeDevices.clear();
    }

    /**
     * Stops the scan process
     */
    @Override
    public void stopScanning() {
        super.stopScanning();
        scanning = false;
        bluetoothAdapter.stopLeScan(leScanCallback);
    }

    /**
     * Calculates a beacon id and converts it to a better readable number
     *
     * @param id beacon id
     * @return number
     */
    public static int idToNumber(String id) {
        BluetoothLeDevice bluetoothLeDevice = new BluetoothLeDevice(id);
        return majorMinorToNumber(bluetoothLeDevice.getMajor(), bluetoothLeDevice.getMinor());
    }

    /**
     * Converts major and minor data to a single number
     *
     * @param major major value
     * @param minor minor value
     * @return representative number
     */
    public static int majorMinorToNumber(int major, int minor) {
        String majorBits = String.format("%3s", Integer.toBinaryString(major)).replace(' ', '0');
        String minorBits = String.format("%6s", Integer.toBinaryString(minor)).replace(' ', '0');
        return Integer.parseInt(majorBits + minorBits, 2);
    }

    /**
     * Enables the bluetooth hardware
     *
     * @return true it has been enabled, else false when it was already enabled
     */
    public boolean enableHardware() {
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            BluetoothAdapter.getDefaultAdapter().enable();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Disables the bluetooth hardware
     */
    public void disableHardware() {
        if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            BluetoothAdapter.getDefaultAdapter().disable();
        }
    }
}
