package de.hadizadeh.positioning.roommodel.android;

import android.app.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import de.hadizadeh.positioning.controller.ActiveTechnology;
import de.hadizadeh.positioning.controller.MappedPositionManager;
import de.hadizadeh.positioning.controller.Technology;
import de.hadizadeh.positioning.model.MappingPoint;
import de.hadizadeh.positioning.roommodel.ContentController;
import de.hadizadeh.positioning.roommodel.android.technologies.NfcHandler;
import de.hadizadeh.positioning.roommodel.android.technologies.NfcHandlerInterface;
import de.hadizadeh.positioning.roommodel.model.Material;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines the structure of a positioning activity
 */
public abstract class PositioningActivity extends Activity {
    protected static ViewerMap viewerMap;
    protected static MappedPositionManager mappedPositionManager;
    protected static ContentController contentController;

    protected List<Material> materials;
    protected FragmentManager fragmentManager;
    protected NfcHandlerInterface nfcHandler;

    protected String mainDataPath;

    protected String[] materialNames;
    protected int layoutIdfragmentBox;
    protected String openingDataText;
    protected String enableLocationServiceText;
    protected String enableLocationServiceYes;
    protected String enableLocationServiceNo;
    protected String confirmationTitle;
    protected String downloadText;
    protected String downloadConfirmation;
    protected String uploadText;
    protected String uploadConfirmation;

    protected String settingsWebserviceUrl;
    protected String settingsAuthToken;
    protected String settingsProjectName;

    protected LoadingFragment loadingFragment;
    protected List<Technology> technologies = new ArrayList<Technology>();
    protected boolean loadDataOnStartUp = true;

    /**
     * Sets the necessary data
     *
     * @param mainDataPath              path to the data folder
     * @param materialNames             all material names
     * @param layoutIdfragmentBox       layout id of the fragment box
     * @param openingDataText           text for opening data
     * @param enableLocationServiceText text for enabling location services
     * @param enableLocationServiceYes  text for yes confirmation
     * @param enableLocationServiceNo   text for no confirmation
     * @param confirmationTitle         title for the confirmation
     * @param downloadText              text for downloading
     * @param downloadConfirmation      text for download confirmation
     */
    protected void setData(String mainDataPath, String[] materialNames, int layoutIdfragmentBox,
                           String openingDataText, String enableLocationServiceText, String enableLocationServiceYes,
                           String enableLocationServiceNo, String confirmationTitle, String downloadText, String downloadConfirmation) {
        setData(mainDataPath, materialNames, layoutIdfragmentBox, openingDataText, enableLocationServiceText, enableLocationServiceYes,
                enableLocationServiceNo, confirmationTitle, downloadText, downloadConfirmation, null, null);
    }

    /**
     * Sets the necessary data
     *
     * @param mainDataPath              path to the data folder
     * @param materialNames             all material names
     * @param layoutIdfragmentBox       layout id of the fragment box
     * @param openingDataText           text for opening data
     * @param enableLocationServiceText text for enabling location services
     * @param enableLocationServiceYes  text for yes confirmation
     * @param enableLocationServiceNo   text for no confirmation
     * @param confirmationTitle         title for the confirmation
     * @param downloadText              text for downloading
     * @param downloadConfirmation      text for download confirmation
     * @param uploadText                text for uploading
     * @param uploadConfirmation        text for upload confirmation
     */
    protected void setData(String mainDataPath, String[] materialNames, int layoutIdfragmentBox,
                           String openingDataText, String enableLocationServiceText, String enableLocationServiceYes,
                           String enableLocationServiceNo, String confirmationTitle, String downloadText,
                           String downloadConfirmation, String uploadText, String uploadConfirmation) {
        if (contentController == null) {
            contentController = new ContentController();
        }
        this.mainDataPath = mainDataPath;
        this.materialNames = materialNames;
        this.layoutIdfragmentBox = layoutIdfragmentBox;

        this.openingDataText = openingDataText;
        this.enableLocationServiceText = enableLocationServiceText;
        this.enableLocationServiceYes = enableLocationServiceYes;
        this.enableLocationServiceNo = enableLocationServiceNo;
        this.confirmationTitle = confirmationTitle;
        this.downloadText = downloadText;
        this.downloadConfirmation = downloadConfirmation;
        this.uploadText = uploadText;
        this.uploadConfirmation = uploadConfirmation;
        loadSettings();
    }

