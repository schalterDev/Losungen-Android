package de.schalter.losungen.log;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.nononsenseapps.filepicker.FilePickerActivity;

import java.io.File;
import java.util.ArrayList;

import de.schalter.losungen.Losung;
import de.schalter.losungen.MainActivity;
import de.schalter.losungen.R;
import de.schalter.losungen.files.XmlWriter;

/**
 * Created by martin on 07.10.16.
 */

public class CustomLog {

    private static final int FILE_CODE = 65;

    private String tag;
    private String content;
    private long date;
    private int level;

    public static final int VERBOSE = 0;
    public static final int DEBUG = 1;
    public static final int WARN = 2;
    public static final int ERROR = 3;

    public static boolean activated = true;

    public static final String TAG_DB = "dbHandler";
    public static final String TAG_NOTIFICATION = "notfication";

    private static LogDBHandler dbHandler;

    public static void writeToLog(Context context, CustomLog log) {
        Log.d(log.getTag(), log.getContent());

        if(activated) {
            if (dbHandler == null)
                dbHandler = LogDBHandler.newInstance(context);

            dbHandler.addLogEntry(log.getDate(), log.getTag(), log.getLevel(), log.getContent());
        }
    }

    public static void setPreference(Context context, String PREF_TAG, boolean defaultValue) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        activated = settings.getBoolean(PREF_TAG, defaultValue);
    }

    //Sets date to now
    public CustomLog(int level, String tag, String content) {
        date = System.currentTimeMillis();
        this.level = level;
        this.tag = tag;
        this.content = content;
    }

    public CustomLog(long date, int level, String tag, String content) {
        this.date = date;
        this.level = level;
        this.tag = tag;
        this.content = content;
    }

    public String getTag() {
        return tag;
    }

    public String getContent() {
        return content;
    }

    public long getDate() {
        return date;
    }

    public int getLevel() {
        return level;
    }


    public static void exportLog(Activity activity) {
        // This always works
        Intent i = new Intent(activity, FilePickerActivity.class);
        // This works if you defined the intent filter
        // Intent i = new Intent(Intent.ACTION_GET_CONTENT);

        // Set these depending on your use case. These are the defaults.
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
        i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);

        // Configure initial directory by specifying a String.
        // You could specify a String like "/storage/emulated/0/", but that can
        // dangerous. Always use Android's API calls to get paths to the SD-card or
        // internal memory.
        i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());

        activity.startActivityForResult(i, MainActivity.LOG_CODE);
    }

    //Wird von MainActivity aufgerufen
    public static void exportLogToFile(final Context context, final Uri uri) {
        Thread export = new Thread(new Runnable() {
            @Override
            public void run() {
                LogDBHandler dbHandler = LogDBHandler.newInstance(context);
                ArrayList<CustomLog> map = dbHandler.getAllLogs();

                ArrayList<String> tags = new ArrayList<>();
                ArrayList<String> values = new ArrayList<>();

                tags.add(XmlWriter.STARTTAG);
                values.add("LosungenDebug");
                for (int i = 0; i < map.size(); i++) {
                    CustomLog log = map.get(i);

                    tags.add(XmlWriter.STARTTAG);
                    values.add("Datum");
                    tags.add(XmlWriter.TEXT);
                    values.add(Losung.getFullDateForXml(log.date));

                    tags.add(XmlWriter.STARTTAG);
                    values.add("Level");
                    tags.add(XmlWriter.TEXT);
                    values.add(String.valueOf(log.level));
                    tags.add(XmlWriter.ENDTAG);
                    values.add("Level");

                    tags.add(XmlWriter.STARTTAG);
                    values.add("Tag");
                    tags.add(XmlWriter.TEXT);
                    values.add(log.tag);
                    tags.add(XmlWriter.ENDTAG);
                    values.add("Tag");

                    tags.add(XmlWriter.STARTTAG);
                    values.add("Content");
                    tags.add(XmlWriter.TEXT);
                    values.add(log.content);
                    tags.add(XmlWriter.ENDTAG);
                    values.add("Content");

                    tags.add(XmlWriter.ENDTAG);
                    values.add("Datum");
                }
                tags.add(XmlWriter.ENDTAG);
                values.add("LosungenDebug");

                XmlWriter xmlWriter = new XmlWriter();
                xmlWriter.setData(tags, values);

                File dir = new File(uri.getPath());

                final File file = new File(dir, "log.xml");

                xmlWriter.writeXml(file);

                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.toast(context,
                                context.getResources().getString(R.string.exported_to) + file.getPath(),
                                Toast.LENGTH_LONG);
                    }
                });
            }
        });
        export.start();
    }

}
