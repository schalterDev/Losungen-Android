package de.schalter.losungen.services;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Smarti on 27.12.2015.
 */
public class DownloadTask extends AsyncTask<Integer, Integer, Void> {
    private DownloadNotificationHelper mNotificationHelper;

    private Context context;
    private String url;
    private String path;
    private String absolutePath;
    private boolean internal;
    private int progress;

    private Runnable finished;
    private Runnable onUpdate;

    public DownloadTask(Context context, String url, String path, boolean internal){
        this.context = context;
        this.path = path;
        this.url = url;
        this.internal = internal;
        mNotificationHelper = new DownloadNotificationHelper(context);
    }

    public void onFinishedListener(Runnable runnable) {
        finished = runnable;
    }

    public void onProgressUpdateListener(Runnable runnable) {
        onUpdate = runnable;
    }

    protected void onPreExecute(){
        //Create the notification in the statusbar
        mNotificationHelper.createNotification();
    }

    @Override
    protected Void doInBackground(Integer... integers) {
        try {
            int count;
            URL url = new URL(this.url);
            URLConnection connexion = url.openConnection();
            connexion.connect();

            int lenghtOfFile = connexion.getContentLength();

            InputStream input = new BufferedInputStream(url.openStream());
            File file;
            //Write to internal storage
            if(internal) {
                file = new File(context.getFilesDir(), path);
                //Create directorys
                new File(context.getFilesDir(), path.substring(0,path.lastIndexOf("/"))).mkdirs();
            } else { //Write to external storage
                file = new File(context.getExternalFilesDir(null), path);
                //Create directorys
                boolean success = new File(context.getExternalFilesDir(null), path.substring(0,path.lastIndexOf("/"))).mkdirs();
            }

            absolutePath = file.getAbsolutePath();

            OutputStream output = new FileOutputStream(file);
            byte data[] = new byte[1024];

            long total = 0;

            while ((count = input.read(data)) != -1) {
                total += count;
                publishProgress((int)((total*100)/lenghtOfFile));
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();

        } catch (Exception e) {
            e.printStackTrace();
            mNotificationHelper.error();
            cancel(true);
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

        if(finished != null) {
            finished.run();
        }
    }

    @Override
    protected void onCancelled(Void result){
        mNotificationHelper.completed();
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
