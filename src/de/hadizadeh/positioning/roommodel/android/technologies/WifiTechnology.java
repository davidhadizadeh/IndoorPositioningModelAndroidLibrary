package de.hadizadeh.positioning.roommodel.android.technologies;


import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import de.hadizadeh.positioning.controller.Technology;
import de.hadizadeh.positioning.model.SignalInformation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wifi fingerprinting technology
 */
public class WifiTechnology extends Technology {
    private WifiManager wifiManager;

    /**
     * Creates the technology
     *
     * @param context      activity context
     * @param name         name of the technologies
     * @param keyWhiteList whitelist of allowed wifi access points or null, if every access point should be allowed
     */
    public WifiTechnology(Context context, String name, List<String> keyWhiteList) {
        super(name, keyWhiteList);
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    /**
     * Returns current wifi signal data
     *
     * @return wifi signal data
     */
    @Override
    public Map<String, SignalInformation> getSignalData() {
        Map<String, SignalInformation> signalData = new HashMap<String, SignalInformation>();
        wifiManager.startScan();
        List<ScanResult> scanResults = wifiManager.getScanResults();
        for (final ScanResult scanResult : scanResults) {
            signalData.put(scanResult.BSSID, new SignalInformation(scanResult.level));
        }
        return signalData;
    }
}
