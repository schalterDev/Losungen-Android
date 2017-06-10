package de.schalter.losungen.rss;

import android.content.Context;
import android.widget.Toast;

import java.io.IOException;
import java.util.Calendar;

import de.schalter.losungen.MainActivity;
import de.schalter.losungen.R;
import de.schalter.losungen.network.Network;

/**
 * Created by martin on 09.06.17.
 */

public class SermonUrl {

    private static final String FEED_URL = "http://feeds.feedburner.com/erf/wzt";
    private static final String audio_url_prefix = "https://www.erf.de";
    private static final String audio_url_html_anfang = "data-file=\"";
    private static final String audio_url_html_ende = "\"";

    private Context context;
    private long time;
    private SermonUrlListener listener;

    public SermonUrl(Context context, long time, SermonUrlListener listener) {
        this.context = context;
        this.time = time;
        this.listener = listener;
    }

    public SermonUrl(Context context, Calendar calendar, SermonUrlListener listener) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        this.context = context;
        this.time = calendar.getTimeInMillis();
        this.listener = listener;
    }

    public void load() {
        MainActivity.toast(context, R.string.download_starting, Toast.LENGTH_SHORT);

        Rss rss = new Rss(context, FEED_URL, new Rss.FeedLoaded() {
            @Override
            public void onLoaded(final String websiteUrl) {
                Thread download = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(websiteUrl == null) {
                            MainActivity.toast(context, R.string.download_error_rss, Toast.LENGTH_LONG);
                        } else {

                            String html = null;
                            try {
                                html = Network.downloadHtml(websiteUrl);
                            } catch (IOException e) {
                                MainActivity.toast(context, R.string.download_error, Toast.LENGTH_LONG);
                            }

                            int indexAnfang = html.indexOf(audio_url_html_anfang) + audio_url_html_anfang.length();
                            int indexEnde = html.indexOf(audio_url_html_ende, indexAnfang);

                            String url = audio_url_prefix + html.substring(indexAnfang, indexEnde);

                            listener.urlFound(url);
                        }
                    }
                });
                download.start();
            }
        });

        rss.load(time);
    }

    public interface SermonUrlListener {
        void urlFound(String url);
    }

}
