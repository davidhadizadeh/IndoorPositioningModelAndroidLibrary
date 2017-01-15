package de.hadizadeh.positioning.roommodel.android;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.hadizadeh.positioning.controller.MappedPositionManager;
import de.hadizadeh.positioning.controller.Technology;
import de.hadizadeh.positioning.exceptions.PositioningException;
import de.hadizadeh.positioning.exceptions.PositioningPersistenceException;
import de.hadizadeh.positioning.roommodel.ContentController;
import de.hadizadeh.positioning.roommodel.FileManager;
import de.hadizadeh.positioning.roommodel.Map;
import de.hadizadeh.positioning.roommodel.RoomModelPersistence;
import de.hadizadeh.positioning.roommodel.model.Material;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.List;


/**
 * Handles all loading operations for room models, content and positions. Handles threading.
 */
public class LoadingFragment extends Fragment {
    protected ViewerMap viewerMap;
    protected ContentController contentController;
    protected MappedPositionManager mappedPositionManager;
    protected RoomModelPersistence roomModelPersistence;

    protected static IncomingHandler incomingHandler;

    protected String roomModelFile;
    protected String positioningFile;
    protected String mefFile;

    protected String settingsWebserviceUrl;
    protected String settingsAuthToken;
    protected String settingsProjectName;

    protected String openingDataText;
    protected String confirmationTitle;
    protected String downloadText;
    protected String downloadConfirmation;
    protected String uploadText;
    protected String uploadConfirmation;

    protected ProgressDialog progressDialog;
    protected List<Technology> technologies;
    protected List<Material> materials;
    protected PositioningActivity positioningActivity;

    protected boolean dialogShowing = false;
    protected String progressMessage;

    /**
     * Sets the initial data
     *
     * @param positioningActivity   activity
     * @param contentController     content controller
     * @param technologies          used technologies
     * @param materials             available materials
     * @param settingsWebserviceUrl webservice url
     * @param settingsAuthToken     authentication token
     * @param settingsProjectName   current project name
     * @param mainDataPath          path to the files
     * @param openingDataText       user message for opening the data
     * @param confirmationTitle     user message title for confirmation downloads and uloads
     * @param downloadText          user message for downloading
     * @param downloadConfirmation  user message for download confirmation
     * @param uploadText            user message for uploading
     * @param uploadConfirmation    user message for upload confirmation
     */
    protected void setData(PositioningActivity positioningActivity, ContentController contentController, List<Technology> technologies, List<Material> materials,
                           String settingsWebserviceUrl, String settingsAuthToken, String settingsProjectName,
                           String mainDataPath, String openingDataText, String confirmationTitle, String downloadText,
                           String downloadConfirmation, String uploadText, String uploadConfirmation) {
        this.positioningActivity = positioningActivity;
        this.contentController = contentController;
        this.technologies = technologies;
        this.materials = materials;
        this.settingsWebserviceUrl = settingsWebserviceUrl;
        this.settingsAuthToken = settingsAuthToken;
        this.settingsProjectName = settingsProjectName;
        File appFilesPath = new File(positioningActivity.getExternalFilesDir(null).getParentFile().getParentFile().getAbsolutePath() + File.separator + mainDataPath);
        this.positioningFile = appFilesPath.getAbsolutePath() + File.separator + "positioningPersistence.xml";
        this.roomModelFile = appFilesPath.getAbsolutePath() + File.separator + "roomModel" + File.separator + "roomModel.rm";
        this.mefFile = appFilesPath.getAbsolutePath() + File.separator + "roomModel.mef";
        File positioningDir = new File(positioningFile).getParentFile();
        if (!positioningDir.exists()) {
            positioningDir.mkdirs();
        }

        this.openingDataText = openingDataText;
        this.confirmationTitle = confirmationTitle;
        this.downloadText = downloadText;
        this.downloadConfirmation = downloadConfirmation;
        this.uploadText = uploadText;
        this.uploadConfirmation = uploadConfirmation;

        ProgressDialog progressDialog = new ProgressDialog(positioningActivity);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.setMax(100);
        if (this.progressDialog == null) {
            progressDialog.setIndeterminate(true);
        } else {
            progressDialog.setIndeterminate(this.progressDialog.isIndeterminate());
            progressDialog.setMessage(progressMessage);
            progressDialog.setProgress(this.progressDialog.getProgress());
        }
        this.progressDialog = progressDialog;
        if (dialogShowing) {
            this.progressDialog.show();
        }
    }

