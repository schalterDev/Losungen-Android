package schalter.dev.mediaplayer_library;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import java.io.IOException;

/**
 * Created by Smarti on 09.01.2016.
 */
public class AudioService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnBufferingUpdateListener {

    public static final String ACTION_PLAY = "PLAY";
    public static final String ACTION_RESUME = "RESUME";
    public static final String ACTION_CLOSE = "CLOSE";

    private boolean running = false;

    private String mUrl;

    private AudioNotification audioNotification;
    private NotificationManager notificationManager;

    private MediaPlayer mMediaPlayer = null;    // The Media Player
    private int mBufferPosition;
    private String title;
    private String subtitle;

    private int colorPrimary = Integer.MIN_VALUE;
    private int colorPrimaryDark = Integer.MIN_VALUE;
    private int drawable_launcher = Integer.MIN_VALUE;
    private Activity pendingActivity = null;

    private ControlElements elements;

    @Override
    public void onCompletion(MediaPlayer mp) {
        stopForeground(true);
        elements.cancel();
    }

    // indicates the state our service:
    enum State {
        Retrieving, // the MediaRetriever is retrieving music
        Stopped, // media player is stopped and not prepared to play
        Preparing, // media player is preparing...
        Playing, // playback active (media player ready!). (but the media player may actually be
        // paused in this state if we don't have audio focus. But we stay in this state
        // so that we know we have to resume playback once we get focus back)
        Paused
        // playback paused (media player ready!)
    };

    State mState = State.Retrieving;

    private final IBinder mBinder = new MyBinder();

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    public class MyBinder extends Binder {
        public AudioService getService() {
            return AudioService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        //if(running)
        if(intent != null)
            firstStart(intent.getAction());

        running = true;

        return START_STICKY;
    }

    public void firstStart(String action) {
        try {
            if (action.equals(ACTION_PLAY)) {
                audioNotification = new AudioNotification(getApplicationContext(), title, subtitle);

                //Set color and icon
                if (colorPrimary != Integer.MIN_VALUE) {
                    audioNotification.setColorPrimary(colorPrimary);
                }
                if (colorPrimaryDark != Integer.MIN_VALUE) {
                    audioNotification.setColorPrimaryDark(colorPrimaryDark);
                }
                if (drawable_launcher != Integer.MIN_VALUE) {
                    audioNotification.setDrawable_launcher(drawable_launcher);
                }

                audioNotification.updateOnClickImage(getPendingIntentResume());
                audioNotification.updateOnClickNoti(getPendingIntent());
                audioNotification.updateOnClickClose(getPendingIntentClose());
                notificationAsForeground(audioNotification.getNotification());

                mMediaPlayer = new MediaPlayer(); // initialize it here
                mMediaPlayer.setOnPreparedListener(this);
                mMediaPlayer.setOnErrorListener(this);
                mMediaPlayer.setOnBufferingUpdateListener(this);
                mMediaPlayer.setOnCompletionListener(this);
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                initMediaPlayer();
            } else if (action.equals(ACTION_RESUME)) {
                if (mState.equals(State.Paused)) {
                    startMusic();
                } else if (mState.equals(State.Playing)) {
                    pauseMusic();
                }
            } else if (action.equals(ACTION_CLOSE)) {
                closeMusic();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void initMediaPlayer() {
        try {
            mMediaPlayer.setDataSource(mUrl);
        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
            // ...
        }

        try {
            mMediaPlayer.prepare(); // prepare async to not block main thread
        } catch (IllegalStateException e) {
            // ...
        } catch (IOException e) {
            e.printStackTrace();
        }
        mState = State.Preparing;

        elements.init(this, mMediaPlayer.getDuration());
    }

    public void restartMusic() {
        seekMusicTo(0);
    }

    protected void setBufferPosition(int progress) {
        mBufferPosition = progress;
    }

    /** Called when MediaPlayer is ready */
    @Override
    public void onPrepared(MediaPlayer player) {
        // Begin playing music
        mState = State.Paused;
        startMusic();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        stopForeground(true);
        return false;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDestroy() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }
        mState = State.Retrieving;
    }

    public MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }

    public void pauseMusic() {
        if (mState.equals(State.Playing)) {
            elements.pause();
            mMediaPlayer.pause();
            mState = State.Paused;

            //Update Notification
            audioNotification.setPlaying(false);
            notificationAsForeground(audioNotification.getNotification());
            stopForeground(false);
        }
    }

    public void startMusic() {
        if (!mState.equals(State.Preparing) &&!mState.equals(State.Retrieving)) {
            elements.play();
            mMediaPlayer.start();
            mState = State.Playing;

            //Update Notification
            audioNotification.setPlaying(true);
            notificationAsForeground(audioNotification.getNotification());
        }
    }

    public void closeMusic() {
        elements.cancel();
        mMediaPlayer.release();
        mState = State.Stopped;
        stopForeground(true);
    }

    public boolean isPlaying() {
        return mState.equals(State.Playing);
    }

    public int getMusicDuration() {
        // Return current music duration
        return mMediaPlayer.getDuration();
    }

    public int getCurrentPosition() {
        // Return current position
        return mMediaPlayer.getCurrentPosition();
    }

    public int getBufferPercentage() {
        return mBufferPosition;
    }

    public void seekMusicTo(int pos) {
        // Seek music to pos
        mMediaPlayer.seekTo(pos);
        elements.seekedTo(pos);
    }

    public void setSong(String url, String newTitle, String newSubtitle) {
        mUrl = url;
        title = newTitle;
        subtitle = newSubtitle;

        elements.songSet(newTitle, newSubtitle);
    }

    public void setPendingActivity(Activity pendingActivity) {
        this.pendingActivity = pendingActivity;
    }

    public void setPrimaryColor(int primaryColor) {
        colorPrimary = primaryColor;
    }

    public void setPrimarDarkColor(int primarDarkColor) {
        colorPrimaryDark = primarDarkColor;
    }

    public void setIcon(int icon) {
        drawable_launcher = icon;
    }

    public String getSongTitle() {
        return title;
    }

    public String getSongSubtitle() {
        return subtitle;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        setBufferPosition(percent * mp.getDuration() / 100);
    }

    /**
     * Configures service as a foreground service. A foreground service is a service that's doing something the user is
     * actively aware of (such as playing music), and must appear to the user as a notification. That's why we create
     * the notification here.
     */
    private int NOTIFICATION_ID = 451515;

    private void notificationAsForeground(Notification notification) {
        startForeground(NOTIFICATION_ID, notification);
    }
    
    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(getApplicationContext(), pendingActivity.getClass());
        intent.setAction(AudioService.ACTION_RESUME);

        return PendingIntent.getActivity(getApplicationContext(), 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getPendingIntentResume() {
        Intent intent = new Intent(getApplicationContext(), AudioService.class);
        intent.setAction(AudioService.ACTION_RESUME);

        return PendingIntent.getService(getApplicationContext(), 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getPendingIntentClose() {
        Intent intent = new Intent(getApplicationContext(), AudioService.class);
        intent.setAction(AudioService.ACTION_CLOSE);

        return PendingIntent.getService(getApplicationContext(), 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void setElements(ControlElements elements) {
        this.elements = elements;
    }


}