package de.schalter.losungen.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

import de.schalter.losungen.MainActivity;
import de.schalter.losungen.R;
import de.schalter.losungen.files.DBHandler;
import de.schalter.losungen.log.CustomLog;
import de.schalter.losungen.settings.Tags;

import static de.schalter.losungen.services.AlarmReceiver.NOTIFICATION_MARK;
import static de.schalter.losungen.services.AlarmReceiver.NOTIFICATION_MARK_DATE;
import static de.schalter.losungen.services.AlarmReceiver.NOTIFICATION_SHARE;
import static de.schalter.losungen.services.AlarmReceiver.NOTIFICATION_SHARE_MESSAGE;
import static de.schalter.losungen.services.AlarmReceiver.NOTIFICATION_SHARE_TITLE;

/**
 * Created by marti on 31.10.2015.
 */
public class Notifications extends IntentService {

    static final String NOTIFICATION_CHANNEL_ID = "Losungen";
    private static final String NOTIFICATION_CHANNEL_NAME = "Losungen";
    private static final String NOTIFICATION_CHANNEL_DESCRIPTION = "Daily notifications";
    private static final int NOTIFICATION_ID = 241504;

    static void createDailyNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, importance);
            channel.setDescription(NOTIFICATION_CHANNEL_DESCRIPTION);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static void setNotifications(Context context, long time) {
        // CustomLog.writeToLog(context, new CustomLog(CustomLog.DEBUG, CustomLog.TAG_NOTIFICATION, "Set Notification with time: " + time));

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = getPendingIntentForNotification(context);

        int hourOfDay = (int) (time / 1000 / 60 / 60);
        int minute = (int) ((time - (1000 * 60 * 60 *hourOfDay)) / 1000 / 60);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        alarmManager.cancel(pendingIntent);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 24 * 60 * 60 * 1000, pendingIntent);

        Log.i("Losungen", "Notifications added");
    }

    public static void removeNotifications(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent =getPendingIntentForNotification(context);

        alarmManager.cancel(pendingIntent);

        // CustomLog.writeToLog(context, new CustomLog(CustomLog.DEBUG, CustomLog.TAG_NOTIFICATION, "Notifications canceled"));
        Log.i("Losungen", "Notifications removed");
    }

    private static PendingIntent getPendingIntentForNotification(Context context) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.SHOW_NOTIFICATION);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    static void showNotification(Context context, String titel, String msg, long datum) {
        Notifications.createDailyNotificationChannel(context);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(Tags.TAG_LASTNOTIFICATION, System.currentTimeMillis());
        editor.apply();

        NotificationManager mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.mipmap.ic_launcher))
                        .setContentTitle(titel)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg)
                        .setAutoCancel(true)
                        .addAction(R.drawable.ic_action_share,
                                context.getResources().getString(R.string.share),
                                getPendingActionShare(context, msg, titel))
                        .addAction(R.drawable.ic_action_star,
                                context.getResources().getString(R.string.mark_favorite),
                                getPendingActionMark(context, datum));

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());

//        CustomLog.writeToLog(context, new CustomLog(CustomLog.DEBUG, CustomLog.TAG_NOTIFICATION, "Show notification"));
    }

    private static PendingIntent getPendingActionShare(Context context, String losung, String title) {
        Intent intent = new Intent(context, Notifications.class);
        intent.putExtra(NOTIFICATION_SHARE_MESSAGE, losung);
        intent.putExtra(NOTIFICATION_SHARE_TITLE, title);
        intent.setAction(NOTIFICATION_SHARE);

        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static PendingIntent getPendingActionMark(Context context, long datum) {
        Intent intent = new Intent(context, Notifications.class);
        intent.putExtra(NOTIFICATION_MARK_DATE, datum);
        intent.setAction(NOTIFICATION_MARK);

        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private Context context;

    public Notifications() {
        super(Notifications.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();

//        CustomLog.writeToLog(
//                context,
//                new CustomLog(
//                        CustomLog.DEBUG,
//                        CustomLog.TAG_NOTIFICATION,
//                        "Started intent service with action: " + action
//                )
//        );

        if (action != null) {
            this.context = getApplicationContext();

            switch (action) {
                case NOTIFICATION_MARK:
                    markNotification(intent.getLongExtra(NOTIFICATION_MARK_DATE, System.currentTimeMillis()));
                    break;
                case NOTIFICATION_SHARE:
                    shareNotification(
                            intent.getStringExtra(NOTIFICATION_SHARE_MESSAGE),
                            intent.getStringExtra(NOTIFICATION_SHARE_TITLE));
                    break;
            }
        }
    }

    private void shareNotification(String message, String title) {
        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(it);

        MainActivity.share(context, title, message);
    }

    private void markNotification(long date) {
        DBHandler dbHandler = DBHandler.newInstance(context);
        dbHandler.setMarkiert(date);

        Intent intentClose = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(intentClose);

        MainActivity.toast(context, context.getResources().getString(R.string.add_fav), Toast.LENGTH_SHORT);

    }
}