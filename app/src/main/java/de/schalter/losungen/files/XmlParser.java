package de.schalter.losungen.files;

import android.content.Context;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

import de.schalter.losungen.Losung;

/**
 * Created by marti on 28.10.2015.
 */
public class XmlParser {

    // We don't use namespaces
    private static final String ns = null;

    private List<Losung> losungen;

    public XmlParser() {

    }

    /**
     * Parse a XML-File on the internet with a url
     * @param url the URL to the xml-file
     */
    public void parseXML(String url) {
        try {
            parseXML(downloadUrl(url));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Parse XML-File
     * @param file the XML-File to parse
     */
    public void parseXML(File file) {
        try {
            FileInputStream in = new FileInputStream(file);
            parseXML(in);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Parse XML - File with InputStream
     * @param in InputStream with XML - File
     */
    public void parseXML(InputStream in) {
        losungen = new ArrayList<>();

        List losungenXML = null;
        try {
            losungenXML = parse(in);
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        if(losungenXML != null) {
            for(int i = 0; i < losungenXML.size(); i++) {
                Losung losung = new Losung();
                Entry entry = (Entry) losungenXML.get(i);

                losung.setLosungstext(entry.losungstext.replace("/", ""));
                losung.setLosungsvers(entry.losungsvers);
                losung.setLehrtext(entry.lehretext.replace("/", ""));
                losung.setLehrtextVers(entry.lehrtextvers);
                losung.setSundayName(entry.sonntagname);

                String datum = entry.datum;
                losung.setDate(getTimeByDatum(datum));
                losung.setMarked(false);
                losung.setNotesLehrtext("");
                losung.setNotesLosung("");

                losungen.add(losung);
            }
        }
    }

    public void writeIntoDatabase(Context context, boolean monat, boolean woche) {
        DBHandler dbHandler = DBHandler.newInstance(context);

        for(int i = 0; i < losungen.size(); i++) {
            if(monat) {
                dbHandler.addMonthlyWord(losungen.get(i).getLosungstext(),
                        losungen.get(i).getLosungsvers(),
                        losungen.get(i).getDate());
            } else if(woche) {
                dbHandler.addWeeklyWord(losungen.get(i).getLosungstext(),
                        losungen.get(i).getLosungsvers(),
                        losungen.get(i).getDate());
            } else {
                dbHandler.addNew(losungen.get(i));
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


    public void updateIntoDatabase(Context context, boolean monat, boolean woche) {
        DBHandler dbHandler = DBHandler.newInstance(context);

        if(monat || woche) {
            for (int i = 0; i < losungen.size(); i++) {
                Losung losung = losungen.get(i);
                dbHandler.updateLanguage(losung.getLosungstext(), losung.getLosungsvers(), losung.getDate(), monat);
            }
        } else {
            for (int i = 0; i < losungen.size(); i++) {
                dbHandler.updateLanguage(losungen.get(i));
            }
        }
    }

    public static class Entry {
        public final String losungstext;
        public final String losungsvers;
        public final String lehretext;
        public final String lehrtextvers;
        public final String datum;
        public final String sonntagname;

        private Entry(String losungstext, String losungsvers, String lehretext, String lehrtextvers, String datum, String sonntagname) {
            this.losungstext = losungstext;
            this.losungsvers = losungsvers;
            this.lehretext = lehretext;
            this.lehrtextvers = lehrtextvers;
            this.datum = datum;
            this.sonntagname = sonntagname;
        }
    }

    // Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them off
    // to their respective "read" methods for processing. Otherwise, skips the tag.
    private Entry readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "Losungen");
        String losungstext = null;
        String losungsvers = null;
        String lehrtext = null;
        String lehrtextvers = null;
        String datum = null;
        String sonntagname = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("Losungstext")) {
                losungstext = readTitle(parser, "Losungstext");
            } else if (name.equals("Losungsvers")) {
                losungsvers = readTitle(parser, "Losungsvers");
            } else if (name.equals("Lehrtext")) {
                lehrtext = readTitle(parser, "Lehrtext");
            } else if (name.equals("Lehrtextvers")) {
                lehrtextvers = readTitle(parser, "Lehrtextvers");
            } else if (name.equals("Sonntag")) {
                sonntagname = readTitle(parser, "Sonntag");
            } else if (name.equals("Datum")) {
                datum = readTitle(parser, "Datum");
            } else {
                skip(parser);
            }
        }
        return new Entry(losungstext, losungsvers, lehrtext, lehrtextvers, datum, sonntagname);
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

}
