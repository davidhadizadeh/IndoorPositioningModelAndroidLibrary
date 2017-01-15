package de.hadizadeh.positioning.roommodel.android.technologies;


import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanRecord;

/**
 * Manages bluetooth low energy devices (beacons)
 */
public class BluetoothLeDevice implements Comparable<BluetoothLeDevice> {
    /**
     * distance categories
     */
    public enum DistanceCategory {
        UNKNOWN(0.0f),
        IMMEDIATE(1.0f), // <= 0,5 meters
        NEAR(2.0f), // <= 2 meters
        FAR(3.0f); // <= 30 meters
        private final float value;

        DistanceCategory(final float value) {
            this.value = value;
        }

        public float getValue() {
            return value;
        }
    }

    private String name;
    private String address;
    private String companyId;
    private String uuid;
    private int rssi;
    private int major;
    private int minor;
    private int txPower;
    private long timeStamp;
    private String uuidText;
    private double distance;
    private DistanceCategory distanceCategory;

    /**
     * Creates a btle device
     *
     * @param name      name
     * @param address   mac address
     * @param companyId company identifier
     * @param uuid      uuid
     * @param rssi      rssi signal strength
     * @param major     major value
     * @param minor     minor value
     * @param txPower   tx power
     * @param timeStamp timestamp of the time where the data have been received
     */
    public BluetoothLeDevice(String name, String address, String companyId, String uuid, int rssi, int major, int minor, int txPower, long
            timeStamp) {
        initialize(name, address, companyId, uuid, rssi, major, minor, txPower, timeStamp);
    }

    /**
     * Creates a btle device and converts the values to the correct format
     *
     * @param name      name
     * @param address   mac address
     * @param companyId company identifier
     * @param uuid      uuid
     * @param rssi      rssi signal strength
     * @param major     major value
     * @param minor     minor value
     * @param txPower   tx power
     * @param timeStamp timestamp of the time where the data have been received
     */
    public BluetoothLeDevice(String name, String address, String companyId, String uuid, int rssi, String major, String minor, String txPower, long
            timeStamp) {
        initialize(name, address, companyId, uuid, rssi, Integer.parseInt(major, 16), Integer.parseInt(minor, 16), Integer.valueOf(txPower, 16)
                .shortValue(), timeStamp);
    }

    /**
     * Creates a btle device
     *
     * @param device     device with included data
     * @param scanRecord advertisement message
     * @param rssi       rssi signal strength
     */
    public BluetoothLeDevice(BluetoothDevice device, byte[] scanRecord, int rssi) {
        extractBtData(device, scanRecord, rssi);
    }

    /**
     * Creates a btle device for displaying
     *
     * @param id identificator
     */
    public BluetoothLeDevice(String id) {
        String[] idParts = id.split("\\|");
        if (idParts.length == 2) {
            initialize(name, address, companyId, uuid, rssi, Integer.valueOf(idParts[0]), Integer.valueOf(idParts[1]), txPower, timeStamp);
        }
    }

    /**
     * Creates a btle device
     *
     * @param device     device with included data
     * @param scanRecord advertisement message
     * @param rssi       rssi signal strength
     */
    public BluetoothLeDevice(BluetoothDevice device, ScanRecord scanRecord, int rssi) {
        extractBtData(device, scanRecord.getBytes(), rssi);
    }

    private void initialize(String name, String address, String companyId, String uuid, int rssi, int major, int minor, int txPower, long timeStamp) {
        this.name = name;
        this.address = address;
        this.companyId = companyId;
        this.uuid = uuid;
        this.rssi = rssi;
        this.major = major;
        this.minor = minor;
        this.txPower = txPower;
        this.timeStamp = timeStamp;
        this.uuidText = hexToAscii(uuid);
        this.distance = calculateDistance(rssi, txPower);
        distanceCategory = determineDistanceCategory(this.distance);
    }

    private void extractBtData(BluetoothDevice device, byte[] scanRecord, int rssi) {
        String uuid = "";
        String companyId = String.format("%02x", scanRecord[5]) + String.format("%02x", scanRecord[6]);
        String major = String.format("%02x", scanRecord[25]) + String.format("%02x", scanRecord[26]);
        String minor = String.format("%02x", scanRecord[27]) + String.format("%02x", scanRecord[28]);
        String txPower = String.format("%02x", scanRecord[29]);
        for (int i = 0; i < scanRecord.length; i++) {
            if (i >= 9 && i <= 24) {
                uuid += String.format("%02x", scanRecord[i]);
            }
        }

        String name = "";
        String address = "";
        if (device != null) {
            name = device.getName();
            address = device.getAddress();
        }
        initialize(name, address, companyId, uuid, rssi, Integer.parseInt(major, 16), Integer.parseInt(minor, 16), Short
                .valueOf(txPower, 16).byteValue(), System.currentTimeMillis());
    }

