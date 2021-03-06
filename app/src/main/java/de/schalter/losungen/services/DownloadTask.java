package de.schalter.losungen.services;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import de.schalter.losungen.MainActivity;
import de.schalter.losungen.R;
import de.schalter.losungen.files.Files;

/**
 * Created by Smarti on 27.12.2015
 */
public class DownloadTask extends AsyncTask<Integer, Integer, Void> {
    private DownloadNotificationHelper mNotificationHelper;

    private Context context;
    private String url;
    private String folder;
    private String fileName;
    private String absolutePath;
    private boolean internal;
    private int progress;

    private Runnable finished;
    private Runnable onUpdate;

    private boolean error;

    public DownloadTask(Context context, String url, String folder,
                        String fileName, boolean internal, int resourceTitleNotification, int resourceSubtitleNotification){
        this.context = context;
        this.folder = folder;
        this.fileName = fileName;
        this.url = url;
        this.internal = internal;

        error =false;

        mNotificationHelper = new DownloadNotificationHelper(context, resourceTitleNotification, resourceSubtitleNotification);
    }

    public void onFinishedListener(Runnable runnable) {
        finished = runnable;
    }

    public void setOnProgressUpdateListener(Runnable runnable) {
        onUpdate = runnable;
    }

    protected void onPreExecute(){
        //Create the notification in the statusbar
        mNotificationHelper.createNotification();
    }

    @Override
    protected Void doInBackground(Integer... integers) {
        final Files files = new Files();
        Runnable progressRunnable = new Runnable() {
            @Override
            public void run() {
                publishProgress(files.getProgress());
            }
        };
        files.setOnProgress(progressRunnable);

        try {
            URL url = new URL(this.url);

            Log.d("Losungen", "Url-tast: " + url);

            URLConnection connexion = url.openConnection();
            //URLConnection connexion = url.openConnection();
            connexion.connect();

            files.setLenghtOfFile(connexion.getContentLength());

            InputStream input = new BufferedInputStream(url.openStream());
            //Write to internal storage
            if(internal) {
                absolutePath = files.writeToPrivateStorage(context, input, folder, fileName);
            } else { //Write to external storage
                try {
                    absolutePath = files.writeToRealExternalCacheStorage(context, input,
                            folder, fileName);

                    if (absolutePath == null) {
                        //couldn't write to external sd-card, try internal sd-card
                        absolutePath = files.writeToExternalPrivateStorage(input, fileName, folder);
                    }

                    if (absolutePath == null) {
                        //Error writing to external resource
                        MainActivity.toast(context,
                                context.getResources().getString(R.string.failed_external),
                                Toast.LENGTH_LONG);

                        mNotificationHelper.error("Failed to write to external and internal storage");
                        error = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    //Write to internal storage
                    absolutePath = files.writeToPrivateStorage(context, input, folder, fileName);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            error = true;
            mNotificationHelper.error(e.getMessage());
        }
        return null;
    }

    private int progressBefore = 0;

    protected void onProgressUpdate(Integer... progress) {
        //This method runs on the UI thread, it receives progress updates
        //from the background thread and publishes them to the status bar
        if(progress[0] != progressBefore) {
            progressBefore = progress[0];
            mNotificationHelper.progressUpdate(progress[0]);
            this.progress = progress[0];

            if (onUpdate != null)
                onUpdate.run();
        }
    }

    @Override
    protected void onPostExecute(Void result)    {
        //The task is complete, tell the status bar about it
        mNotificationHelper.completed();

        if(finished != null && !error) {
            finished.run();
        }
    }

    @Override
    protected void onCancelled(Void result){
        super.onCancelled(result);
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    /**
     * @return progress in xx%
     */
    public int getProgress() {
        return progress;
    }
}
