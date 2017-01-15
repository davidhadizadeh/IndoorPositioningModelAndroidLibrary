package test.de.hadizadeh.positioning.roommodel.android.technologies;

import android.location.Location;
import android.location.LocationProvider;
import de.hadizadeh.positioning.model.PositionInformation;
import de.hadizadeh.positioning.model.SignalInformation;
import de.hadizadeh.positioning.roommodel.android.technologies.GpsTechnology;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GpsTechnologyTest extends TestCase {

    private GpsTechnology gpsTechnology;

    public void setUp() throws Exception {
        super.setUp();
        gpsTechnology = new GpsTechnology(null, "GPS", 10, 0, 0);
    }

    public void testMatch() throws Exception {
        /*Location location = new Location("provider");
        location.setLatitude(52.45831);
        location.setLongitude(13.52603);
        gpsTechnology.setLocation(location);

        List<PositionInformation> persistedPositions = new ArrayList<PositionInformation>();
        HashMap<String, SignalInformation> positionValue = new HashMap<String, SignalInformation >();
        positionValue.put("lat", new SignalInformation(52.45817));
        positionValue.put("lng", new SignalInformation(13.52709));
        persistedPositions.add(new PositionInformation("position", positionValue));

        assertTrue(gpsTechnology.match(persistedPositions).size() == 1);*/
    }
}