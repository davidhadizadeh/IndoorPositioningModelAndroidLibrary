package test.de.hadizadeh.positioning.roommodel.android;

import de.hadizadeh.positioning.model.SignalInformation;
import de.hadizadeh.positioning.roommodel.android.BalanceCachingManager;
import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

public class BalanceCachingManagerTest extends TestCase {

    private BalanceCachingManager balanceCachingManager;

    public void setUp() throws Exception {
        super.setUp();
        balanceCachingManager = new BalanceCachingManager(10);
    }

    public void testInterpolateData() throws Exception {
        for(int i = 0; i < 10; i ++) {
            Map<String, SignalInformation> data = new HashMap<String, SignalInformation>();
            data.put("a", new SignalInformation(i+1));
            data.put("b", new SignalInformation(10-i));
            data.put("c", new SignalInformation(i+11));
            data.put("d", new SignalInformation(20-i));
            balanceCachingManager.addData(data);
        }
        Map<String, SignalInformation> interpolatedData = balanceCachingManager.interpolateData();
        assertEquals(5.5, interpolatedData.get("a").getStrength());
        assertEquals(5.5, interpolatedData.get("b").getStrength());
        assertEquals(15.5, interpolatedData.get("c").getStrength());
        assertEquals(15.5, interpolatedData.get("d").getStrength());
    }
}