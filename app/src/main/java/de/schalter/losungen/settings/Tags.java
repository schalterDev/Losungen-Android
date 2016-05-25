package de.schalter.losungen.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by marti on 30.10.2015.
 */
public class Tags {

    //Für Losungen, Wochen und Monatssprüche
    private static final String[] IMPORT_LIST_DE = {"2015", "2016"};
    private static final String[] IMPORT_LIST_EN = {"2015", "2016"};
    private static final String[] IMPORT_LIST_ES = {"2015", "2016"};
    private static final String[] IMPORT_LIST_AR = {};
    private static final String[] IMPORT_LIST_FR = {"2015", "2016"};
    private static final String[] IMPORT_LIST_IT = {};
    private static final String[] IMPORT_LIST_NL = {"2016"};
    private static final String[] IMPORT_LIST_DA = {};
    private static final String[] IMPORT_LIST_PT = {};
    private static final String[] IMPORT_LIST_NO = {};
    private static final String[] IMPORT_LIST_SV = {};
    private static final String[] IMPORT_LIST_PL = {};
    private static final String[] IMPORT_LIST_HR = {};
    private static final String[] IMPORT_LIST_HU = {};
    private static final String[] IMPORT_LIST_RO = {};
    private static final String[] IMPORT_LIST_BE = {};
    private static final String[] IMPORT_LIST_BG = {};

    public static final String[] SUPPORTED_LANGUAGES = {"en", "de", "nl"}; //Das gilt für die Strings.xml
    public static final String[] CHANGELOG_LANGUAGES = {"en", "de"};
    public static final String[] ANDACHT_LANGUAGES = {"de"};

    public static final String TAG_LASTSTART = "laststart";
    public static final String TAG_LASTNOTIFICATION = "last_notification";

    //PREF - AUDIO
    public static final String PREF_AUDIO_DOWNLOAD = "audio_download";
    public static final String PREF_AUDIO_AUTODOWNLOAD = "audio_autodownload";
    public static final String PREF_AUDIO_AUTODOWNLOAD_NETWORK = "audio_autodownload_network";
    public static final String PREF_AUDIO_EXTERNAL_STORGAE = "audio_external_storage";
    public static final String PREF_AUDIO_DELETE_DAYS = "audio_delete_days";

    public static final String PREF_NOTIFICATION = "notifications_losung";
    public static final String PREF_NOTIFICATIONART = "notification_art";
    public static final String PREF_NOTIFICATIONTIME = "notification_time";
    public static final String PREF_LANGUAGE = "language";

    public static final String PREF_GOOGLEANALYTICS = "google_analytics";
    public static final String PREF_SHOWNOTES = "show_notes";

    public static final int LOSUNG_NOTIFICATION = 0;
    public static final int LEHRTEXT_NOTIFICATION = 1;
    public static final int LOSUNG_UND_LEHRTEXT_NOTIFICATION = 2;
    public static final String PREF_ADS = "show_ads";

    public static final String PREF_VERSIONCODE = "versioncode";
    public static final String PREF_IMPORTS = "imports";
    //For the daily words
    public static final String SELECTED_LANGUAGE = "selected_language";

    //CUSTOMISE
    public static final String WHICH_VERS_TO_SHOW = "which_vers_to_show";

    //SHARING
    public static final String OPEN_WITH_APP = "open_with_external_app";
    public static final String OPEN_WITH_DEFAULT = "open_verses_default";

