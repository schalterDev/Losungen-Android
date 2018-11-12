package de.schalter.losungen.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {

    public static String SHOW_NOTIFICATION = "de.schalter.losungen.show_notificaiton";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action != null && action.equals(SHOW_NOTIFICATION)) {
            Intent intentNotification = new Intent(context, Notifications.class);
            context.startService(intentNotification);
        }
    }
}