    /**
     * Creates the loading fragment
     *
     * @param inflater           layout inflater
     * @param container          layout container
     * @param savedInstanceState saved instances
     * @return created view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        //View view = inflater.inflate(layoutIdFragmentMap, container, false);

        setRetainInstance(true);
        if (incomingHandler == null) {
            incomingHandler = new IncomingHandler(this);
        } else {
            incomingHandler.setCallback(this);
        }
        return null;
    }

    /**
     * Dismisses all dialogs
     */
    protected void dismissDialogs() {
        if (progressDialog != null && progressDialog.isShowing()) {
            dialogShowing = true;
            progressDialog.dismiss();
        } else {
            dialogShowing = false;
        }
    }

    /**
     * Initializes the positioning manager
     */
    protected void initPositioningManager() {
        if (mappedPositionManager != null) {
            mappedPositionManager.stopPositioning();
        }
        try {
            mappedPositionManager = new MappedPositionManager(new File(positioningFile));
            for (Technology technology : technologies) {
                mappedPositionManager.addTechnology(technology);
            }
        } catch (PositioningPersistenceException e) {
            e.printStackTrace();
        } catch (PositioningException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads the room model data
     */
    protected void openData() {
        if (mappedPositionManager == null) {
            initPositioningManager();
        }
        roomModelPersistence = new ViewerRoomModelPersistence(contentController, mappedPositionManager.getMappedPositionMappingPoints());
        progressMessage = openingDataText;
        progressDialog.setMessage(progressMessage);
        progressDialog.setIndeterminate(true);
        progressDialog.show();
        new Thread() {
            public void run() {
                boolean loadingSuccess = true;
                File roomModelFolder = new File(roomModelFile).getParentFile();
                if (!roomModelFolder.exists()) {
                    File decompressedFolder = FileManager.decompress(new File(mefFile), roomModelFolder);
                    loadingSuccess = (decompressedFolder != null);
                }
                if (loadingSuccess) {
                    Map createdMap = null;
                    try {
                        String workingDirectory = roomModelFolder.getAbsolutePath();
                        contentController.preloadAllContents(workingDirectory);
                        createdMap = roomModelPersistence.load(roomModelFile, materials);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        loadingSuccess = false;
                    }
                    viewerMap = (ViewerMap) createdMap;
                }
                boolean success = loadingSuccess;
                Message message = incomingHandler.obtainMessage();
                message.obj = success;
                incomingHandler.sendMessage(message);
            }
        }.start();
    }

    /**
     * Downloads the room model editor file
     */
    protected void downloadMefData() {
        String url = settingsWebserviceUrl + settingsProjectName + "/mef/";
        progressMessage = downloadText;
        progressDialog.setMessage(progressMessage);
        progressDialog.setIndeterminate(false);
        final DownloadCheckMefTask downloadTask = new DownloadCheckMefTask();
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                downloadTask.cancel(true);
            }
        });
        downloadTask.execute(url, settingsAuthToken);
    }

    /**
     * Downloads the positioning file (fingerprints)
     */
    protected void downloadPositioningFile() {
        downloadPositioningFile(true);
    }

    /**
     * Downloads the positioning file (fingerprints)
     *
     * @param dismissLoading true if the dialog should be dismiss, else it will stay opened
     */
    protected void downloadPositioningFile(boolean dismissLoading) {
        String url = settingsWebserviceUrl + settingsProjectName + "/positioning/";
        progressMessage = downloadText;
        progressDialog.setMessage(progressMessage);
        progressDialog.setIndeterminate(false);
        new DownloadPositioningTask(dismissLoading).execute(url, settingsAuthToken, positioningFile);
    }

    /**
     * Uploads the current positioning file (fingerprints)
     */
    protected void uploadPositioningFile() {
        positioningActivity.confirmUser(confirmationTitle, uploadConfirmation, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String url = settingsWebserviceUrl + settingsProjectName + "/positioning/";
                progressMessage = uploadText;
                progressDialog.setMessage(progressMessage);
                progressDialog.setIndeterminate(false);
                new UploadPositioningTask().execute(url, settingsAuthToken, positioningFile);
            }
        });
    }

    /**
     * Called when the loading is finished
     */
    protected void dataLoaded() {
        PositioningActivity.setMappedPositionManager(mappedPositionManager);
        PositioningActivity.setViewerMap(viewerMap);
        positioningActivity.dataLoaded();
    }

    private static class IncomingHandler extends Handler {
        private LoadingFragment loadingFragment;

        IncomingHandler(LoadingFragment loadingFragment) {
            this.loadingFragment = loadingFragment;
        }

        public void setCallback(LoadingFragment loadingFragment) {
            this.loadingFragment = loadingFragment;
        }

        @Override
        public void handleMessage(Message msg) {
            loadingFragment.progressDialog.dismiss();
            if (loadingFragment.progressDialog.isShowing()) {
                loadingFragment.progressDialog.dismiss();
            }
            boolean success = (Boolean) msg.obj;
            if (success) {
                loadingFragment.dataLoaded();
            }
        }
    }

    private class DownloadCheckMefTask extends AsyncTask<String, Integer, Integer> {
        private String downloadUrl;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(String... data) {
            String hash = FileManager.calculateHash(new File(mefFile));
            downloadUrl = data[0];
            String checkUrl = downloadUrl + "check/";
            if (hash == null) {
                downloadUrl += "-";
                checkUrl += "-";
            } else {
                downloadUrl += hash;
                checkUrl += hash;
            }
            int statusCode = FileManager.checkFileState(checkUrl, data[1]);
            return statusCode;
        }

        @Override
        protected void onPostExecute(Integer statusCode) {
            if (statusCode == HttpURLConnection.HTTP_NO_CONTENT) {
                openData();
            } else {
                positioningActivity.confirmUser(confirmationTitle, downloadConfirmation, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        new DownloadMefTask().execute(downloadUrl, settingsAuthToken, mefFile);
                    }
                }, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        positioningActivity.finish();
                    }
                });
            }
        }
    }

    private class DownloadMefTask extends AsyncTask<String, Integer, Integer> implements FileManager.ProgressListener {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected Integer doInBackground(String... data) {
            int statusCode = FileManager.downloadFile(data[0], data[1], new File(data[2]), this);
            return statusCode;
        }

        @Override
        public void progress(int progress) {
            super.onProgressUpdate(progress);
            progressDialog.setProgress(progress);
        }

        @Override
        protected void onPostExecute(Integer statusCode) {
            try {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            } catch (IllegalArgumentException ex) {
            }
            if (statusCode != HttpURLConnection.HTTP_OK && statusCode != HttpURLConnection.HTTP_NO_CONTENT) {
                // TODO: SHOW ERROR
                System.out.println("ERROR WHILE DOWNLOADING: " + statusCode);
            } else if (statusCode == HttpURLConnection.HTTP_NO_CONTENT) {
                openData();
            } else {
                FileManager.removeDirectory(new File(roomModelFile).getParentFile());
                openData();
            }
            progressDialog.setOnCancelListener(null);
        }
    }

    private class DownloadPositioningTask extends AsyncTask<String, Integer, Integer> implements FileManager.ProgressListener {
        private boolean dismissLoading;

        public DownloadPositioningTask(boolean dismissLoading) {
            this.dismissLoading = dismissLoading;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (dismissLoading) {
                progressDialog.show();
            }
        }

        @Override
        protected Integer doInBackground(String... data) {
            String hash = FileManager.calculateHash(new File(positioningFile));
            String downloadUrl = data[0];
            if (hash == null) {
                downloadUrl += "-";
            } else {
                downloadUrl += hash;
            }
            int statusCode = FileManager.downloadFile(downloadUrl, data[1], new File(data[2]), this);
            return statusCode;
        }

        @Override
        protected void onPostExecute(Integer statusCode) {
            try {
                if (dismissLoading && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            } catch (IllegalArgumentException ex) {
            }
            if (statusCode != HttpURLConnection.HTTP_OK) {
                // TODO: SHOW ERROR
                System.out.println("ERROR WHILE DOWNLOADING POSITIONING FILE: " + statusCode);
            }
            initPositioningManager();
        }

        @Override
        public void progress(int progress) {
            super.onProgressUpdate(progress);
            progressDialog.setProgress(progress);
        }
    }

    private class UploadPositioningTask extends AsyncTask<String, Integer, Integer> implements FileManager.ProgressListener {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected Integer doInBackground(String... data) {
            int statusCode = FileManager.uploadFile(data[0], data[1], new File(data[2]), this);
            return statusCode;
        }

        @Override
        protected void onPostExecute(Integer statusCode) {
            try {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            } catch (IllegalArgumentException ex) {
            }
            if (statusCode != HttpURLConnection.HTTP_CREATED) {
                // TODO: SHOW ERROR
                System.out.println("ERROR WHILE UPLOADING: " + statusCode);
            }
        }

        @Override
        public void progress(int progress) {
            super.onProgressUpdate(progress);
            progressDialog.setProgress(progress);
        }
    }
}
