package de.schalter.losungen.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;

import de.schalter.losungen.Losung;
import de.schalter.losungen.R;
import de.schalter.losungen.files.DBHandler;
import de.schalter.losungen.settings.Tags;

/**
 * Created by Smarti on 15.01.2016.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

        boolean languageSupported = Arrays.asList(Tags.ANDACHT_LANGUAGES).contains(Tags.getLanguage(context));

        boolean wifi = Tags.isWifiConnected(context);
        boolean mobile = Tags.isMobileConnected(context);
        //Wegen der Sprache wird hier "false" als Standartwert ausgewählt
        //Für andere Sprachen is das nämlich noch nicht vefügbar
        boolean downloadAudio = settings.getBoolean(Tags.PREF_AUDIO_DOWNLOAD, false);
        boolean autoDownloadAudio = settings.getBoolean(Tags.PREF_AUDIO_AUTODOWNLOAD, false);
        boolean onlyWifi = settings.getString(Tags.PREF_AUDIO_AUTODOWNLOAD_NETWORK, "0").equals("0");

        if(autoDownloadAudio && downloadAudio && wifi && languageSupported) {
            downloadAudio();
        } else if(autoDownloadAudio && downloadAudio && !onlyWifi && mobile && languageSupported) {
            downloadAudio();
        }

    }

    private void downloadAudio() {
        final DBHandler dbHandler = DBHandler.newInstance(context);
        long datumJetzt = System.currentTimeMillis();
        final long datum = dbHandler.getLosung(datumJetzt).getDatum();
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

        Thread download = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //get URL first
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(datum);
                    String url = Tags.getAudioUrl(calendar);

                    //set Path
                    String folder = "audio";
                    String fileName = context.getString(R.string.app_name) + "_" + Losung.getDatumLongFromTime(datum) + ".mp3";

                    //use internal or external storage
                    boolean internal = !settings.getBoolean(Tags.PREF_AUDIO_EXTERNAL_STORGAE, false);

                    final DownloadTask downloadTask = new DownloadTask(context, url,
                            folder, fileName, internal, R.string.download_ticker, R.string.content_title);

                    //When finished
                    Runnable finished = new Runnable() {
                        @Override
                        public void run() {
                            //Write into database
                            String absolutePath = downloadTask.getAbsolutePath();
                            dbHandler.addAudioLosungen(datum, absolutePath);

                            //We dont want to play the file
                            //playFile(absolutePath);
                        }
                    };
                    downloadTask.onFinishedListener(finished);

                    //Start download with notification
                    downloadTask.execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        //Check if audio-file exists allready
        String pathAudioLosung = dbHandler.getAudioLosungen(datum);
        if(pathAudioLosung != null) { //Es wurde bereits ein Pfad gespeichert
            //Es kann aber immer noch sein, dass der Pfad nicht mehr stimmt
            //Wenn zum Beispiel die SD-Karte entfernt wurde
            //Deswegen wird überprüft ob die Datei existiert
            File file = new File(pathAudioLosung);
            if(file.exists()) {
                //playFile(pathAudioLosung);
                //Do nothing
            } else {
                download.start();
            }
        } else {
            //Audio download and write into database
            download.start();
        }
    }
}
