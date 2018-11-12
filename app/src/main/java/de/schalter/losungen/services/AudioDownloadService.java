package de.schalter.losungen.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;

import java.util.Calendar;

import de.schalter.losungen.log.CustomLog;
import de.schalter.losungen.network.Network;
import de.schalter.losungen.settings.Tags;

public class AudioDownloadService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        CustomLog.writeToLog(this, new CustomLog(
                CustomLog.DEBUG,
                CustomLog.TAG_AUDIO_DOWNLOAD,
                "Started service for audio download with action: " + intent.getAction()
        ));

        Context context = getApplicationContext();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

        //Download AUDIO
        boolean autoDownloadAudio = settings.getBoolean(Tags.PREF_AUDIO_AUTODOWNLOAD, false);
        if(autoDownloadAudio) {
            boolean wifiConnected = Tags.isWifiConnected(getApplicationContext());
            boolean mobileConnected = Tags.isMobileConnected(getApplicationContext());

            int network = Integer.valueOf(settings.getString(Tags.PREF_AUDIO_AUTODOWNLOAD_NETWORK, "0"));
            //network: 0 (only wifi), 1 (all)
            if(wifiConnected) {
                //Wifi enabled
                Network.downloadSermon(context, null);
            } else if(mobileConnected && network == 1) {
                //Wifi not enabled but user allows to download with mobile internet
                Network.downloadSermon(context, null);
            }
        }

        return START_NOT_STICKY;
    }

    public static void scheduleAutoDownload(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        int hourOfDay = 5;
        int minute = 0;
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        PendingIntent pendingIntent = getPendingIntentForAutoDownload(context);

        alarmManager.cancel(pendingIntent);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 24 * 60 * 60 * 1000, pendingIntent);
    }

    public static void cancleAutoDownload(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        alarmManager.cancel(getPendingIntentForAutoDownload(context));
    }

    private static PendingIntent getPendingIntentForAutoDownload(Context context) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.DOWNLOAD_AUDIO);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }
}
