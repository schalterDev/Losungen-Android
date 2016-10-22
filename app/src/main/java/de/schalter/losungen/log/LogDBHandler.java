package de.schalter.losungen.log;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import de.schalter.losungen.files.DBHandler;

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

    public void addLogEntry(long date, String tag, int level, String content) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(KEY_DATE, date);
        cv.put(KEY_TAG, tag);
        cv.put(KEY_LEVEL, level);
        cv.put(KEY_CONTENT, content);

        db.insert(TABLE_LOGGER, null, cv);
    }

    public ArrayList<String[]> getAllLogs() {
        SQLiteDatabase db = this.getReadableDatabase();

        String select = "SELECT " + KEY_DATE + "," + KEY_LEVEL + "," + KEY_TAG + "," + KEY_CONTENT + " from " +
                TABLE_LOGGER + " ORDER BY " + KEY_DATE + ";";

        Cursor c = db.rawQuery(select, null);

        ArrayList<String[]> values = new ArrayList<>();

        while(c.moveToNext()) {
            String[] array = {String.valueOf(c.getLong(0)), c.getString(1)};

            //Only add if it isnt empty
            if(!(array[0].equals("") & array[1].equals("")))
                values.add(array);
        }

        c.close();

        return values;
    }

}
