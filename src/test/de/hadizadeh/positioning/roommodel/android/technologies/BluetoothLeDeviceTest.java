package test.de.hadizadeh.positioning.roommodel.android.technologies;

import android.bluetooth.BluetoothDevice;
import de.hadizadeh.positioning.roommodel.android.technologies.BluetoothLeDevice;
import junit.framework.TestCase;

/**
 * Tests if a received btle advertisement packed can be parsed correctly
 */
public class BluetoothLeDeviceTest extends TestCase {

    private BluetoothLeDevice bluetoothLeDevice;

    public void setUp() throws Exception {
        super.setUp();

        byte[] scanRecord = new byte[] {2, 1, 6, 26, -1, 76, 0, 2, 21, 73, 110, 100, 46, 80, 111, 115, 105, 116, 105, 111, 110, 105, 110, 103, 32, 0, 1, 0, 12, -73, 11, 9, 112, 66, 101, 97, 99, 111, 110, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        bluetoothLeDevice = new BluetoothLeDevice(null, scanRecord, -50);
    }

    public void testGetCompanyId() throws Exception {
        assertEquals("4c00", bluetoothLeDevice.getCompanyId());
    }

    public void testGetRssi() throws Exception {
        assertEquals(-50, bluetoothLeDevice.getRssi());
    }

    public void testGetTimeStamp() throws Exception {
        assertTrue(bluetoothLeDevice.getTimeStamp() < System.currentTimeMillis() + 1000 && bluetoothLeDevice.getTimeStamp() > System.currentTimeMillis() - 1000);
    }

    public void testGetMajor() throws Exception {
        assertEquals(1, bluetoothLeDevice.getMajor());
    }

    public void testGetMinor() throws Exception {
        assertEquals(12, bluetoothLeDevice.getMinor());
    }

    public void testGetIdentificator() throws Exception {
        assertEquals("1|12", bluetoothLeDevice.getIdentificator());
    }

    public void testGetUuid() throws Exception {
        assertEquals("496e642e506f736974696f6e696e6720", bluetoothLeDevice.getUuid());
    }

    public void testGetTxPower() throws Exception {
        assertEquals(-73, bluetoothLeDevice.getTxPower());
    }

    public void testGetDistance() throws Exception {
        assertEquals(0.0707945784384138, bluetoothLeDevice.getDistance());
    }

    public void testGetDistanceCategory() throws Exception {
        assertEquals(BluetoothLeDevice.DistanceCategory.IMMEDIATE, bluetoothLeDevice.getDistanceCategory());
    }

    public void testGetUuidText() throws Exception {
        assertEquals("Ind.Positioning", bluetoothLeDevice.getUuidText());
    }
}