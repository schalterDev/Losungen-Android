package de.schalter.losungen.services;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import de.schalter.losungen.Losung;
import de.schalter.losungen.MainActivity;
import de.schalter.losungen.R;
import de.schalter.losungen.files.DBHandler;
import de.schalter.losungen.settings.Tags;

/**
 * Created by marti on 31.10.2015.
 */
public class Notifications extends Service {

    private static final int NOTIFICATION_ID = 241504;

    public Notifications() {
        super();
    }

    public static void setNotifications(Context context, long time) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        long lastNotification = settings.getLong(Tags.TAG_LASTNOTIFICATION, 0);

        //Was this day allready a notification?
        Calendar calendarNotification = Calendar.getInstance();
        calendarNotification.setTimeInMillis(lastNotification);
        boolean notificationToday = (calendarNotification.get(Calendar.DAY_OF_YEAR) ==
                Calendar.getInstance().get(Calendar.DAY_OF_YEAR));


        Intent intent = new Intent(context, Notifications.class);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);

        int hourOfDay = (int) (time / 1000 / 60 / 60);
        int minute = (int) ((time - (1000 * 60 * 60 *hourOfDay)) / 1000 / 60);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        //If their was a notification today add one day
        if(notificationToday) calendar.add(Calendar.DAY_OF_YEAR, 1);

        alarmManager.cancel(pendingIntent);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 24 * 60 * 60 * 1000, pendingIntent);

        Log.i("Losungen", "Notifications added");
    }

    public static void removeNotifications(Context context) {
        Intent intent = new Intent(context, Notifications.class);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);

        alarmManager.cancel(pendingIntent);

        Log.i("Losungen", "Notifications canceled");
    }

    private void showNotification(Context context, String titel, String msg, long datum) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(Tags.TAG_LASTNOTIFICATION, System.currentTimeMillis());
        editor.apply();

        NotificationManager mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
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
                                        losung.getDatum());
                                break;

                            case Tags.LEHRTEXT_NOTIFICATION:
                                showNotification(context, getResources().getString(R.string.lehrtext),
                                        losung.getLehrtext() + System.getProperty("line.separator") + losung.getLehrtextVers(),
                                        losung.getDatum());
                                break;

                            case Tags.LOSUNG_UND_LEHRTEXT_NOTIFICATION:
                                showNotification(context, getResources().getString(R.string.losungen),
                                        losung.getLosungstext() + System.getProperty("line.separator") + losung.getLosungsvers() +
                                                System.getProperty("line.separator") + System.getProperty("line.separator") +
                                                losung.getLehrtext() + System.getProperty("line.separator") + losung.getLehrtextVers(),
                                        losung.getDatum());
                                break;
                        }
                    }
                }

                //Download AUDIO
                boolean autoDownloadAudio = settings.getBoolean(Tags.PREF_AUDIO_AUTODOWNLOAD, false);
                if(autoDownloadAudio) {
                    boolean wifiConnected = Tags.isWifiConnected(getApplicationContext());
                    boolean mobileConnected = Tags.isMobileConnected(getApplicationContext());

                    int network = Integer.valueOf(settings.getString(Tags.PREF_AUDIO_AUTODOWNLOAD_NETWORK, "0"));
                    //network: 0 (only wifi), 1 (all)
                    if(wifiConnected) {
                        //Wifi enabled
                        downloadAudio();
                    } else if(mobileConnected && network == 1) {
                        //Wifi not enabled but user allows to download with mobile internet
                        downloadAudio();
                    }
                }
                break;
            }
        }

        return START_NOT_STICKY;
    }

    private void downloadAudio() {
        final DBHandler dbHandler = DBHandler.newInstance(getApplicationContext());
        long datumJetzt = System.currentTimeMillis();
        final long datum = dbHandler.getLosung(datumJetzt).getDatum();
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        Thread download = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //get URL first
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(datum);
                    String url = Tags.getAudioUrl(Notifications.this, calendar);

                    //set Path
                    String folder = "audio";
                    String fileName = Notifications.this.getString(R.string.app_name) + "_" + Losung.getDatumLongFromTime(datum) + ".mp3";

                    //use internal or external storage
                    boolean internal = !settings.getBoolean(Tags.PREF_AUDIO_EXTERNAL_STORGAE, false);

                    final DownloadTask downloadTask = new DownloadTask(getApplicationContext(), url, folder, fileName, internal, R.string.download_ticker, R.string.content_title);

                    //When finished
                    Runnable finished = new Runnable() {
                        @Override
                        public void run() {
                            //Write into database
                            String absolutePath = downloadTask.getAbsolutePath();
                            dbHandler.addAudioLosungen(datum, absolutePath);

                            //We dont want to play the file
                            //playFile(absolutePath);
                        }
                    };
                    downloadTask.onFinishedListener(finished);

                    //Start download with notification
                    downloadTask.execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        //Check if audio-file exists allready
        String pathAudioLosung = dbHandler.getAudioLosungen(datum);
        if(pathAudioLosung != null) { //Es wurde bereits ein Pfad gespeichert
            //Es kann aber immer noch sein, dass der Pfad nicht mehr stimmt
            //Wenn zum Beispiel die SD-Karte entfernt wurde
            //Deswegen wird überprüft ob die Datei existiert
            File file = new File(pathAudioLosung);
            if(file.exists()) {
                //playFile(pathAudioLosung);
                //Do nothing
            } else {
                download.start();
            }
        } else {
            //Audio download and write into database
            download.start();
        }
    }


}
