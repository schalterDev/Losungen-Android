package de.schalter.losungen.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import de.schalter.losungen.Losung;
import de.schalter.losungen.MainActivity;
import de.schalter.losungen.R;
import de.schalter.losungen.customViews.CardLosung;
import de.schalter.losungen.dialogs.LosungOpenDialog;
import de.schalter.losungen.dialogs.OpenUrlDialog;
import de.schalter.losungen.dialogs.SearchDialog;
import de.schalter.losungen.dialogs.ShareSermon;
import de.schalter.losungen.files.DBHandler;
import de.schalter.losungen.services.DownloadTask;
import de.schalter.losungen.settings.Tags;
import schalter.dev.customizelibrary.Colors;
import schalter.dev.mediaplayer_library.AudioService;
import schalter.dev.mediaplayer_library.ControlElements;

/**
 * Created by marti on 27.10.2015.
 */
public class FragmentLosungTag extends Fragment implements ControlElements {

    private Menu menu;
    private DBHandler dbHandler;
    private MainActivity mainActivity;
    private Context context;
    private SharedPreferences settings;

    private Losung losung;

    private AudioService audioService;
    private RelativeLayout audio_relative;
    private TextView audio_title;
    private TextView audio_subtitle;
    private TextView audio_duration;
    private ImageView play_audio;
    private ImageView close_audio;
    private SeekBar audio_seek_bar;
    private String path_audio;

    /**
     * @param time which day is it
     * @return the fragment with the right daily word
     */
    public static FragmentLosungTag newFragmentLosungTag(long time, MainActivity context) {
        FragmentLosungTag fragment = new FragmentLosungTag();
        fragment.setDate(time, context);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        initialiseSettings();

        View view = inflater.inflate(R.layout.fragment_losung_tag, container,
                false);

        context = getContext();

        initialise(view);

        checkAudioIsRunning();

        return view;
    }

    /**
     * @param datum the time of the day of the daily word
     * @return the daily word of this day
     */
    private Losung getLosung(long datum) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(datum);

        datum = calendar.getTimeInMillis();

        //I had some errors with context = null
        //so no I check and try every available method
        //to get a context
        if(context == null) {
            context = mainActivity;
        }
        if(context == null)
            context = getActivity();

        dbHandler = DBHandler.newInstance(context);

