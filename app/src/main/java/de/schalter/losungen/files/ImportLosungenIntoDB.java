package de.schalter.losungen.files;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.schalter.losungen.R;
import de.schalter.losungen.dialogs.WaitDialog;

/**
 * Created by martin on 16.11.16.
 */

public class ImportLosungenIntoDB {

    /**
     * Imports Losungen from the given files from assets into the database
     *
     * @param context application context
     * @param filesAssets every file from assets to import
     * @param whenFinished will be called after all imports
     * @param update the entrys already exist in the database. Just update them
     * @param month are the files to import monthly words
     * @param week are the files to import weekly words
     */
    public static void importLosungenFromAssets(final Context context, final List<String> filesAssets,
                                                final Runnable whenFinished, final boolean update, final boolean month, final boolean week) {

        final WaitDialog waitDialog = new WaitDialog(context, R.string.importString, R.string.importing);
        waitDialog.show();

        Thread background = new Thread(new Runnable() {
            @Override
            public void run() {
                XmlParser xmlParser = new XmlParser();
                InputStream in = null;
                for(int i = 0; i < filesAssets.size(); i++) {
                    try {
                        in = context.getResources().getAssets().open(filesAssets.get(i) + ".xml");
                        xmlParser.parseXML(in);
                        if(update) {
                            xmlParser.updateIntoDatabase(context, month, week);
                        } else {
                            xmlParser.writeIntoDatabase(context, month, week);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                waitDialog.close();

                if(whenFinished != null) {
                    Handler mainHandler = new Handler(Looper.getMainLooper());
                    mainHandler.post(whenFinished);
                }
            }
        });
        background.start();

    }

    /**
     * Imports Losungen from the given file-paths into the database
     *
     * @param context application context
     * @param filesStorage every filePath to import
     * @param whenFinished will be called after all imports
     * @param update the entrys already exist in the database. Just update them
     * @param month are the files to import monthly words
     * @param week are the files to import weekly words
     */
    public static void importLosungenFromStorage(final Context context, final List<String> filesStorage,
                                      final Runnable whenFinished, final boolean update, final boolean month, final boolean week) {

        final WaitDialog waitDialog = new WaitDialog(context, R.string.importString, R.string.importing);
        waitDialog.show();

        Thread background = new Thread(new Runnable() {
            @Override
            public void run() {
                XmlParser xmlParser = new XmlParser();
                InputStream in = null;
                for(int i = 0; i < filesStorage.size(); i++) {
                    try {
                        in = new FileInputStream(filesStorage.get(i));
                        xmlParser.parseXML(in);
                        if(update) {
                            xmlParser.updateIntoDatabase(context, month, week);
                        } else {
                            xmlParser.writeIntoDatabase(context, month, week);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                waitDialog.close();

                if(whenFinished != null) {
                    Handler mainHandler = new Handler(Looper.getMainLooper());
                    mainHandler.post(whenFinished);
                }
            }
        });
        background.start();

    }

    /**
     * writes the monthly and weekly losungen to the database
     * @param context application context
     * @param language which language of the losugen to import
     * @param years which years to import
     * @param ifFinished will be called after all imports are finished
     * @param update are the weekly and monthly words already int he database and only
     *               have to be updated?
     */
    public static void importMonthAndWeeks(final Context context, String language, List<String> years,
                                    final Runnable ifFinished, final boolean update) {
        final List<String> filesMonth = new ArrayList<>();
        final List<String> filesWeek = new ArrayList<>();
        for(int i = 0; i < years.size(); i++) {
            filesMonth.add(language + "/" + years.get(i) + "_month");
            filesWeek.add(language + "/" + years.get(i) + "_week");
        }

        final WaitDialog waitDialog = new WaitDialog(context, R.string.importVerb, R.string.importing);
        waitDialog.show();

        Runnable afterMonth = new Runnable() {

            public void run() {
                importLosungenFromAssets(context, filesWeek, ifFinished, update, false, true);
                waitDialog.close();
            }

        };

        importLosungenFromAssets(context, filesMonth, afterMonth, update, true, false);
    }
}