    /**
     * Adds a technology
     *
     * @param technology technology
     */
    protected void addTechnology(Technology technology) {
        technologies.add(technology);
        if (isNfcTechnology(technology)) {
            nfcHandler = new NfcHandler(this);
            nfcHandler.startScanning();
        }
    }

    /**
     * Loads the shared preference settings
     */
    protected void loadSettings() {
        SharedPreferences sharedPref = getSharedPreferences(Settings.PREFERENCE_FILE, Context.MODE_PRIVATE);
        settingsWebserviceUrl = sharedPref.getString(Settings.WEBSERVICE, null);
        settingsAuthToken = sharedPref.getString(Settings.TOKEN, null);
        settingsProjectName = sharedPref.getString(Settings.PROJECT, null);
    }

    /**
     * Creates the activity
     *
     * @param savedInstanceState saved instance
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentManager = getFragmentManager();
        materials = new ArrayList<Material>();
        String[][] materialValues = Material.getDefaultMaterialValues();
        for (int i = 0; i < materialValues.length; i++) {
            addMaterial(materialValues[i], materialNames[i]);
        }

        if (loadDataOnStartUp) {
            Fragment currentFragment = fragmentManager.findFragmentById(layoutIdfragmentBox);
            if (currentFragment instanceof LoadingFragment) {
                loadingFragment = (LoadingFragment) currentFragment;
            } else if (viewerMap == null) {
                downloadMefData();
            }
            setLoadingFragmentData();
        } else {
            loadDataOnStartUp = true;
        }
        enableLocationService();
    }

    /**
     * Resumes the nfc handler
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (nfcHandler != null) {
            nfcHandler.resume();
        }
    }

    /**
     * Pauses the nfc handler
     */
    @Override
    protected void onPause() {
        if (nfcHandler != null) {
            nfcHandler.pause();
        }
        super.onPause();
    }

    /**
     * Dismisses all dialogs
     */
    @Override
    protected void onDestroy() {
        if (loadingFragment != null) {
            loadingFragment.dismissDialogs();
        }
        super.onDestroy();
    }

    /**
     * Enables the location services with user interaction
     */
    protected void enableLocationService() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(enableLocationServiceText)
                    .setCancelable(false)
                    .setPositiveButton(enableLocationServiceYes, new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton(enableLocationServiceNo, new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            dialog.cancel();
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        }
    }

    /**
     * Checks if a technology is a nfc technology
     *
     * @param technology technology
     * @return true, if it is an nfc technology, else it is not
     */
    protected boolean isNfcTechnology(Technology technology) {
        if (technology instanceof ActiveTechnology && technology.getName().toLowerCase().contains("nfc")) {
            return true;
        }
        return false;
    }

    /**
     * New nfc tag detected
     *
     * @param intent intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        ActiveTechnology nfcTechnology = null;
        for (Technology technology : technologies) {
            if (isNfcTechnology(technology)) {
                nfcTechnology = (ActiveTechnology) technology;
            }
        }

        if (nfcTechnology != null) {
            try {
                if (nfcHandler.tagMatches(intent.getAction())) {
                    String tagId = nfcHandler.getTagId(intent);
                    nfcTechnology.idDetected(tagId, 5000);
                    Log.i("NFC-Tag-ID", tagId);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        super.onNewIntent(intent);
    }

    /**
     * Adds a material
     *
     * @param materialValues material values
     * @param name           name of the material
     */
    protected void addMaterial(String[] materialValues, String name) {
        materials.add(new ViewerMaterial(this, materialValues[0], name, materialValues[1], materialValues[2]));
    }

