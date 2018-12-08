package de.schalter.losungen.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Calendar;

import de.schalter.losungen.R;
import de.schalter.losungen.log.CustomLog;
import de.schalter.losungen.network.Network;

public class AudioDownloadService extends IntentService {

    public static final String ACTION_START_DOWNLOAD = "START_DOWNLOAD";

    private static final int JOB_ID = 125843;

    public AudioDownloadService() {
        super(AudioDownloadService.class.getSimpleName());
    }

    public static void setupJobSchedulerWhenWifiConnected(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

            JobInfo.Builder jobBuilder = new JobInfo.Builder(
                    JOB_ID,
                    new ComponentName(context.getPackageName(), AudioDownloadService.class.getName())
            );
            jobBuilder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
            jobBuilder.setPersisted(true);

            jobScheduler.schedule(jobBuilder.build());
        }
    }

    public static void scheduleAutoDownload(Context context) {
        CustomLog.writeToLog(context, new CustomLog(
                CustomLog.DEBUG,
                CustomLog.TAG_AUDIO_DOWNLOAD,
                "schedule auto download"
        ));

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

    @Override
    protected void onHandleIntent(Intent intent) {
        CustomLog.writeToLog(this, new CustomLog(
                CustomLog.DEBUG,
                CustomLog.TAG_AUDIO_DOWNLOAD,
                "Started service for audio download with action: " + intent.getAction()
        ));

        if (intent.getAction() != null && intent.getAction().equals(ACTION_START_DOWNLOAD)) {
            this.startDownloadAsForeground();
        } else {
            // this is for jobscheduler
            // TODO how to add action to intent of job scheduler
            this.startDownloadAsForeground();
        }
    }

    private void startDownloadAsForeground() {
        Context context = getApplicationContext();

        DownloadNotificationHelper downloadNotificationHelper = new DownloadNotificationHelper(
                context,
                R.string.download_ticker,
                R.string.content_title);

        Notification notification = downloadNotificationHelper.createNotification();
        this.startForeground(DownloadNotificationHelper.NOTIFICATION_ID, notification);

        Network.downloadSermon(context, null);
    }
}