        return dbHandler.getLosung(datum);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.menu_losung_tag, menu);

        this.menu = menu;
        updateMarkiertIcon();

        super.onCreateOptionsMenu(menu, menuInflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem audioMenuItem = menu.findItem(R.id.action_audio);

        if(audioMenuItem != null) {
            String textAudioMenu;

            String file = dbHandler.getAudioLosungen(losung.getDatum());
            boolean isDownloaded;
            if(file != null) {
                File file1 = new File(file);
                isDownloaded = file1.exists();
            } else {
                isDownloaded = false;
            }

            String platzHalter = Losung.getDatumFromTime(losung.getDatum());

            if(isDownloaded) {
                textAudioMenu = getResources().getString(R.string.audio_download_downloaded);
                textAudioMenu = String.format(textAudioMenu, platzHalter);
            } else {
                textAudioMenu = getResources().getString(R.string.audio_download_download);
                textAudioMenu = String.format(textAudioMenu, platzHalter);
            }
            audioMenuItem.setTitle(textAudioMenu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            //Managed by MainActivity
            return false;
        } else if (id == R.id.action_search) {
            //Show search dialog
            SearchDialog dialog = new SearchDialog(mainActivity);
            dialog.show();
            return true;
        } else if(id == R.id.action_mark) {
            //Set this daily word as marked/unmarked
            losung.setMarkiert(!losung.isMarkiert());
            setMarkiert(losung.isMarkiert());
            return true;
        } else if(id == R.id.action_web) {
            //Get the right bible-translation for bibleserver.com
            String uebersetzung = Tags.getUebersetzung(settings.getString(Tags.SELECTED_LANGUAGE, "en"));

            String urlLosung = "http://www.bibleserver.com/text/" + uebersetzung + "/" + losung.getLosungsvers();
            String urlLehrtext = "http://www.bibleserver.com/text/" + uebersetzung + "/" + losung.getLehrtextVers();

            String[] urls = new String[2];
            urls[0] = urlLosung;
            urls[1] = urlLehrtext;

            String[] items = {
                    getResources().getString(R.string.losung),
                    getResources().getString(R.string.lehrtext)};

            //Let the user choose what to show: new or old testament vers
            OpenUrlDialog dialog = new OpenUrlDialog(context, items, urls);
            dialog.show();

            return true;
        } else if(id == R.id.action_share) {
            //Opens a share dialog
            Losung.shareLosung(losung, context);
            return true;
        } else if(id == R.id.action_audio) {
            playAudio();
            return true;
        } else if(id == R.id.action_share_sermon) {
            ShareSermon dialog = new ShareSermon(context, losung);
            dialog.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void playAudio() {
        Thread download = new Thread(new Runnable() {
            @Override
            public void run() {
                MainActivity.toast(context, context.getString(R.string.download_starting), Toast.LENGTH_SHORT);
                //get URL first
                losung.getSermonUrlDownload(new Runnable() {
                    @Override
                    public void run() {
                        String url = losung.getUrlForDownload();

                        if(url == null) {
                            MainActivity.toast(context, context.getString(R.string.download_error_connection), Toast.LENGTH_LONG);
                        } else {
                            //set Path
                            String folder = "audio";
                            String fileName = context.getString(R.string.app_name) + "_" +
                                    Losung.getDatumLongFromTime(losung.getDatum()) + ".mp3";

                            //use internal or external storage
                            boolean internal = !settings.getBoolean(Tags.PREF_AUDIO_EXTERNAL_STORGAE, false);

                            final DownloadTask downloadTask = new DownloadTask(context, url, folder, fileName, internal, R.string.download_ticker, R.string.content_title);

                            //When finished
                            Runnable finished = new Runnable() {
                                @Override
                                public void run() {
                                    //Write into database
                                    String absolutePath = downloadTask.getAbsolutePath();
                                    dbHandler.addAudioLosungen(losung.getDatum(), absolutePath);

                                    playFile(absolutePath);
                                }
                            };
                            downloadTask.onFinishedListener(finished);

                            //Start download with notification
                            downloadTask.execute();
                        }
                    }
                });
            }
        });

        boolean stream = !settings.getBoolean(Tags.PREF_AUDIO_DOWNLOAD, true);

        //Check if audio-file exists allready
        String pathAudioLosung = dbHandler.getAudioLosungen(losung.getDatum());
        if(pathAudioLosung != null) { //Es wurde bereits ein Pfad gespeichert
            //Es kann aber immer noch sein, dass der Pfad nicht mehr stimmt
            //Wenn zum Beispiel die SD-Karte entfernt wurde
            //Deswegen wird überprüft ob die Datei existiert
            File file = new File(pathAudioLosung);
            if(file.exists()) {
                playFile(pathAudioLosung);
            } else {
                //Only if users want to download
                if(stream) {
                    streamFile();
                } else {
                    download.start();
                }
            }
        } else {
            //Audio download and write into database
            //TODO audio download (ask user) and notify user
            //Only if users want to download
            if(stream) {
                streamFile();
            } else {
                download.start();
            }
        }
    }

    private void playFile(String path) {

        Intent intent = new Intent(context, AudioService.class);
        intent.setAction(AudioService.ACTION_PLAY);
        context.startService(intent);

        context.bindService(intent, serviceConnector, Context.BIND_AUTO_CREATE);

        path_audio = path;

        //Show control buttons
        //Will be done when music starts
    }

    private void serviceReady(String path) {
        //Play audio with notification
        audioService.setElements(this);
        audioService.setSong(path, Losung.getFullDatumFromTime(losung.getDatum()), "ERF Wort zum Tag");
        audioService.setPrimaryColor(Colors.getColor(context, Colors.PRIMARY));
        audioService.setPrimarDarkColor(Colors.getColor(context, Colors.PRIMARYDARK));
        audioService.setIcon(R.mipmap.ic_launcher);
        audioService.setPendingActivity(mainActivity);

        audioService.firstStart(AudioService.ACTION_PLAY);
    }

    private void streamFile() {
        Thread stream = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //get URL first
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(losung.getDatum());
                    String url = Tags.getAudioUrl(calendar);

                    //Play audio with notification
                    playFile(url);
                } catch (IOException e) {
                    e.printStackTrace();
                    MainActivity.toast(context, context.getResources().getString(R.string.download_error), Toast.LENGTH_LONG);
                }
            }
        });
        stream.start();
    }

    private void setMarkiert(boolean markiert) {
        //Update the Icon in the Action-Bar
        updateMarkiertIcon();

        //Show right the text in the snackbar
        //and change the value in the files
        if(markiert) {
            mainActivity.snackbar(context.getString(R.string.add_fav), Snackbar.LENGTH_SHORT, true);
            dbHandler.setMarkiert(losung.getDatum());
        } else {
            mainActivity.snackbar(context.getString(R.string.remove_fav), Snackbar.LENGTH_SHORT, true);
            dbHandler.removeMarkiert(losung.getDatum());
        }
    }

    private void updateMarkiertIcon() {
        if(losung != null) {
            if (!losung.isMarkiert()) {
                menu.getItem(1).setIcon(getResources()
                        .getDrawable(R.drawable.ic_star_notfilled));
            } else {
                menu.getItem(1).setIcon(getResources()
                        .getDrawable(R.drawable.ic_star_filled));
            }
        } else {
            Log.e("Losungen", "Error");
        }
    }

    private void initialiseSettings() {
        //I had some problems with context = null
        //So now I try every method to get a context
        if(context != null)
            settings = PreferenceManager.getDefaultSharedPreferences(context);
        else if(getContext() != null) {
            settings = PreferenceManager.getDefaultSharedPreferences(getContext());
            context = getContext();
        } else if (getActivity() != null) {
            settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
            context = getActivity();
        } else if(mainActivity != null) {
            settings = PreferenceManager.getDefaultSharedPreferences(mainActivity);
            context = mainActivity;
        } else {
            Log.e("Losungen", "in FragmentLosungTag:  context, getContext and getActivity are null");
        }
    }

    private void initialise(View view) {
        audio_relative = (RelativeLayout) view.findViewById(R.id.audio_relative);
        audio_title = (TextView) view.findViewById(R.id.textView_audio_title);
        audio_subtitle = (TextView) view.findViewById(R.id.textView_subtitile_audio);
        audio_duration = (TextView) view.findViewById(R.id.textView_duration);
        audio_seek_bar = (SeekBar) view.findViewById(R.id.seekBar_audio);
        play_audio = (ImageView) view.findViewById(R.id.audio_play);
        close_audio = (ImageView) view.findViewById(R.id.audio_cancle);

        CardLosung cardLosung = (CardLosung) view.findViewById(R.id.cardLosung);

        if(losung != null) {
            cardLosung.setLosungsText(losung.getLosungstext());
            cardLosung.setLosungsTitle(getResources().getString(R.string.losung));
            cardLosung.setLosungsVers(losung.getLosungsvers());
        }

        //Show dialog to choose how to open the verse

        cardLosung.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] items = {getResources().getString(R.string.open_in_app_dialog),
                        getResources().getString(R.string.open_in_browser_dialog)};

                LosungOpenDialog dialog = new LosungOpenDialog(context);
                dialog.show(items, losung, true);
            }
        });

        //Share dialog onLongClick
        cardLosung.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Losung.shareLosung(losung, context, Tags.LOSUNG_NOTIFICATION);
                return true;
            }
        });

        CardLosung cardLehrvers = (CardLosung) view.findViewById(R.id.cardLehrtext);
        if(losung != null) {
            cardLehrvers.setLosungsText(losung.getLehrtext());
            cardLehrvers.setLosungsTitle(getResources().getString(R.string.lehrtext));
            cardLehrvers.setLosungsVers(losung.getLehrtextVers());
        }

        cardLehrvers.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Losung.shareLosung(losung, context, Tags.LEHRTEXT_NOTIFICATION);
                return true;
            }
        });

        cardLehrvers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] items = {getResources().getString(R.string.open_in_app_dialog),
                        getResources().getString(R.string.open_in_browser_dialog)};

                LosungOpenDialog dialog = new LosungOpenDialog(context);
                dialog.show(items, losung, false);
            }
        });

        // -------------- MAYBE HIDE LOSUNG OR LEHRTEXT CARD -------------
        int which_vers = Integer.parseInt(settings.getString(Tags.WHICH_VERS_TO_SHOW, "0"));

        switch (which_vers) {
            case 0: //both
                //nothing to do
                break;
            case 1: //only Losung (OT)
                //hide lehrtext card
                cardLehrvers.setVisibility(View.GONE);
                break;
            case 2: //only Lehretext (NT)
                //hide losung card
                cardLosung.setVisibility(View.GONE);
                break;
        }

        // ------------------ END HIDE LEHRTEXT OR LOSUNG ----------------

        EditText editNotizen = (EditText) view.findViewById(R.id.editText_notizen);

        //In the settings you can choose to show notes or not
        boolean showNotes = settings.getBoolean(Tags.PREF_SHOWNOTES, true);
        if(!showNotes) {
            LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.linearLayout_losung);
            try {
                linearLayout.removeView(editNotizen);
            } catch(Exception ignored) {}
        } else {
            editNotizen.setMaxLines(15);
            editNotizen.setSingleLine(false);
            editNotizen.setHint(getResources().getString(R.string.notes_hint));
            if(losung != null)
                editNotizen.setText(losung.getNotizenLosung());

            editNotizen.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    //Safe changes to files
                    try {
                        if(dbHandler != null)
                            dbHandler.editLosungNotiz(losung.getDatum(), s.toString());
                    } catch (Exception e) {
                        Log.d("Losungen", "Fehler: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public void setDate(long time, MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        losung = getLosung(time);
    }

    private void checkAudioIsRunning() {
        //TODO onBind
    }

    private Handler handler = new Handler();
    private Runnable moveSeekBarThread;

    @Override
    public void init(final AudioService audioService, int duration) { //in milliseconds

        // ---------------- SEEK BAR --------------------------

        moveSeekBarThread = new Runnable() {

            public void run() {
                if(audioService.isPlaying()){

                    try {
                        int mediaPos_new = audioService.getCurrentPosition();
                        int mediaMax_new = audioService.getMusicDuration();
                        audio_seek_bar.setMax(mediaMax_new);
                        audio_seek_bar.setProgress(mediaPos_new);

                        //Set TextView audio duration

                        String duration = getTimeFormat(mediaPos_new) + " / " + getTimeFormat(mediaMax_new);
                        audio_duration.setText(duration);

                        handler.postDelayed(this, 100); //Looping the thread after 0.1 second
                        // seconds
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        audio_seek_bar.setMax(duration);
        audio_seek_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    audioService.seekMusicTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // ------------------ SEEK BAR ENDE ------------------

        //Image Buttons
        play_audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(audioService.isPlaying()) {
                    audioService.pauseMusic();
                    play_audio.setImageDrawable(getResources().getDrawable(R.drawable.ic_media_play));
                } else {
                    audioService.startMusic();
                    play_audio.setImageDrawable(getResources().getDrawable(R.drawable.ic_media_pause));
                }
            }
        });

        close_audio.setColorFilter(Color.argb(255, 10, 10, 10));
        close_audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioService.closeMusic();
            }
        });

        //Text
        audio_title.setText(audioService.getSongTitle());
        audio_subtitle.setText(audioService.getSongSubtitle());
    }

    private String getTimeFormat(int time) {
        int minutes = time / 1000 / 60;
        int seconds = (time / 1000) % 60;

        String duration = minutes + ":";

        if(seconds >= 10) {
            duration += String.valueOf(seconds);
        } else {
            duration += "0" + String.valueOf(seconds);
        }

        return duration;
    }

    @Override
    public void pause() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                play_audio.setImageDrawable(getResources()
                        .getDrawable(R.drawable.ic_media_play));
            }
        });
    }

    @Override
    public void play() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                audio_relative.setVisibility(View.VISIBLE);
                play_audio.setImageDrawable(getResources()
                        .getDrawable(R.drawable.ic_media_pause));
            }
        });

        handler.removeCallbacks(moveSeekBarThread);
        handler.postDelayed(moveSeekBarThread, 100);
    }

    @Override
    public void cancel() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                audio_relative.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void seekedTo(int position) {
        //Nothing to do
    }

    @Override
    public void songSet(final String title, final String subtitle) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                audio_title.setText(title);
                audio_subtitle.setText(subtitle);
            }
        });
    }

    boolean isBound = false;

    private ServiceConnection serviceConnector = new ServiceConnection() {

        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            AudioService.MyBinder binder = (AudioService.MyBinder) service;
            audioService = binder.getService(); //<--------- from here on can access service!
            serviceReady(path_audio);
            isBound = true;
        }

        public void onServiceDisconnected(ComponentName arg0) {
            serviceConnector = null;
            isBound = false;
        }

    };
}
