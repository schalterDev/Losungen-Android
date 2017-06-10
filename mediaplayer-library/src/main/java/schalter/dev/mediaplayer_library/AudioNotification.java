package schalter.dev.mediaplayer_library;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;
/**
 * Created by Smarti on 10.01.2016.
 */
public class AudioNotification {

    private static int NOTIFICATION_ID = 8645156;

    private NotificationCompat.Builder mBuilder;
    private RemoteViews mContentView;
    private NotificationManager notificationManager;
    private Context context;

    private PendingIntent pendingIntentImage;
    private PendingIntent pendingIntentNoti;

    private boolean playing;
    private String title;
    private String subtitle;
    private int drawable_resource;
    private PendingIntent pendingIntentClose;

    private int colorPrimary;
    private int colorPrimaryDark;
    private int drawable_launcher;

    public AudioNotification(Context context, String title, String subtitle) {
        this.context = context;
        this.title = title;
        this.subtitle = subtitle;
        init();
    }

    private void init() {
        playing = true;

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setSmallIcon(R.drawable.ic_notification_play);
        mBuilder.setAutoCancel(false);

        if(pendingIntentNoti != null)
            mBuilder.setContentIntent(pendingIntentNoti);

        mContentView = new RemoteViews(context.getPackageName(), R.layout.audio_notification);
        //mContentView.setImageViewResource(R.id.notifimage, R.drawable.cumulus_icon);
        mContentView.setTextViewText(R.id.audio_notification_title, title);
        mContentView.setTextViewText(R.id.audio_notification_subtitle, subtitle);

        //OnClickListener for picture
        mContentView.setOnClickPendingIntent(R.id.audio_notification_image, pendingIntentImage);
        //OnClickListener for close
        mContentView.setOnClickPendingIntent(R.id.audio_notification_close, pendingIntentClose);

        mBuilder.setContent(mContentView);
    }

    public void showNotification() {
        notificationManager.notify(NOTIFICATION_ID, getNotification());
    }

    public Notification getNotification() {
        //Set colors
        //mContentView.setInt(R.id.audio_notification_layout, "setBackgroundResource", colorPrimary);
        //mContentView.setInt(R.id.imageView2, "setBackgroundResource", colorPrimaryDark);
        //TODO Absturz???

        //Set Drawable
        mContentView.setImageViewResource(R.id.imageView2, drawable_launcher);

        //Update Text
        mContentView.setTextViewText(R.id.audio_notification_title, title);
        mContentView.setTextViewText(R.id.audio_notification_subtitle, subtitle);
        //Update drawable
        mContentView.setImageViewResource(R.id.audio_notification_image, drawable_resource);
        //Update pending intent image
        mContentView.setOnClickPendingIntent(R.id.audio_notification_image, pendingIntentImage);
        mContentView.setOnClickPendingIntent(R.id.audio_notification_close, pendingIntentClose);
        //Update pending intent
        if(pendingIntentNoti != null)
            mBuilder.setContentIntent(pendingIntentNoti);

        mBuilder.setContent(mContentView);
        Notification mNotification = mBuilder.build();

        if(playing) {
            mNotification.flags = mNotification.flags | (Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT);
        } else {

        }

        return mNotification;
    }

    public void update(String title, String subtitle){
        this.title = title;
        this.subtitle = subtitle;
    }

    private void update(int drawable_resource) {
        this.drawable_resource = drawable_resource;
    }

    public void updateOnClickImage(PendingIntent pendingIntentImage) {
        this.pendingIntentImage = pendingIntentImage;
    }

    public void updateOnClickNoti(PendingIntent pendingIntentNoti) {
        this.pendingIntentNoti = pendingIntentNoti;
    }

    public void updateOnClickClose(PendingIntent pendingIntentClose) {
        this.pendingIntentClose = pendingIntentClose;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;

        if(playing) {
            update(R.drawable.ic_media_pause);
        } else {
            update(R.drawable.ic_media_play);
        }
    }

    public void cancel() {
        //remove the notification from the status bar
        notificationManager.cancel(NOTIFICATION_ID);
    }

    public void setColorPrimary(int colorPrimary) {
        this.colorPrimary = colorPrimary;
    }

    public void setColorPrimaryDark(int colorPrimaryDark) {
        this.colorPrimaryDark = colorPrimaryDark;
    }

    public void setDrawable_launcher(int drawable_launcher) {
        this.drawable_launcher = drawable_launcher;
    }
}