    /**
     * Called if opening fails
     */
    protected void showOpeningError() {
        // TODO
    }

    /**
     * Data loading finished
     */
    protected void dataLoaded() {
        removeWrongMappingPoints();
        fragmentManager.popBackStackImmediate();
    }

    /**
     * Sets the room model map
     *
     * @param viewerMap
     */
    public static void setViewerMap(ViewerMap viewerMap) {
        PositioningActivity.viewerMap = viewerMap;
    }

    /**
     * Sets the position manager
     *
     * @param mappedPositionManager position manager
     */
    public static void setMappedPositionManager(MappedPositionManager mappedPositionManager) {
        PositioningActivity.mappedPositionManager = mappedPositionManager;
    }

    /**
     * Removes wrong mapping points
     */
    protected void removeWrongMappingPoints() {
        for (MappingPoint mappingPoint : mappedPositionManager.getMappedPositionMappingPoints()) {
            if (mappingPoint.getZ() / viewerMap.getFloorHeight() >= viewerMap.getFloors() || mappingPoint.getY() >= viewerMap.getRows() || mappingPoint.getX() >= viewerMap.getColumns()) {
                mappedPositionManager.removeMappedPosition(mappingPoint);
            }
        }
    }

    /**
     * Downloads the model editor file data
     */
    protected void downloadMefData() {
        LoadingFragment loadingFragment = setLoadingFragment();
        loadingFragment.downloadMefData();
    }

    /**
     * Downloads the positioning data (fingerprints)
     */
    protected void downloadPositioningFile() {
        downloadPositioningFile(true);
    }

    /**
     * Downloads the positioning data (fingerprints)
     *
     * @param dismissLoading true if the dialog should be dismiss, else it will stay opened
     */
    protected void downloadPositioningFile(boolean dismissLoading) {
        LoadingFragment loadingFragment = setLoadingFragment();
        loadingFragment.downloadPositioningFile(dismissLoading);
    }

    /**
     * Uploads the positioning file
     */
    protected void uploadPositioningFile() {
        LoadingFragment loadingFragment = setLoadingFragment();
        loadingFragment.uploadPositioningFile();
    }

    /**
     * Sets the current fragment to the loading fragment for preparing loading operations
     *
     * @return reference to the loading fragment
     */
    protected LoadingFragment setLoadingFragment() {
        loadingFragment = new LoadingFragment();
        setLoadingFragmentData();
        // replaceFragment(loadingFragment);

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(layoutIdfragmentBox, loadingFragment, null);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        fragmentManager.executePendingTransactions();

        return loadingFragment;
    }

    /**
     * Sets the loading fragment data
     */
    protected void setLoadingFragmentData() {
        if (loadingFragment != null) {
            loadingFragment.setData(this, contentController, technologies, materials, settingsWebserviceUrl, settingsAuthToken, settingsProjectName,
                    mainDataPath, openingDataText, confirmationTitle, downloadText, downloadConfirmation, uploadText, uploadConfirmation);
        }
    }

    /**
     * Asks for user confirmation
     *
     * @param title         title of the question
     * @param message       text of the question
     * @param successAction Action which wil be called if the user accepts
     */
    protected void confirmUser(String title, String message, DialogInterface.OnClickListener successAction) {
        confirmUser(title, message, successAction, null);
    }

    /**
     * Asks for user confirmation
     *
     * @param title         title of the question
     * @param message       text of the question
     * @param successAction Action which wil be called if the user accepts
     * @param cancelAction  Action which wil be called if the user cacels
     */
    protected void confirmUser(String title, String message, DialogInterface.OnClickListener successAction, DialogInterface.OnClickListener cancelAction) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, successAction)
                .setNegativeButton(android.R.string.no, cancelAction).show();
    }

    /**
     * Replaces the current fragment with a new one
     *
     * @param fragment new fragment
     */
    protected void replaceFragment(Fragment fragment) {
        fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(layoutIdfragmentBox, fragment);
        fragmentTransaction.commitAllowingStateLoss();
    }
}
