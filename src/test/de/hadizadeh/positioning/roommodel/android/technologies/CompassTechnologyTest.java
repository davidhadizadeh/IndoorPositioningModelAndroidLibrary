package test.de.hadizadeh.positioning.roommodel.android.technologies;

import de.hadizadeh.positioning.model.SignalInformation;
import de.hadizadeh.positioning.roommodel.android.technologies.CompassTechnology;
import junit.framework.TestCase;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

public class CompassTechnologyTest extends TestCase {

    private CompassTechnology compassTechnology;

    public void setUp() throws Exception {
        super.setUp();
        compassTechnology = new CompassTechnology(null, "COMPASS", 60.0);
    }

    public void testIsValueOutOfExclusionRange() throws Exception {
        Class[] params = new Class[]{Map.class, Double.TYPE};
        Method method = CompassTechnology.class.getDeclaredMethod("isValueOutOfExclusionRange", params);
        method.setAccessible(true);
        Map<String, SignalInformation> signalData = new HashMap<String, SignalInformation>();
        signalData.put("currentAngle", new SignalInformation(50.0));

        assertTrue((Boolean) method.invoke(compassTechnology, signalData, 80.0));
        assertFalse((Boolean)method.invoke(compassTechnology, signalData, 81.0));
        assertTrue((Boolean)method.invoke(compassTechnology, signalData, 20.0));
        assertFalse((Boolean) method.invoke(compassTechnology, signalData, 19.0));
    }
}