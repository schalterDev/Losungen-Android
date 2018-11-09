package de.schalter.losungen.log;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by martin on 07.10.16.
 */

class LogDBHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "CustomLogger";

    private static final String TABLE_LOGGER = "logs";
    private static final String KEY_ID = "id";
    private static final String KEY_DATE = "date";
    private static final String KEY_TAG = "tag";
    private static final String KEY_LEVEL = "level";
    private static final String KEY_CONTENT = "content";

    private static LogDBHandler instance;

    private LogDBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static LogDBHandler newInstance(Context context) {
        if(instance == null)
            instance = new LogDBHandler(context);

        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createDB = "create table if not exists " + TABLE_LOGGER + " ( " +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_DATE + " INTEGER, " +
                KEY_TAG + " TEXT, " +
                KEY_LEVEL + " INTEGER, " +
                KEY_CONTENT + " TEXT);";

        db.execSQL(createDB);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    void addLogEntry(long date, String tag, int level, String content) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(KEY_DATE, date);
        cv.put(KEY_TAG, tag);
        cv.put(KEY_LEVEL, level);
        cv.put(KEY_CONTENT, content);

        Log.d("Losungen", "insert into db-logger: " + date + ", " + tag + ", " + level + ", " + content);

        db.insert(TABLE_LOGGER, null, cv);
    }

    ArrayList<CustomLog> getAllLogs() {
        SQLiteDatabase db = this.getReadableDatabase();

        String select = "SELECT " + KEY_DATE + "," + KEY_LEVEL + "," + KEY_TAG + "," + KEY_CONTENT + " from " +
                TABLE_LOGGER + " ORDER BY " + KEY_DATE + ";";

        Cursor c = db.rawQuery(select, null);

        ArrayList<CustomLog> values = new ArrayList<>();

        while(c.moveToNext()) {
            CustomLog log = new CustomLog(
                    c.getLong(0),
                    c.getInt(1),
                    c.getString(2),
                    c.getString(3)
            );

            values.add(log);
        }

        c.close();

        return values;
    }

}
