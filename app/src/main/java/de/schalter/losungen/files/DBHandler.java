package de.schalter.losungen.files;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.schalter.losungen.Losung;
import de.schalter.losungen.R;
import de.schalter.losungen.log.CustomLog;

/**
 * Created by marti on 27.10.2015.
 */
public class DBHandler extends SQLiteOpenHelper {

    private Context context;
    private static DBHandler dbHandler;

    private static final String DATABASE_NAME = "losungen";
    private static final int DATABASE_VERSION = 3;

    private static final String TABLE_LOSUNGEN = "losungen";
    private static final String TABLE_MONTH = "monthly";
    private static final String KEY_ID = "id";
    private static final String KEY_MONTH_TITLE = "monthtitle";
    private static final String KEY_LOSUNGSTEXT = "losungstext";
    private static final String KEY_LOSUNGSVERS = "losungsvers";
    private static final String KEY_LEHRTEXT = "lehrtext";
    private static final String KEY_LEHRTEXTVERS = "lehrtextvers";
    private static final String KEY_SONNTAGNAME = "sonntagname";
    private static final String KEY_DATUM = "datum"; //at 12 am
    private static final String KEY_MARKIERT = "markiert";
    private static final String KEY_NOTIZENLOSUNG = "notizenlosung";
    private static final String KEY_NOTIZENLEHRTEXT = "notizenlehrtext";
    private static final String KEY_AUDIOLOSUNG = "audiolosung";
    private static final String KEY_AUDIOLEHRTEXT = "audiolehrtext"; //Not used

    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    public static DBHandler newInstance(Context context) {
        if(dbHandler == null)
            dbHandler = new DBHandler(context);

        return dbHandler;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createDB = "create table if not exists " + TABLE_LOSUNGEN + " ( " +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_LOSUNGSTEXT + " TEXT, " +
                KEY_LOSUNGSVERS + " TEXT, " +
                KEY_LEHRTEXT + " TEXT, " +
                KEY_LEHRTEXTVERS + " TEXT, " +
                KEY_SONNTAGNAME + " TEXT, " +
                KEY_DATUM + " INTEGER, " +
                KEY_MARKIERT + " INTEGER," +
                KEY_NOTIZENLOSUNG + " TEXT, " +
                KEY_NOTIZENLEHRTEXT + " TEXT, " +
                KEY_AUDIOLOSUNG + " TEXT, " +
                KEY_AUDIOLEHRTEXT + "TEXT);";

        db.execSQL(createDB);

        CustomLog.writeToLog(context, new CustomLog(CustomLog.DEBUG, CustomLog.TAG_DB, createDB));

        createDB = "create table if not exists " + TABLE_MONTH + " ( " +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_LOSUNGSTEXT + " TEXT, " +
                KEY_LOSUNGSVERS + " TEXT, " +
                KEY_DATUM + " INTEGER, " +
                KEY_MARKIERT + " INTEGER, " +
                KEY_NOTIZENLOSUNG + " TEXT, " +
                KEY_AUDIOLOSUNG + " TEXT, " +
                KEY_MONTH_TITLE + " INTEGER);";

        db.execSQL(createDB);

        CustomLog.writeToLog(context, new CustomLog(CustomLog.DEBUG, CustomLog.TAG_DB, createDB));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion == 1) {
            String sql = "ALTER TABLE " + TABLE_LOSUNGEN + " ADD COLUMN " +
                    KEY_AUDIOLOSUNG + " TEXT";
            db.execSQL(sql);

            sql = "ALTER TABLE " + TABLE_LOSUNGEN + " ADD COLUMN " +
                    KEY_AUDIOLEHRTEXT + " TEXT";
            db.execSQL(sql);

            String createDB = "create table if not exists " + TABLE_MONTH + " ( " +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KEY_LOSUNGSTEXT + " TEXT, " +
                    KEY_LOSUNGSVERS + " TEXT, " +
                    KEY_DATUM + " INTEGER, " +
                    KEY_MARKIERT + " INTEGER, " +
                    KEY_NOTIZENLOSUNG + " TEXT, " +
                    KEY_AUDIOLOSUNG + " TEXT, " +
                    KEY_MONTH_TITLE + " INTEGER);";

            db.execSQL(createDB);

            CustomLog.writeToLog(context, new CustomLog(CustomLog.DEBUG, CustomLog.TAG_DB, createDB));
        } else if (oldVersion == 2) {
            String createDB = "create table if not exists " + TABLE_MONTH + " ( " +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KEY_LOSUNGSTEXT + " TEXT, " +
                    KEY_LOSUNGSVERS + " TEXT, " +
                    KEY_DATUM + " INTEGER, " +
                    KEY_MARKIERT + " INTEGER, " +
                    KEY_NOTIZENLOSUNG + " TEXT, " +
                    KEY_AUDIOLOSUNG + " TEXT, " +
                    KEY_MONTH_TITLE + " INTEGER);";

            db.execSQL(createDB);

            CustomLog.writeToLog(context, new CustomLog(CustomLog.DEBUG, CustomLog.TAG_DB, createDB));
        }
    }

    //Add new Monthly word
    public void addMonthlyWord(String losungstext, String losungsvers,
                               long datum) {
        //Date always 12th of month 12h
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(datum);
        calendar.set(Calendar.DAY_OF_MONTH, 12);
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        datum = calendar.getTimeInMillis();

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(KEY_LOSUNGSTEXT, losungstext);
        cv.put(KEY_LOSUNGSVERS, losungsvers);
        cv.put(KEY_DATUM, datum);
        cv.put(KEY_MARKIERT, 0);
        cv.put(KEY_NOTIZENLOSUNG, "");
        cv.put(KEY_MONTH_TITLE, 1);

        db.insert(TABLE_MONTH, null, cv);
    }

    //Add new Weekly word
    public void addWeeklyWord(String losungstext, String losungsvers,
                              long datum) {
        //Date always 11:00
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(datum);
        calendar.set(Calendar.HOUR_OF_DAY, 11);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        datum = calendar.getTimeInMillis();

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(KEY_LOSUNGSTEXT, losungstext);
        cv.put(KEY_LOSUNGSVERS, losungsvers);
        cv.put(KEY_DATUM, datum);
        cv.put(KEY_MARKIERT, 0);
        cv.put(KEY_NOTIZENLOSUNG, "");
        cv.put(KEY_MONTH_TITLE, 0);

        db.insert(TABLE_MONTH, null, cv);
    }

    /**
     *
     * @param datum can be any date of this month
     * @return Losung for this month
     */
    public Losung getMonthlyWord(long datum) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(datum);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        datum = calendar.getTimeInMillis();

        SQLiteDatabase db = this.getReadableDatabase();

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));

        String select = "Select * from " + TABLE_MONTH +
                " where( " + KEY_DATUM + " >= " + datum + " AND " + KEY_DATUM + " <= " + calendar.getTimeInMillis() + " " +
                "AND " + KEY_MONTH_TITLE + " = 1);";

        Cursor c = db.rawQuery(select, null);

        if(c.moveToFirst()) {
            Losung losung = new Losung();
            losung.setLosungstext(c.getString(1));
            losung.setLosungsvers(c.getString(2));
            losung.setLehrtext("");
            losung.setLehrtextVers("");
            losung.setSonntagsname("");
            losung.setDatum(c.getLong(3));
            losung.setMarkiert(c.getInt(4) == 1);
            losung.setNotizenLosung(c.getString(5));
            losung.setNotizenLehrtext("");

            c.close();
            return losung;
        }

        c.close();
        //If their was no entry with a hint that this date is not available
        Losung losung = new Losung();
        losung.setLosungstext(context.getString(R.string.no_date));
        losung.setLosungsvers(context.getString(R.string.no_date));
        losung.setLehrtext(context.getString(R.string.no_date));
        losung.setLehrtextVers(context.getString(R.string.no_date));
        losung.setSonntagsname(context.getString(R.string.no_date));
        losung.setDatum(datum);
        losung.setMarkiert(false);
        losung.setNotizenLosung("");
        losung.setNotizenLehrtext("");
        return losung;
    }

    /**
     *
     * @param datum can be any date of this week
     * @return
     */
    public Losung getWeeklyWord(long datum) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(datum);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        datum = calendar.getTimeInMillis();

        SQLiteDatabase db = this.getReadableDatabase();

        String select = "Select * from " + TABLE_MONTH +
                " where( " + KEY_DATUM + " >= " + datum + " AND " + KEY_DATUM + " <= " + (datum + 1000 * 60 * 60 * 24 * 7) + " " +
                "AND " + KEY_MONTH_TITLE + " = 0);";

        Cursor c = db.rawQuery(select, null);

        if(c.moveToFirst()) {
            Losung losung = new Losung();
            losung.setLosungstext(c.getString(1));
            losung.setLosungsvers(c.getString(2)
            );
            losung.setLehrtext("");
            losung.setLehrtextVers("");
            losung.setSonntagsname("");
            losung.setDatum(c.getLong(3));
            losung.setMarkiert(c.getInt(4) == 1);
            losung.setNotizenLosung(c.getString(5));
            losung.setNotizenLehrtext("");

            c.close();
            return losung;
        }

        c.close();
        //If their was no entry with a hint that this date is not available
        Losung losung = new Losung();
        losung.setLosungstext(context.getString(R.string.no_date));
        losung.setLosungsvers(context.getString(R.string.no_date));
        losung.setLehrtext(context.getString(R.string.no_date));
        losung.setLehrtextVers(context.getString(R.string.no_date));
        losung.setSonntagsname(context.getString(R.string.no_date));
        losung.setDatum(datum);
        losung.setMarkiert(false);
        losung.setNotizenLosung("");
        losung.setNotizenLehrtext("");
        return losung;
    }

    //Add new daily word
    public void addNew(String losungstext, String losungsvers,
                       String lehrtext, String lehrtextvers,
                       String sonntagname, long datum) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(KEY_LOSUNGSTEXT, losungstext);
        cv.put(KEY_LOSUNGSVERS, losungsvers);
        cv.put(KEY_LEHRTEXT, lehrtext);
        cv.put(KEY_LEHRTEXTVERS, lehrtextvers);
        cv.put(KEY_SONNTAGNAME, sonntagname);
        cv.put(KEY_DATUM, datum);
        cv.put(KEY_MARKIERT, 0);
        cv.put(KEY_NOTIZENLOSUNG, "");
        cv.put(KEY_NOTIZENLEHRTEXT, "");

        db.insert(TABLE_LOSUNGEN, null, cv);
    }

    //Add new daily word
    public void addNew(Losung losung) {
        this.addNew(losung.getLosungstext(), losung.getLosungsvers(),
                losung.getLehrtext(), losung.getLehrtextVers(),
                losung.getSonntagsname(), losung.getDatum());
    }

    //Update fields in Database to new Language
    //Keep notes and mark
    public void updateLanguage(Losung losung) {
        this.updateLanguage(losung.getLosungstext(), losung.getLosungsvers(),
                losung.getLehrtext(), losung.getLehrtextVers(),
                losung.getSonntagsname(), losung.getDatum());
    }

    //Update fields in Database to new Language
    //Keep notes and mark
    public void updateLanguage(String losungstext, String losungsvers,
                               String lehrtext, String lehrtextvers,
                               String sonntagname, long datum) {
        SQLiteDatabase db = this.getWritableDatabase();

        long count = DatabaseUtils.queryNumEntries(db,
                TABLE_LOSUNGEN, KEY_DATUM + " = ?", new String[]{String.valueOf(datum)});

        //Check if entry exists
        //If not: Insert instead of update
        if(count > 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(datum);
            calendar.set(Calendar.HOUR, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            long timeBefore = calendar.getTimeInMillis();

            calendar.add(Calendar.HOUR, 24);
            long timeAfter = calendar.getTimeInMillis();

            ContentValues cv = new ContentValues();

            cv.put(KEY_LOSUNGSTEXT, losungstext);
            cv.put(KEY_LOSUNGSVERS, losungsvers);
            cv.put(KEY_LEHRTEXT, lehrtext);
            cv.put(KEY_LEHRTEXTVERS, lehrtextvers);
            cv.put(KEY_SONNTAGNAME, sonntagname);

            db.update(TABLE_LOSUNGEN, cv, KEY_DATUM + " <= ? AND " + KEY_DATUM + " >= ?", new String[]{String.valueOf(timeAfter),
                    String.valueOf(timeBefore)});
        } else {
            addNew(losungstext, losungsvers,
                    lehrtext, lehrtextvers,
                    sonntagname, datum);
        }
    }

    public void updateLanguage(String losungstext, String losungsvers,
                               long datum, boolean monthlyTitle) {
        SQLiteDatabase db = this.getWritableDatabase();

        long orginialDatum = datum;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(datum);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        if(monthlyTitle)
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        else
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);

        long timeBegin = calendar.getTimeInMillis();
        long timeEnd = timeBegin;

        if(monthlyTitle) {
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            timeEnd = calendar.getTimeInMillis();
        } else {
            timeEnd += 1000 * 60 * 60 * 24 * 7;
        }

        String monthTitleString = "0";
        if(monthlyTitle) monthTitleString = "1";

        long count = DatabaseUtils.queryNumEntries(db,
                TABLE_MONTH, KEY_DATUM + " >= ? AND " + KEY_DATUM + " <= ? AND " + KEY_MONTH_TITLE + " = ?",
                new String[]{String.valueOf(timeBegin), String.valueOf(timeEnd), monthTitleString});

        //Check if entry exists
        //If not: Insert instead of update
        if(count > 0) {
            ContentValues cv = new ContentValues();

            cv.put(KEY_LOSUNGSTEXT, losungstext);
            cv.put(KEY_LOSUNGSVERS, losungsvers);

            db.update(TABLE_MONTH, cv, KEY_DATUM + " >= ? AND " + KEY_DATUM + " <= ? AND " + KEY_MONTH_TITLE + " = ?",
                    new String[]{String.valueOf(timeBegin),
                    String.valueOf(timeEnd), monthTitleString});
        } else {
            if(monthlyTitle) {
                addMonthlyWord(losungstext, losungsvers,
                        datum);
            } else {
                addWeeklyWord(losungstext, losungsvers,
                        datum);
            }
        }
    }

    //Returns a daily word for a special date (day)
    /**
     *
     * @param datum date is the first second of the day
     * @return Losung, the daily word for this day
     */
    public Losung getLosung(long datum) {
        SQLiteDatabase db = this.getReadableDatabase();

        String select = "Select * from " + TABLE_LOSUNGEN +
                " where( " + KEY_DATUM + " <= " + datum + " AND " + KEY_DATUM + " >= " + (datum - 1000 * 60 * 60 *24) + " );";

        Cursor c = db.rawQuery(select, null);

        if(c.moveToFirst()) {
            Losung losung = new Losung();
            losung.setLosungstext(c.getString(1));
            losung.setLosungsvers(c.getString(2));
            losung.setLehrtext(c.getString(3));
            losung.setLehrtextVers(c.getString(4));
            losung.setSonntagsname(c.getString(5));
            losung.setDatum(c.getLong(6));
            losung.setMarkiert(c.getInt(7) == 1);
            losung.setNotizenLosung(c.getString(8));
            losung.setNotizenLehrtext(c.getString(9));

            c.close();
            return losung;
        }

        c.close();
        //If their was no entry with a hint that this date is not available
        Losung losung = new Losung();
        losung.setLosungstext(context.getString(R.string.no_date));
        losung.setLosungsvers(context.getString(R.string.no_date));
        losung.setLehrtext(context.getString(R.string.no_date));
        losung.setLehrtextVers(context.getString(R.string.no_date));
        losung.setSonntagsname(context.getString(R.string.no_date));
        losung.setDatum(datum);
        losung.setMarkiert(false);
        losung.setNotizenLosung("");
        losung.setNotizenLehrtext("");
        return losung;
    }

    /**
     * returns all makred daily words
     * @return a list of all marked daily words
     */
    public List<Losung> getFavoriten() {
        List<Losung> losungen = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        String select = "Select * from " + TABLE_LOSUNGEN +
                " where( " + KEY_MARKIERT + " = 1 );";

        Cursor c = db.rawQuery(select, null);

        while(c.moveToNext()) {
            Losung losung = new Losung();
            losung.setLosungstext(c.getString(1));
            losung.setLosungsvers(c.getString(2));
            losung.setLehrtext(c.getString(3));
            losung.setLehrtextVers(c.getString(4));
            losung.setSonntagsname(c.getString(5));
            losung.setDatum(c.getLong(6));
            losung.setMarkiert(c.getInt(7) == 1);
            losung.setNotizenLosung(c.getString(8));
            losung.setNotizenLehrtext(c.getString(9));

            losung.setTitleLosung(context.getResources().getString(R.string.losung_from) + " " + Losung.getFullDatumFromTime(losung.getDatum()));
            losung.setTitleLehrtext(context.getResources().getString(R.string.lehrtext));

            losungen.add(losung);
        }

        c.close();
        return losungen;
    }

    /**
     * search with some parameters in the files for daily words
     * @param text the entry of the search text-box
     * @param losungen search in the verses (only the text) for new testament
     * @param lehrtexte search in the verses (only the text) for old testament
     * @param losungsVerse search in the verses (not the text, only name of vers, e.g. Matthew 1,1) for new testament
     * @param lehrtextVerse search in the verses (not the text, only name of vers e.g. 1. Mose 2,1) for old testament
     * @param notizen search in the notes
     * @param nurMarkierte only marked days
     * @param years a list of all selected years
     * @return a list of all relevant daily words
     */
    public List<Losung> suchen(String text, boolean losungen,
                               boolean lehrtexte, boolean losungsVerse,
                               boolean lehrtextVerse, boolean notizen,
                               boolean nurMarkierte, List<Integer> years) {

        //The given phrase can be at the beginning, middle and end of the text
        text = "'%" + text + "%'";

        String where = "(";

        String losungenSelect = "";
        String losungsVerseSelect = "";
        String lehrtexteSelect = "";
        String lehrtextVerseSelect = "";
        String notizenSelect = "";
        String nurMarkierteSelect = "";

        if(losungen) {
            losungenSelect = KEY_LOSUNGSTEXT + " LIKE " + text + "";
            where += losungenSelect;
        }

        if(lehrtexte) {
            lehrtexteSelect = KEY_LEHRTEXT + " LIKE " + text + "";

            //if there is not a where statement before we do not need to add "or"
            if(where.length() > 3)
                where += " OR ";

            where += lehrtexteSelect;
        }

        if(losungsVerse) {
            losungsVerseSelect = KEY_LOSUNGSVERS + " LIKE " + text + "";

            //if there is not a where statement before we do not need to add "or"
            if(where.length() > 3)
                where += " OR ";

            where += losungsVerseSelect;
        }

        if(lehrtextVerse) {
            lehrtextVerseSelect = KEY_LEHRTEXTVERS + " LIKE " + text + "";

            //if there is not a where statement before we do not need to add "or"
            if(where.length() > 3)
                where += " OR ";

            where += lehrtextVerseSelect;
        }

        if(notizen) {
            notizenSelect = KEY_NOTIZENLOSUNG + " LIKE " + text + "";

            //if there is not a where statement before we do not need to add "or"
            if(where.length() > 3)
                where += " OR ";

            where += notizenSelect;
        }

        // --- YEARS ---
        Calendar calendar = Calendar.getInstance();

        // "WHERE (search...) AND ( (firstYearStatement) OR (second...) OR ... )"

        where += ") AND (";

        for(int i = 0; i < years.size(); i++) {

            //For the first where statement I do not need a "or"
            if(i != 0)
                where += " OR ";

            String addWhere = "( " +  KEY_DATUM + " >= ";

            //Set the date to first january 00:00
            calendar.set(Calendar.YEAR, years.get(i));
            calendar.set(Calendar.DAY_OF_YEAR, 0);
            calendar.set(Calendar.MONTH, 0);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            //All entrys between first january this year and next year
            addWhere += calendar.getTimeInMillis() + " AND " + KEY_DATUM + " < ";
            calendar.set(Calendar.YEAR, years.get(i) + 1);

            addWhere += calendar.getTimeInMillis() + " )";

            where += addWhere;
        }
        where += ")";
        // --- YEARS END ---

        //"WHERE (search...) AND (years...) AND (marked...)"
        if(nurMarkierte) {
            nurMarkierteSelect = "(" + KEY_MARKIERT + " = 1";

            where += " AND ";

            where += nurMarkierteSelect + ") ";
        }

        List<Losung> losungenList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        String select = "Select * from " + TABLE_LOSUNGEN +
                " where( " + where + " );";

        CustomLog.writeToLog(context, new CustomLog(CustomLog.DEBUG, CustomLog.TAG_DB, select));

        Cursor c = db.rawQuery(select, null);

        while(c.moveToNext()) {
            Losung losung = new Losung();
            losung.setLosungstext(c.getString(1));
            losung.setLosungsvers(c.getString(2));
            losung.setLehrtext(c.getString(3));
            losung.setLehrtextVers(c.getString(4));
            losung.setSonntagsname(c.getString(5));
            losung.setDatum(c.getLong(6));
            losung.setMarkiert(c.getInt(7) == 1);
            losung.setNotizenLosung(c.getString(8));
            losung.setNotizenLehrtext(c.getString(9));

            losungenList.add(losung);
        }

        c.close();
        return losungenList;
    }

    /**
     * Edit notes of one daily word
     * @param datum defines which daily word
     * @param notiz overrides all earlier notes
     */
    public void editLosungNotiz(long datum, String notiz) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(KEY_NOTIZENLOSUNG, notiz);
        db.update(TABLE_LOSUNGEN, cv, KEY_DATUM + " = " + datum, null);

        CustomLog.writeToLog(context, new CustomLog(CustomLog.DEBUG, CustomLog.TAG_DB, "Date: " + datum + ", note: " + notiz + ", cv: " + cv.toString()));
    }

    //Is not in use, because LehrtextNotizen is not used until now
    public void editLehrtextNotiz(long datum, String notiz) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(KEY_NOTIZENLEHRTEXT, notiz);
        db.update(TABLE_LOSUNGEN, cv, KEY_DATUM + " = " + datum, null);
    }

    /**
     * mark one daily word
     * @param datum defines which daily word should be marked
     */
    public void setMarkiert(long datum) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(KEY_MARKIERT, 1);
        db.update(TABLE_LOSUNGEN, cv, KEY_DATUM + " = " + datum, null);
    }

    /**
     * unmark one daily word
     * @param datum defines which daily word should be unmarked
     */
    public void removeMarkiert(long datum) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(KEY_MARKIERT, 0);
        db.update(TABLE_LOSUNGEN, cv, KEY_DATUM + " = " + datum, null);
    }

    public void addAudioLosungen(long datum, String path) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(KEY_AUDIOLOSUNG, path);
        db.update(TABLE_LOSUNGEN, cv, KEY_DATUM + " = " + datum, null);
    }

    public String getAudioLosungen(long datum) {
        SQLiteDatabase db = this.getReadableDatabase();

        String select = "Select " + KEY_AUDIOLOSUNG + " from " + TABLE_LOSUNGEN +
                " where( " + KEY_DATUM + " = " + datum + " );";

        Cursor c = db.rawQuery(select, null);

        if (c.moveToNext()) {
            String audio = c.getString(0);
            c.close();
            return audio;
        }

        c.close();
        return null;
    }

    public List<Long> getAllAudios() {
        SQLiteDatabase db = this.getReadableDatabase();

        String select = "SELECT " + KEY_DATUM + "," + KEY_AUDIOLOSUNG + " from " + TABLE_LOSUNGEN +
                " where( " + KEY_AUDIOLOSUNG + " IS NOT NULL);";

        Cursor c = db.rawQuery(select, null);

        List<Long> dates = new ArrayList<>();
        List<String> datesUrl = new ArrayList<>();
        while(c.moveToNext()) {
            dates.add(c.getLong(0));
            datesUrl.add(c.getString(1));
        }

        c.close();

        //Check if they are available
        for(int i = 0; i < datesUrl.size(); i++) {
            File file = new File(datesUrl.get(i));
            if(!file.exists()) {
                dates.remove(i);
                datesUrl.remove(i);
                i--;
            }
        }

        return dates;
    }

    public void setAudioNull(long datum) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(datum);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);

        long timeOneDay = 1000l * 60l * 60l * 24l;

        String update = "UPDATE " + TABLE_LOSUNGEN + " set " + KEY_AUDIOLOSUNG +
                " = NULL where( " + KEY_DATUM + " > " + calendar.getTimeInMillis() + " AND " +
                KEY_DATUM + " < " + (calendar.getTimeInMillis() + timeOneDay) + ");";

        SQLiteDatabase db = this.getWritableDatabase();

        db.rawQuery(update, null);
    }

    public ArrayList<String[]> getAllNotes() {
        SQLiteDatabase db = this.getReadableDatabase();

        String select = "SELECT " + KEY_DATUM + "," + KEY_NOTIZENLOSUNG + " from " +
                TABLE_LOSUNGEN + " WHERE (" + KEY_NOTIZENLOSUNG + " IS NOT NULL)" +
                " ORDER BY " + KEY_DATUM + ";";

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

    /**
     * import the notes to the database
     * @param notes
     * [date, note]
     */
    public void importNotes(ArrayList<String[]> notes, boolean override) {
        Calendar calendar = Calendar.getInstance();
        long timeOneDay = 1000l * 60l * 60l * 24l;

        SQLiteDatabase db = this.getWritableDatabase();

        for(int i = 0; i< notes.size(); i++) {
            ContentValues cv = new ContentValues();
            String[] array = notes.get(i);

            calendar.setTimeInMillis(Long.parseLong(array[0]));
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.HOUR_OF_DAY, 0);

            //If notes are not empty
            if(!array[1].equals("") || override) {
                String where = KEY_DATUM + " >= " +
                        calendar.getTimeInMillis() + " AND " + KEY_DATUM +
                        " <= " + (calendar.getTimeInMillis() + timeOneDay);

                cv.put(KEY_NOTIZENLOSUNG, array[1]);
                db.update(TABLE_LOSUNGEN, cv, where, null);

                Log.d("Losungen", "SQL: " + where);
            }
        }
    }
}
