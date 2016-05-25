package de.schalter.losungen.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import de.schalter.losungen.settings.Tags;

/**
 * Created by marti on 31.10.2015.
 */
public class BootBroadcast extends BroadcastReceiver {

    @Override
    public void onReceive(Context ctx, Intent arg1) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
        if(settings.getBoolean(Tags.PREF_NOTIFICATION, true)) {
            Notifications.setNotifications(ctx, settings.getLong(Tags.PREF_NOTIFICATIONTIME, 1000 * 60 * 60 * 7));
        }
    }
}
