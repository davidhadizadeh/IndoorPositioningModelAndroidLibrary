package test.de.hadizadeh.positioning.roommodel.android.technologies;

import de.hadizadeh.positioning.roommodel.android.technologies.BluetoothLeTechnology;
import junit.framework.TestCase;
import org.junit.Test;

import static org.junit.Assert.*;

public class BluetoothLeTechnologyTest extends TestCase {
    public void setUp() throws Exception {
        super.setUp();
    }

    public void testIdToNumber() throws Exception {
        assertEquals(74, BluetoothLeTechnology.idToNumber("1|10"));
    }

    public void testMajorMinorToNumber() throws Exception {
        assertEquals(74, BluetoothLeTechnology.majorMinorToNumber(1, 10));
    }
}