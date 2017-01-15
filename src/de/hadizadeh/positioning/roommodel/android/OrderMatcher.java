package de.hadizadeh.positioning.roommodel.android;

import de.hadizadeh.positioning.controller.CachingManager;
import de.hadizadeh.positioning.controller.Matcher;
import de.hadizadeh.positioning.model.PositionInformation;
import de.hadizadeh.positioning.model.SignalInformation;

import java.util.*;

/**
 * Matcher which matches the fingerprints by order
 */
public class OrderMatcher extends Matcher {

    /**
     * Matches the fingerprints by order instead of the default matching
     *
     * @param cachingManager     caching manager
     * @param fingerPrint        fingerprint
     * @param persistedPositions persisted fingerprints
     * @param ignoreDisabledAPs  not in use for this implementation
     * @return matched fingerprints
     */
    @Override
    public Map<PositionInformation, Double> nearestNeighbour(CachingManager cachingManager, Map<String, SignalInformation> fingerPrint, List<PositionInformation> persistedPositions, boolean ignoreDisabledAPs) {
        Map<PositionInformation, Double> orderResults = new HashMap<PositionInformation, Double>();
        cachingManager.addData(fingerPrint);
        final Map<String, SignalInformation> interpolatedFingerPrint = cachingManager.interpolateData();

        // Order finger prints
        Comparator<String> fingerPrintComparator = new Comparator<String>() {
            @Override
            public int compare(String k1, String k2) {
                return ((Double) interpolatedFingerPrint.get(k1).getStrength()).compareTo(interpolatedFingerPrint.get(k2).getStrength());
            }
        };
        TreeMap<String, SignalInformation> sortedFingerPrint = new TreeMap<String, SignalInformation>(fingerPrintComparator);
        sortedFingerPrint.putAll(interpolatedFingerPrint);

        for (PositionInformation positionInformation : persistedPositions) {
            int countDifferences = 0;
            final Map<String, SignalInformation> persistedSignalInformation = positionInformation.getSignalInformation();
            Comparator<String> persistedFpComparator = new Comparator<String>() {
                @Override
                public int compare(String k1, String k2) {
                    return ((Double) persistedSignalInformation.get(k1).getStrength()).compareTo(persistedSignalInformation.get(k2).getStrength());
                }
            };
            TreeMap<String, SignalInformation> sortedPersistedSi = new TreeMap<String, SignalInformation>(persistedFpComparator);
            sortedPersistedSi.putAll(persistedSignalInformation);

            String[] mapKeys = new String[sortedPersistedSi.size()];
            int pos = 0;
            for (String key : sortedPersistedSi.keySet()) {
                mapKeys[pos++] = key;
            }

            int keyIndex = 0;
            for (Map.Entry<String, SignalInformation> fingerPrintElement : sortedFingerPrint.entrySet()) {
                String mapKeyText = "";
                if (keyIndex < mapKeys.length) {
                    mapKeyText = mapKeys[keyIndex];
                }
                //System.out.println("COMPARE: " +mapKeys.length + " <= "+ keyIndex + " || " + fingerPrintElement.getKey() + " == "+mapKeyText);
                if (mapKeys.length <= keyIndex || keyIndex >= mapKeys.length || !fingerPrintElement.getKey().equals(mapKeys[keyIndex])) {
                    countDifferences++;
                }
                keyIndex++;
            }
            orderResults.put(positionInformation, (double) countDifferences);
        }
        return orderResults;
    }
}