    private double calculateDistance(int rssi, int txPower) {
        return Math.pow(10d, ((double) txPower - rssi) / (10 * 2));
    }

    private DistanceCategory determineDistanceCategory(double distance) {
        if (distance <= 0.5) {
            return DistanceCategory.IMMEDIATE;
        } else if (distance <= 2.0) {
            return DistanceCategory.NEAR;
        } else if (distance <= 30.0) {
            return DistanceCategory.FAR;
        } else {
            return DistanceCategory.UNKNOWN;
        }
    }

    /**
     * Returns the name
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name
     *
     * @param name name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the mac address
     *
     * @return mac address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the mac address
     *
     * @param address mac address
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Returns the company identifier
     *
     * @return company identifier
     */
    public String getCompanyId() {
        return companyId;
    }

    /**
     * Sets the company identifier
     *
     * @param companyId compani identifier
     */
    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    /**
     * Returns the rssi signal strength value
     *
     * @return rssi signal strength value
     */
    public int getRssi() {
        return rssi;
    }

    /**
     * Sets the rssi signal strength value
     *
     * @param rssi rssi signal strength value
     */
    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    /**
     * Returns the timestamp of the time where the data have been received
     *
     * @return timestamp of the time where the data have been received
     */
    public long getTimeStamp() {
        return timeStamp;
    }

    /**
     * Sets the timestamp of the time where the data have been received
     *
     * @param timeStamp timestamp of the time where the data have been received
     */
    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    /**
     * Returns the major value
     *
     * @return major value
     */
    public int getMajor() {
        return major;
    }

    /**
     * Sets the major value
     *
     * @param major major value
     */
    public void setMajor(int major) {
        this.major = major;
    }

    /**
     * Returns the minor value
     *
     * @return minor value
     */
    public int getMinor() {
        return minor;
    }

    /**
     * Sets the minor value
     *
     * @param minor minor value
     */
    public void setMinor(int minor) {
        this.minor = minor;
    }

    /**
     * Returns an identification string for the beacon
     *
     * @return identification string for the beacon
     */
    public String getIdentificator() {
        return major + "|" + minor;
    }

    /**
     * Returns the uuid
     *
     * @return uuid
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Sets the uuid
     *
     * @param uuid uuid
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * Returns the tx power
     *
     * @return tx power
     */
    public int getTxPower() {
        return txPower;
    }

    /**
     * Sets the tx power
     *
     * @param txPower tx power
     */
    public void setTxPower(int txPower) {
        this.txPower = txPower;
    }

    /**
     * Returns the estimated distance to the beacon
     *
     * @return estimated distance to the beacon
     */
    public double getDistance() {
        return distance;
    }

    /**
     * Returns the estimated distance category
     *
     * @return estimated distance category
     */
    public DistanceCategory getDistanceCategory() {
        return distanceCategory;
    }

    /**
     * Returns the uuid as a text string
     *
     * @return uuid
     */
    public String getUuidText() {
        return uuidText;
    }

    private static String hexToAscii(String hexValue) {
        if (hexValue != null && hexValue.length() > 0) {
            StringBuilder output = new StringBuilder("");
            for (int i = 0; i < hexValue.length(); i += 2) {
                String str = hexValue.substring(i, i + 2);
                output.append((char) Integer.parseInt(str, 16));
            }
            return output.toString().trim();
        } else {
            return null;
        }
    }

    @Override
    public int compareTo(BluetoothLeDevice another) {
        if (rssi > another.rssi) {
            return 1;
        } else if (rssi < another.rssi) {
            return -1;
        }
        return 0;
    }

    @Override
    public String toString() {
        return "BluetoothLeDevice{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", companyId='" + companyId + '\'' +
                ", uuid='" + uuid + '\'' +
                ", rssi=" + rssi +
                ", major=" + major +
                ", minor=" + minor +
                ", txPower=" + txPower +
                ", timeStamp=" + timeStamp +
                ", uuidText='" + uuidText + '\'' +
                ", distance=" + distance +
                ", distanceCategory=" + distanceCategory +
                '}';
    }
}
