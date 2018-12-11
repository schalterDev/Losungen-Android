package de.schalter.losungen.versionUpdates;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;

import java.io.IOException;
import java.util.Arrays;

import de.schalter.losungen.changelog.Changelog;
import de.schalter.losungen.dialogs.ImportLosungenDialog;
import de.schalter.losungen.services.AudioDownloadService;
import de.schalter.losungen.services.Notifications;
import de.schalter.losungen.settings.Tags;

public class NewVersion {

    public static void checkForNewVersion(Context context) {
        int oldVersion = Changelog.getOldVersion(context, Tags.PREF_VERSIONCODE);
        if (oldVersion < 27) {
            v27UpdateOldWrongWeeklyWords(context);
        }
        if (oldVersion < 42) {
            v42SetupAlarmForNotification(context);
        }
        if (oldVersion < 44) {
            v44RemoveNotificationChannel(context);
        }

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

    private static void v27UpdateOldWrongWeeklyWords(Context context) {
        //update wekkly words
        ImportLosungenDialog.reimportWeekThisYear(context);
    }

    private static void v42SetupAlarmForNotification(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        boolean showNotifications = settings.getBoolean(Tags.PREF_NOTIFICATION, true);
        boolean autoDownload = settings.getBoolean(Tags.PREF_AUDIO_AUTODOWNLOAD, true);
        long timeNotifications = settings.getLong(Tags.PREF_NOTIFICATIONTIME, 60 * 7);
        if (showNotifications) {
            Notifications.setNotifications(context, timeNotifications * 60 *1000);
        }
        if (autoDownload) {
            AudioDownloadService.scheduleAutoDownload(context);
        }
    }

    private static void v44RemoveNotificationChannel(Context context) {
        final String oldNotificationChannel = "audio-play";

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.deleteNotificationChannel(oldNotificationChannel);
        }
    }
}
