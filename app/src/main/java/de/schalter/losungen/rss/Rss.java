package de.schalter.losungen.rss;

import android.content.Context;

import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndEntryImpl;
import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndFeed;
import com.google.code.rome.android.repackaged.com.sun.syndication.io.FeedException;
import com.google.code.rome.android.repackaged.com.sun.syndication.io.SyndFeedInput;
import com.google.code.rome.android.repackaged.com.sun.syndication.io.XmlReader;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;

/**
 * Created by martin on 09.06.17.
 */

public class Rss  {

    private FeedLoaded listener;
    private SyndFeed feed;

    private long time;

    public Rss(Context context, String url, FeedLoaded listener) {
        this.listener = listener;

        try {
            feed = new SyndFeedInput().build(new XmlReader(new URL(url)));

        } catch (FeedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load(long time) {
        this.time = time;
        boolean found = false;

        List<SyndEntryImpl> entries = feed.getEntries();
        for(SyndEntryImpl entry : entries) {
            Date date = entry.getPublishedDate();
            if(date.getTime() == time) {
                listener.onLoaded(entry.getUri());
                found = true;
                break;
            }
        }

        if(!found)
            listener.onLoaded(null);
    }

    interface FeedLoaded {
        /**
         * Will be called when the feed is loaded for the specific time
         * @param url download url for the website. Null if there was no url for
         *            this time
         */
        void onLoaded(String url);
    }
}
