package de.hadizadeh.positioning.roommodel.android;


import de.hadizadeh.positioning.controller.CachingManager;
import de.hadizadeh.positioning.model.SignalInformation;

import java.util.HashMap;
import java.util.Map;

/**
 * Caching manager which callifies all signal data balanced
 */
public class BalanceCachingManager extends CachingManager {

    /**
     * Creates a caching manager with a default caching size of 1.
     */
    public BalanceCachingManager() {
        init(1);
    }

    /**
     * Creates a caching manager with a given caching size.
     *
     * @param cacheSize caching size
     */
    public BalanceCachingManager(int cacheSize) {
        init(cacheSize);
    }

    /**
     * Interpolates caching data.
     * Classifies all signal data balanced.
     *
     * @return interpolated caching data
     */
    @Override
    public Map<String, SignalInformation> interpolateData() {
        Map<String, SignalInformation> result = new HashMap<String, SignalInformation>();
        Map<String, Integer> amounts = new HashMap<String, Integer>();
        if (cachingData != null) {
            for (Map<String, SignalInformation> cachingElement : cachingData) {
                for (Map.Entry<String, SignalInformation> positionElement : cachingElement.entrySet()) {
                    double value = positionElement.getValue().getStrength();
                    int amount = 1;
                    if (result.containsKey(positionElement.getKey())) {
                        value += result.get(positionElement.getKey()).getStrength();
                        amount += amounts.get(positionElement.getKey());
                    }
                    amounts.put(positionElement.getKey(), amount);
                    result.put(positionElement.getKey(), new SignalInformation(value));
                }
            }
            for (Map.Entry<String, SignalInformation> signalInformation : result.entrySet()) {
                signalInformation.getValue().setStrength(signalInformation.getValue().getStrength() / amounts.get(signalInformation.getKey()));
            }
        }
        return result;
    }
}
