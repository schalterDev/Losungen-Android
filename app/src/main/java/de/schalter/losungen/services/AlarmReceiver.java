package de.schalter.losungen.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import java.util.Calendar;

import de.schalter.losungen.Losung;
import de.schalter.losungen.R;
import de.schalter.losungen.files.DBHandler;
import de.schalter.losungen.log.CustomLog;
import de.schalter.losungen.settings.Tags;

public class AlarmReceiver extends BroadcastReceiver {

    public static final String SHOW_NOTIFICATION = "de.schalter.losungen.show_notificaiton";
    public static final String DOWNLOAD_AUDIO = "de.scahlter.losungen.download_audio";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        CustomLog.writeToLog(
                context,
                new CustomLog(
                        CustomLog.DEBUG,
                        CustomLog.TAG_NOTIFICATION,
                        "Started broadcast with action: " + action
                )
        );

        if (action != null) {
            switch (action) {
                case SHOW_NOTIFICATION:
                    Intent intentShowNotification = new Intent(context.getApplicationContext(), Notifications.class);
                    intentShowNotification.setAction(Notifications.NOTIFICATION_SHOW);
                    context.startService(intentShowNotification);
                    break;
                case DOWNLOAD_AUDIO:
                    Intent intentService = new Intent(context.getApplicationContext(), AudioDownloadService.class);
                    context.startService(intentService);
                    break;
            }
        }
    }
}
