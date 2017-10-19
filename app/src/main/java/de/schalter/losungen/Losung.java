package de.schalter.losungen;

import android.content.Context;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import de.schalter.losungen.dialogs.ShareDialog;
import de.schalter.losungen.settings.Tags;

/**
 * Created by marti on 27.10.2015.
 */
public class Losung {
    private String titleLosung = "";
    private String titleLehrtext = "";

    private String losungstext;
    private String losungsvers;
    private String lehrtext;
    private String lehrtextVers;
    private String sundayName;
    private long date;
    private boolean marked;
    private String notesLehrtext;
    private String notesLosung;

    private String url;

    public String getLosungstext() {
        return losungstext;
    }

    public void setLosungstext(String losungstext) {
        this.losungstext = losungstext;
    }

    public String getLosungsvers() {
        return losungsvers;
    }

    public void setLosungsvers(String losungsvers) {
        this.losungsvers = losungsvers;
    }

    public String getLehrtext() {
        return lehrtext;
    }

    public void setLehrtext(String lehrtext) {
        this.lehrtext = lehrtext;
    }

    public String getLehrtextVers() {
        return lehrtextVers;
    }

    public void setLehrtextVers(String lehrtextVers) {
        this.lehrtextVers = lehrtextVers;
    }

    public String getSundayName() {
        return sundayName;
    }

    public void setSundayName(String sonntagsname) {
        this.sundayName = sonntagsname;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public boolean isMarked() {
        return marked;
    }

    public void setMarked(boolean marked) {
        this.marked = marked;
    }

    public String getNotesLehrtext() {
        return notesLehrtext;
    }

    public void setNotesLehrtext(String notesLehrtext) {
        this.notesLehrtext = notesLehrtext;
    }

    public String getNotesLosung() {
        return notesLosung;
    }

    public void setNotesLosung(String notesLosung) {
        this.notesLosung = notesLosung;
    }

    public String getTitleLosung() {
        return titleLosung;
    }

    public void setTitleLosung(String titleLosung) {
        this.titleLosung = titleLosung;
    }

    public String getTitleLehrtext() {
        return titleLehrtext;
    }

    public void setTitleLehrtext(String titleLehrtext) {
        this.titleLehrtext = titleLehrtext;
    }

    public static String getDatumFromTime(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        Date date = calendar.getTime();
        DateFormat df = new SimpleDateFormat("E, dd.MM");
        return df.format(date);
    }

    public static String getDatumLongFromTime(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        Date date = calendar.getTime();
        DateFormat df = new SimpleDateFormat("E, dd.MM.yyyy");
        return df.format(date);
    }

    public static String getDatumForXml(long time) {
        //2016-01-01T00:00:00
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        Date date = calendar.getTime();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        String dateString = df.format(date);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(dateString);
        stringBuilder.append("T00:00:00");

        return stringBuilder.toString();
    }

    public static void shareLosung(Losung losung, Context context) {
        String[] items = {context.getResources().getString(R.string.losung),
                context.getResources().getString(R.string.lehrtext),
                context.getResources().getString(R.string.losung_and_lehrtext)};

        String title = context.getResources().getString(R.string.losung_from) + " " + Losung.getDatumFromTime(losung.getDate());

        String[] titles = {title, title, title};
        String[] inhalte = new String[3];

        inhalte[0] = String.valueOf(losung.getLosungstext() + System.getProperty("line.separator") + losung.getLosungsvers());
        inhalte[1] = String.valueOf(losung.getLehrtext() + System.getProperty("line.separator") + losung.getLehrtextVers());
        inhalte[2] = inhalte[0] + System.getProperty("line.separator") + System.getProperty("line.separator") + inhalte[1];

        ShareDialog dialog = new ShareDialog(context, items, titles, inhalte);
        dialog.show();
    }

    public static void shareLosung(Losung losung, Context context, int type) {
        String title = context.getResources().getString(R.string.losung_from) + " " + Losung.getDatumFromTime(losung.getDate());
        String content = "";

        switch (type) {
            case(Tags.LOSUNG_NOTIFICATION):
                content = String.valueOf(losung.getLosungstext() + System.getProperty("line.separator") + losung.getLosungsvers());
                break;
            case(Tags.LEHRTEXT_NOTIFICATION):
                content = String.valueOf(losung.getLehrtext() + System.getProperty("line.separator") + losung.getLehrtextVers());
                break;
            case(Tags.LOSUNG_UND_LEHRTEXT_NOTIFICATION):
                content = String.valueOf(losung.getLosungstext() + System.getProperty("line.separator") + losung.getLosungsvers()) +
                        System.getProperty("line.separator") + System.getProperty("line.separator") +
                        String.valueOf(losung.getLehrtext() + System.getProperty("line.separator") + losung.getLehrtextVers());
                break;
        }

        if(content.length() > 1) {
            MainActivity.share(context, title, content);
        }
    }

    public static String getFullDatumFromTime(long datum) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(datum);
        Date date = calendar.getTime();
        DateFormat df = new SimpleDateFormat("EEEE, dd.MM.yyyy");
        return df.format(date);
    }
}