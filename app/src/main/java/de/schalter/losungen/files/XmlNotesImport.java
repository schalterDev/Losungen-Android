package de.schalter.losungen.files;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by martin on 29.05.16.
 */
public class XmlNotesImport {

    // We don't use namespaces
    private static final String ns = null;

    private ArrayList<String[]> notes;

    public XmlNotesImport() {

    }

    /**
     * Parse a XML-File on the internet with a url
     * @param url the URL to the xml-file
     */
    public boolean parseXML(String url) {
        try {
            parseXML(downloadUrl(url));
            return true;
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Parse XML-File
     * @param file the XML-File to parse
     */
    public boolean parseXML(File file) {
        try {
            FileInputStream in = new FileInputStream(file);
            parseXML(in);
            return true;
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Parse XML - File with InputStream
     * @param in InputStream with XML - File
     */
    public void parseXML(InputStream in) throws IOException, XmlPullParserException {
        notes = new ArrayList<>();

        List notizenXML = null;
        notizenXML = parse(in);

        if(notizenXML != null) {
            for(int i = 0; i < notizenXML.size(); i++) {
                Entry entry = (Entry) notizenXML.get(i);

                String[] array = {String.valueOf(getTimeByDatum(entry.date)), entry.note};
                notes.add(array);
            }
        }
    }

    private long getTimeByDatum(String datum) {

        datum = datum.replaceAll("T","");

        try {
            DateFormat df = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
            Date date = null;
            date = df.parse(datum);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return 0;
    }

    private List parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readFeed(parser);
        } finally {
            in.close();
        }
    }

    private List readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        List entries = new ArrayList();

        //parser.require(XmlPullParser.START_TAG, ns, "Losungen");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("Losungen")) {
                entries.add(readEntry(parser));
            } else {
                skip(parser);
            }
        }
        return entries;
    }

    public static class Entry {
        public final String date;
        public final String note;

        private Entry(String date, String note) {
            this.date = date;
            this.note = note;
        }
    }

    // Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them off
    // to their respective "read" methods for processing. Otherwise, skips the tag.
    private Entry readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "Losungen");
        String date = null;
        String note = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("Datum")) {
                date = readTitle(parser, "Datum");
            } else if (name.equals("Notizen")) {
                note = readTitle(parser, "Notizen");
            }  else {
                skip(parser);
            }
        }
        return new Entry(date, note);
    }

    // Processes title tags in the feed.
    private String readTitle(XmlPullParser parser, String title) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, title);
        String titleString = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, title);
        return titleString;
    }

    // For the tags title and summary, extracts their text values.
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    // Given a string representation of a URL, sets up a connection and gets
    // an input stream.
    private InputStream downloadUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Starts the query
        conn.connect();
        return conn.getInputStream();
    }

    public ArrayList<String[]> getNotes() {
        return notes;
    }
}
