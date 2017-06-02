package de.schalter.losungen.changelog;

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
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Smarti on 14.01.2016.
 */
public class XmlParserChangelog {

    // We don't use namespaces
    private static final String ns = null;

    private List<ChangelogElement> changes;

    public XmlParserChangelog() {

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
        changes = new ArrayList<>();

        List changelogXML = null;
        try {
            changelogXML = parse(in);
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        if(changelogXML != null) {
            for(int i = 0; i < changelogXML.size(); i++) {
                ChangelogElement changesElement = new ChangelogElement();
                Entry entry = (Entry) changelogXML.get(i);

                changesElement.appvesion = entry.version;
                changesElement.changes = entry.change;
                changesElement.important = entry.important;
                changesElement.appVesionNice = entry.appVersionNice;

                changes.add(changesElement);
            }
        }
    }

    public List<ChangelogElement> getChanges() {
        return changes;
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
            if (name.equals("appversion")) {
                entries.add(readEntry(parser));
            } else {
                skip(parser);
            }
        }
        return entries;
    }

    public static class Entry {
        public final int version;
        public final String appVersionNice;
        public final List<String> change;
        public final List<Boolean> important;

        private Entry(int version, String appVersionNice, List<String> change, List<Boolean> important) {
            this.version = version;
            this.appVersionNice = appVersionNice;
            this.change = change;
            this.important = important;
        }
    }

    // Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them off
    // to their respective "read" methods for processing. Otherwise, skips the tag.
    private Entry readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "appversion");
        String appVersionString = parser.getAttributeValue(null, "version");
        int appVersion = Integer.valueOf(appVersionString);
        String appVersionNice = parser.getAttributeValue(null, "versionNice");
        List<String> changesList = new ArrayList<>();
        List<Boolean> changesImportantList = new ArrayList<>();

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            switch (name) {
                case "change":
                    String importantValue = parser.getAttributeValue(null, "important");
                    changesList.add(readTitle(parser, "change"));
                    changesImportantList.add(Boolean.parseBoolean(importantValue));
                    break;
                default:
                    skip(parser);
                    break;
            }
        }
        return new Entry(appVersion, appVersionNice, changesList, changesImportantList);
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
