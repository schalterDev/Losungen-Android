package de.schalter.losungen.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {

    public static String SHOW_NOTIFICATION = "de.schalter.losungen.show_notificaiton";
    public static String DOWNLOAD_AUDIO = "de.scahlter.losungen.download_audio";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action != null && action.equals(SHOW_NOTIFICATION)) {
            Intent intentNotification = new Intent(context, Notifications.class);
            context.startService(intentNotification);
        } else if (action != null && action.equals(DOWNLOAD_AUDIO)) {
            Intent intentService = new Intent(context, AudioDownloadService.class);
            context.startService(intentService);
        }
    }
}
