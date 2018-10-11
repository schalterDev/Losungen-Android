package de.schalter.losungen.versionUpdates;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.util.Arrays;

import de.schalter.losungen.changelog.Changelog;
import de.schalter.losungen.dialogs.ImportLosungenDialog;
import de.schalter.losungen.settings.Tags;

public class NewVersion {

    public static void checkForNewVersion(Context context) {
        v27UpdateOldWrongWeeklyWords(context);

        final Changelog changelog = new Changelog(context);

        //Wenn eine neue Version der APP installiert ist
        if (changelog.isNewVersion(Tags.PREF_VERSIONCODE)) {
            //Changelog
            String[] changelogLanguages = Tags.CHANGELOG_LANGUAGES;
            String language = Tags.getLanguage(context);
            //I GUI-Language is not supported for changelog use english
            if (!Arrays.asList(changelogLanguages).contains(language)) {
                language = "en";
            }

            final String finalLanguage = language;
            //Die Daten im Hintergrund parsen
            Thread changelogThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        changelog.prepair(finalLanguage);

                        Handler mHandler = new Handler(Looper.getMainLooper());
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                //und dann Dialog anzeigen
                                changelog.getDialog().show();
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            changelogThread.start();
        }
    }

    // ----------------- VERSION < 27 ---------------------

    private static void v27UpdateOldWrongWeeklyWords(Context context) {
        if(Changelog.getOldVersion(context, Tags.PREF_VERSIONCODE) < 27) {
            //update wekkly words
            ImportLosungenDialog.reimportWeekThisYear(context);
        }
    }
}
