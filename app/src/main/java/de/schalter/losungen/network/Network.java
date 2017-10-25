package de.schalter.losungen.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.Calendar;

import javax.net.ssl.HttpsURLConnection;

import de.schalter.losungen.Losung;
import de.schalter.losungen.MainActivity;
import de.schalter.losungen.R;
import de.schalter.losungen.files.DBHandler;
import de.schalter.losungen.rss.SermonUrl;
import de.schalter.losungen.services.DownloadTask;
import de.schalter.losungen.settings.Tags;

/**
 * Created by martin on 09.06.17.
 */

public class Network {

    public static String downloadHtml(String urlString) throws IOException {
        //URLConnection urlConnection = url.openConnection();
        HttpURLConnection ucon = null;

        int counter = 5;

        while (counter > 0)
        {
            URL url = new URL(urlString);
            Log.d("Losungen", "url: " + urlString);

            ucon = (HttpURLConnection) url.openConnection();
            ucon.setConnectTimeout(15000);
            ucon.setReadTimeout(15000);
            ucon.setInstanceFollowRedirects(false);   // Make the logic below easier to detect redirections
            ucon.setRequestProperty("User-Agent", "Mozilla/5.0...");

            counter--;

            switch (ucon.getResponseCode())
            {
                case HttpURLConnection.HTTP_MOVED_PERM:
                case HttpURLConnection.HTTP_MOVED_TEMP:
                    String location = ucon.getHeaderField("Location");
                    location = URLDecoder.decode(location, "UTF-8");
                    URL base = new URL(urlString);
                    URL next = new URL(base, location);  // Deal with relative URLs
                    urlString = next.toExternalForm();
                    continue;
            }

            break;
        }


        InputStream is = ucon.getInputStream();
        BufferedInputStream bis = new BufferedInputStream(is);

        /*
         * Read bytes to the Buffer until there is nothing more to read(-1).
         */
        byte[] contents = new byte[1024];

        int bytesRead=0;
        String html = "";
        while( (bytesRead = bis.read(contents)) != -1){
            html += new String(contents, 0, bytesRead);
        }

        return html;
    }

    /**
     * Downlaod sermon from today
     * @param context
     */
    public static void downloadSermon(Context context, NetworkListener listener) {
        Calendar calendar = Calendar.getInstance();

        downloadSermon(context, listener, calendar);
    }

    public static void downloadSermon(Context context, NetworkListener listener, Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        downloadSermon(context, listener, calendar.getTimeInMillis());
    }

    public static void downloadSermon(Context context, NetworkListener listener, long date) {
        final DBHandler dbHandler = DBHandler.newInstance(context);

        //check if sermon already exists
        String pathAudioLosung = dbHandler.getAudioLosungen(date);
        if(pathAudioLosung != null) { //Es wurde bereits ein Pfad gespeichert
            //Es kann aber immer noch sein, dass der Pfad nicht mehr stimmt
            //Wenn zum Beispiel die SD-Karte entfernt wurde
            //Deswegen wird überprüft ob die Datei existiert
            File file = new File(pathAudioLosung);
            if(file.exists()) {
                //Do nothing
            } else {
                download(context, listener, date);
            }
        } else {
            //Audio download and write into database
            download(context, listener, date);
        }
    }

    private static void download(final Context context, final NetworkListener listener, long date) {
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        final DBHandler dbHandler = DBHandler.newInstance(context);
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);

        SermonUrl sermonUrl = new SermonUrl(context, calendar, new SermonUrl.SermonUrlListener() {
            @Override
            public void urlFound(String url) {
                if(url == null) {
                    MainActivity.toast(context, R.string.download_error, Toast.LENGTH_LONG);
                    if(listener != null)
                        listener.downloaded(null);
                } else {
                    //set Path
                    String folder = "audio";
                    String fileName = context.getString(R.string.app_name) + "_" + Losung.getDatumLongFromTime(calendar.getTimeInMillis()) + ".mp3";

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
                            dbHandler.addAudioLosungen(calendar.getTimeInMillis(), absolutePath);

                            if(listener != null)
                                listener.downloaded(absolutePath);
                        }
                    };
                    downloadTask.onFinishedListener(finished);

                    //Start download with notification
                    downloadTask.execute();
                }
            }
        });
        sermonUrl.load();
    }

    public interface NetworkListener {
        void downloaded(String path);
    }
}
