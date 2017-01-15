package test.de.hadizadeh.positioning.roommodel.android;

import de.hadizadeh.positioning.controller.CachingManager;
import de.hadizadeh.positioning.model.PositionInformation;
import de.hadizadeh.positioning.model.SignalInformation;
import de.hadizadeh.positioning.roommodel.android.OrderMatcher;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class OrderMatcherTest extends TestCase {

    private OrderMatcher orderMatcher;

    public void setUp() throws Exception {
        super.setUp();
        orderMatcher = new OrderMatcher();
    }

    public void testNearestNeighbour() throws Exception {
        CachingManager cachingManager = new CachingManager();

        Map<String, SignalInformation> fingerPrint;
        Map<PositionInformation, Double> orderedFingerprints;
        List<PositionInformation> persistedPositions = new ArrayList<PositionInformation>();

        Map<String, SignalInformation> signalInformation;

        signalInformation = new HashMap<String, SignalInformation>();
        signalInformation.put("A", new SignalInformation(-60));
        signalInformation.put("B", new SignalInformation(-50));
        signalInformation.put("C", new SignalInformation(-70));
        persistedPositions.add(new PositionInformation("1.", signalInformation));

        signalInformation = new HashMap<String, SignalInformation>();
        signalInformation.put("A", new SignalInformation(-50));
        signalInformation.put("B", new SignalInformation(-60));
        signalInformation.put("C", new SignalInformation(-70));
        persistedPositions.add(new PositionInformation("2.", signalInformation));

        signalInformation = new HashMap<String, SignalInformation>();
        signalInformation.put("A", new SignalInformation(-80));
        signalInformation.put("B", new SignalInformation(-60));
        signalInformation.put("C", new SignalInformation(-50));
        persistedPositions.add(new PositionInformation("3.", signalInformation));


        fingerPrint = new HashMap<String, SignalInformation>();
        fingerPrint.put("A", new SignalInformation(-60));
        fingerPrint.put("B", new SignalInformation(-50));
        fingerPrint.put("C", new SignalInformation(-80));
        orderedFingerprints = orderMatcher.nearestNeighbour(cachingManager, fingerPrint, persistedPositions, false);
        for(Map.Entry<PositionInformation, Double> orderedFingerprint: orderedFingerprints.entrySet()) {
            if("1.".equals(orderedFingerprint.getKey().getName())) {
                assertEquals(0.0, orderedFingerprint.getValue());
            } else if("2.".equals(orderedFingerprint.getKey().getName())) {
                assertEquals(2.0, orderedFingerprint.getValue());
            } else if("3.".equals(orderedFingerprint.getKey().getName())) {
                assertEquals(3.0, orderedFingerprint.getValue());
            }
        }

        fingerPrint = new HashMap<String, SignalInformation>();
        fingerPrint.put("A", new SignalInformation(-10));
        fingerPrint.put("B", new SignalInformation(-50));
        fingerPrint.put("C", new SignalInformation(-70));
        orderedFingerprints = orderMatcher.nearestNeighbour(cachingManager, fingerPrint, persistedPositions, false);
        for(Map.Entry<PositionInformation, Double> orderedFingerprint: orderedFingerprints.entrySet()) {
            if("1.".equals(orderedFingerprint.getKey().getName())) {
                assertEquals(2.0, orderedFingerprint.getValue());
            } else if("2.".equals(orderedFingerprint.getKey().getName())) {
                assertEquals(0.0, orderedFingerprint.getValue());
            } else if("3.".equals(orderedFingerprint.getKey().getName())) {
                assertEquals(2.0, orderedFingerprint.getValue());
            }
        }

        fingerPrint = new HashMap<String, SignalInformation>();
        fingerPrint.put("A", new SignalInformation(-50));
        fingerPrint.put("B", new SignalInformation(-10));
        fingerPrint.put("C", new SignalInformation(-30));
        orderedFingerprints = orderMatcher.nearestNeighbour(cachingManager, fingerPrint, persistedPositions, false);
        for(Map.Entry<PositionInformation, Double> orderedFingerprint: orderedFingerprints.entrySet()) {
            if("1.".equals(orderedFingerprint.getKey().getName())) {
                assertEquals(2.0, orderedFingerprint.getValue());
            } else if("2.".equals(orderedFingerprint.getKey().getName())) {
                assertEquals(3.0, orderedFingerprint.getValue());
            } else if("3.".equals(orderedFingerprint.getKey().getName())) {
                assertEquals(2.0, orderedFingerprint.getValue());
            }
        }
    }
}