    public static String[] getImport(String language) {
        switch (language) {
            case "de":
                return Tags.IMPORT_LIST_DE.clone();
            case "en":
                return Tags.IMPORT_LIST_EN.clone();
            case "es":
                return Tags.IMPORT_LIST_ES.clone();
            case "ar":
                return Tags.IMPORT_LIST_AR.clone();
            case "fr":
                return Tags.IMPORT_LIST_FR.clone();
            case "it":
                return Tags.IMPORT_LIST_IT.clone();
            case "nl":
                return Tags.IMPORT_LIST_NL.clone();
            case "da":
                return Tags.IMPORT_LIST_DA.clone();
            case "pt":
                return Tags.IMPORT_LIST_PT.clone();
            case "no":
                return Tags.IMPORT_LIST_NO.clone(); //Nur NT
            case "sv":
                return Tags.IMPORT_LIST_SV.clone();
            case "pl":
                return Tags.IMPORT_LIST_PL.clone(); //Nur NT
            case "hr":
                return Tags.IMPORT_LIST_HR.clone(); //Nur NT
            case "hu":
                return Tags.IMPORT_LIST_HU.clone(); //Nur NT (KAR AT)
            case "ro":
                return Tags.IMPORT_LIST_RO.clone();
            case "be":
                return Tags.IMPORT_LIST_BE.clone();
            case "bg":
                return Tags.IMPORT_LIST_BG.clone();
        }

        return Tags.IMPORT_LIST_EN;
    }

    public static String getUebersetzung(String language) {
        switch (language) {
            case "de":
                return "LUT";
            case "en":
                return "ESV";
            case "es":
                return "BTX";
            case "ar":
                return "ALAB";
            case "fr":
                return "BDS";
            case "it":
                return "ITA";
            case "nl":
                return "HTB";
            case "da":
                return "DK";
            case "pt":
                return "PRT";
            case "no":
                return "NOR"; //Nur NT
            case "sv":
                return "BSV";
            case "pl":
                return "PSZ"; //Nur NT
            case "hr":
                return "CKK"; //Nur NT
            case "hu":
                return "HUN"; //Nur NT (KAR AT)
            case "ro":
                return "NTR";
            case "be":
                return "RSZ";
            case "bg":
                return "BLG";
        }

        return "ESV";
    }

    private static final String website_anfang = "http://www.erf.de/radio/erf-plus/mediathek/wort-zum-tag/73-";
    private static final int website_01_01_2015 = 4073;

    private static final String audio_anfang = "";
    private static final String audio_url_prefix = "http://ww.erf.de";
    private static final String audio_url_html_anfang = "class=\"erf-audio\" data-file=\"";
    private static final String audio_url_html_ende = "\"";

    public static String getLanguage(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

        String language = settings.getString(Tags.PREF_LANGUAGE, "---");
        if(language.equals("---")) {
            language = Locale.getDefault().getLanguage();
        }

        return language;
    }

    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if(mWifi != null)
            return mWifi.isConnected();

        return false;
    }

    public static boolean isMobileConnected(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mMobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if(mMobile != null)
            return mMobile.isConnected();

        return false;
    }

    public static String getAudioUrl(Calendar calendar) throws IOException {
        String websiteUrl = getWebsiteUrl(calendar);
        String html = downloadHtml(websiteUrl);

        int indexAnfang = html.indexOf(audio_url_html_anfang) + audio_url_html_anfang.length();
        int indexEnde = html.indexOf(audio_url_html_ende, indexAnfang);

        String url = audio_url_prefix + html.substring(indexAnfang, indexEnde);
        return url;
    }

    private static String downloadHtml(String urlString) throws IOException {
        URL url = new URL(urlString);

        long startTime = System.currentTimeMillis();
        URLConnection ucon = url.openConnection();

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

    public static String getWebsiteUrl(Calendar calendar) {
        Calendar calJanuar = Calendar.getInstance();
        calJanuar.set(Calendar.MONTH, Calendar.JANUARY);
        calJanuar.set(Calendar.YEAR, 2015);
        calJanuar.set(Calendar.DAY_OF_MONTH, 1);
        calJanuar.set(Calendar.HOUR, 0);
        calJanuar.set(Calendar.MINUTE, 0);
        long calJanuarTime = calJanuar.getTimeInMillis();
        Log.d("Losungen", "CalJanuarTime: " + calJanuarTime);

        long timeNow = calendar.getTimeInMillis();
        long diff = timeNow - calJanuarTime;
        int days = (int) (diff / (1000 * 60 * 60 * 24)) + 1;

        return website_anfang + (website_01_01_2015 + days);
    }
}
