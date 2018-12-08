package de.schalter.losungen.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;
import android.os.Build;

import java.util.Calendar;

import de.schalter.losungen.Losung;
import de.schalter.losungen.MainActivity;
import de.schalter.losungen.R;
import de.schalter.losungen.files.DBHandler;
import de.schalter.losungen.log.CustomLog;
import de.schalter.losungen.settings.Tags;

public class AlarmReceiver extends BroadcastReceiver {

    public static final String SHOW_NOTIFICATION = "de.schalter.losungen.show_notificaiton";
    public static final String DOWNLOAD_AUDIO = "de.schalter.losungen.download_audio";

    public static final String NOTIFICATION_SHARE = "SHARE";
    public static final String NOTIFICATION_SHARE_MESSAGE = "message";
    public static final String NOTIFICATION_SHARE_TITLE = "title";

    public static final String NOTIFICATION_MARK = "MARK";
    public static final String NOTIFICATION_MARK_DATE = "date";

    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

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
                    showNotificationInitial();
                    break;
                case DOWNLOAD_AUDIO:
                    checkForConnectivityAndDownload();
                    break;
            }
        }
    }

    private void checkForConnectivityAndDownload() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

        //Download AUDIO
        boolean autoDownloadAudio = settings.getBoolean(Tags.PREF_AUDIO_AUTODOWNLOAD, false);
        if(autoDownloadAudio) {
            boolean wifiConnected = Tags.isWifiConnected(context);
            boolean mobileConnected = Tags.isMobileConnected(context);

            int network = Integer.valueOf(settings.getString(Tags.PREF_AUDIO_AUTODOWNLOAD_NETWORK, "0"));
            //network: 0 (only wifi), 1 (all)
            if(wifiConnected) {
                //Wifi enabled
                downloadAudio();
            } else if(mobileConnected && network == 1) {
                //Wifi not enabled but user allows to download with mobile internet
                downloadAudio();
            } else {
                AudioDownloadService.setupJobSchedulerWhenWifiConnected(context);
            }
        }
    }

    private void downloadAudio() {
        Intent intentService = new Intent(context.getApplicationContext(), AudioDownloadService.class);
        intentService.setAction(AudioDownloadService.ACTION_START_DOWNLOAD);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intentService);
        } else {
            context.startService(intentService);
        }
    }

    /**
     * Do not move this out of the broadcast receiver. Otherwise a service is needed and
     * in Android >= O only foreground services are allowed
     */
    private void showNotificationInitial() {
        DBHandler dbHandler = DBHandler.newInstance(context);
        Calendar calendar = Calendar.getInstance();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

        boolean showNotification = settings.getBoolean(Tags.PREF_NOTIFICATION, true);

        //Show Notification
        if (showNotification) {
            Losung losung = dbHandler.getLosung(calendar.getTimeInMillis());

            if (!losung.getLosungstext().equals(context.getResources().getString(R.string.no_date))) {
                switch (settings.getInt(Tags.PREF_NOTIFICATIONART, Tags.LOSUNG_UND_LEHRTEXT_NOTIFICATION)) {
                    case Tags.LOSUNG_NOTIFICATION:
                        Notifications.showNotification(context, context.getResources().getString(R.string.losung),
                                losung.getLosungstext() + System.getProperty("line.separator") + losung.getLosungsvers(),
                                losung.getDate());
                        break;

                    case Tags.LEHRTEXT_NOTIFICATION:
                        Notifications.showNotification(context, context.getResources().getString(R.string.lehrtext),
                                losung.getLehrtext() + System.getProperty("line.separator") + losung.getLehrtextVers(),
                                losung.getDate());
                        break;

                    case Tags.LOSUNG_UND_LEHRTEXT_NOTIFICATION:
                        Notifications.showNotification(context, context.getResources().getString(R.string.losungen),
                                losung.getLosungstext() + System.getProperty("line.separator") + losung.getLosungsvers() +
                                        System.getProperty("line.separator") + System.getProperty("line.separator") +
                                        losung.getLehrtext() + System.getProperty("line.separator") + losung.getLehrtextVers(),
                                losung.getDate());
                        break;
                }
            }
        }
    }
}
