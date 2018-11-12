package de.schalter.losungen.services;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

import de.schalter.losungen.Losung;
import de.schalter.losungen.MainActivity;
import de.schalter.losungen.R;
import de.schalter.losungen.files.DBHandler;
import de.schalter.losungen.log.CustomLog;
import de.schalter.losungen.settings.Tags;

/**
 * Created by marti on 31.10.2015.
 */
public class Notifications extends Service {

    public static final String NOTIFICATION_CHANNEL_ID = "Losungen";
    private static final String NOTIFICATION_CHANNEL_NAME = "Losungen";
    private static final String NOTIFICATION_CHANNEL_DESCRIPTION = "Daily notifications";
    private static final int NOTIFICATION_ID = 241504;

    public Notifications() {
        super();
    }

    public static void createDailyNotificationChannel(Context context) {
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
        CustomLog.writeToLog(context, new CustomLog(CustomLog.DEBUG, CustomLog.TAG_NOTIFICATION, "Set Notification with time: " + time));

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

        CustomLog.writeToLog(context, new CustomLog(CustomLog.DEBUG, CustomLog.TAG_NOTIFICATION, "Notifications canceled"));
        Log.i("Losungen", "Notifications removed");
    }

    private static PendingIntent getPendingIntentForNotification(Context context) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.SHOW_NOTIFICATION);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    private void showNotification(Context context, String titel, String msg, long datum) {
        Notifications.createDailyNotificationChannel(context);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(Tags.TAG_LASTNOTIFICATION, System.currentTimeMillis());
        editor.apply();

        NotificationManager mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher))
                        .setContentTitle(titel)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg)
                        .setAutoCancel(true)
                        .addAction(R.drawable.ic_action_share,
                                getResources().getString(R.string.share),
                                getPendingAction(this, "SHARE", msg, titel))
                        .addAction(R.drawable.ic_action_star,
                                getResources().getString(R.string.mark_favorite),
                                getPendingAction(this, "MARK", datum));

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());

        CustomLog.writeToLog(context, new CustomLog(CustomLog.DEBUG, CustomLog.TAG_NOTIFICATION, "Show notification"));
    }

    private PendingIntent getPendingAction(Context context, String action, String losung, String title) {
        Intent intent = new Intent(context, Notifications.class);
        intent.putExtra("losung", losung);
        intent.putExtra("title", title);
        intent.setAction(action);

        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getPendingAction(Context context, String action,long datum) {
        Intent intent = new Intent(context, Notifications.class);
        intent.putExtra("datum", datum);
        intent.setAction(action);

        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        CustomLog.writeToLog(this, new CustomLog(
                CustomLog.DEBUG,
                CustomLog.TAG_NOTIFICATION,
                "Started service with action: " + intent.getAction()
        ));

        Context context = getApplicationContext();

        String action = intent.getAction();
        if(action == null)
            action = "";

        switch (action) {
            case "SHARE":
                String msg = intent.getStringExtra("losung");
                String title = intent.getStringExtra("title");

                Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                this.sendBroadcast(it);

                MainActivity.share(this, title, msg);
                break;
            case "MARK": {
                long datum = intent.getLongExtra("datum", 0);
                DBHandler dbHandler = DBHandler.newInstance(this);
                dbHandler.setMarkiert(datum);

                Intent intentClose = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                this.sendBroadcast(intentClose);

                MainActivity.toast(this, getResources().getString(R.string.add_fav), Toast.LENGTH_SHORT);
                break;
            }
            default: {

                DBHandler dbHandler = DBHandler.newInstance(this);
                Calendar calendar = Calendar.getInstance();
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

                boolean showNotification = settings.getBoolean(Tags.PREF_NOTIFICATION, true);

                //Show Notification
                if(showNotification) {
                    Losung losung = dbHandler.getLosung(calendar.getTimeInMillis());

                    if (!losung.getLosungstext().equals(getResources().getString(R.string.no_date))) {
                        switch (settings.getInt(Tags.PREF_NOTIFICATIONART, Tags.LOSUNG_UND_LEHRTEXT_NOTIFICATION)) {
                            case Tags.LOSUNG_NOTIFICATION:
                                showNotification(context, getResources().getString(R.string.losung),
                                        losung.getLosungstext() + System.getProperty("line.separator") + losung.getLosungsvers(),
                                        losung.getDate());
                                break;

                            case Tags.LEHRTEXT_NOTIFICATION:
                                showNotification(context, getResources().getString(R.string.lehrtext),
                                        losung.getLehrtext() + System.getProperty("line.separator") + losung.getLehrtextVers(),
                                        losung.getDate());
                                break;

                            case Tags.LOSUNG_UND_LEHRTEXT_NOTIFICATION:
                                showNotification(context, getResources().getString(R.string.losungen),
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

        return START_NOT_STICKY;
    }
}