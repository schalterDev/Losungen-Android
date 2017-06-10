package de.schalter.losungen;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.Calendar;

import de.schalter.losungen.dialogs.ColorDialog;
import de.schalter.losungen.files.DBHandler;
import de.schalter.losungen.settings.Tags;

/**
 * Created by marti on 04.11.2015.
 */
public class AppWidgetActivity extends Activity {

    public static final String COLOR = "color";
    public static final String BACKGROUND = "background";
    public static final String FONTSIZE = "fontSize";

    private int mAppWidgetId = 0 ;
    private int contentRadio = Tags.LOSUNG_UND_LEHRTEXT_NOTIFICATION;

    private SharedPreferences settings;

    private ColorDialog dialog;

    private TextView textViewWidget;
    private TextView textViewFontSize;
    private RelativeLayout relWidget;

    private DBHandler dbHandler;
    private Calendar calendar;

    private final int min_fontSize = 10;
    private final int default_font_color = Color.BLACK;
    private final int default_background = Color.WHITE;

    private int schriftgroesse = min_fontSize;
    private int font_color = default_font_color;
    private int background_color = default_background;

    private void analytics() {
        if(settings.getBoolean(Tags.PREF_GOOGLEANALYTICS, true)) {
            // Obtain the shared Tracker instance.
            AnalyticsApplication application = (AnalyticsApplication) getApplication();
            Tracker mTracker = application.getDefaultTracker();

            mTracker.setScreenName("AppWidgetActivity");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);

        settings = PreferenceManager.getDefaultSharedPreferences(this);
        analytics();

        setContentView(R.layout.appwidget_activity);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);

            try {
                int color = extras.getInt(COLOR);
                int background = extras.getInt(BACKGROUND);
                int fontSize = extras.getInt(FONTSIZE);

                font_color = color;
                background_color = background;
                schriftgroesse = fontSize;
            } catch (Exception ignored) {
                //If extras are unavailable
                font_color = Color.BLACK;
                background_color = Color.WHITE;
                schriftgroesse = 15;
            }

            if(font_color == 0)
                font_color = -16777216; //Black
        }

        init();
        radioButtons();
        schriftgroesse();
        colors();

        Button fertig = (Button) findViewById(R.id.button_fertig);
        fertig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fertig();
            }
        });
    }

    private void init() {
        textViewWidget = (TextView) findViewById(R.id.textView_widget_preview);
        textViewFontSize = (TextView) findViewById(R.id.textView_textSize);

        relWidget = (RelativeLayout) findViewById(R.id.relative_layout_widget_activity);

        dbHandler = DBHandler.newInstance(this);
        calendar = Calendar.getInstance();
        upateText(contentRadio);
    }

    private void colors() {
        Button btn_font = (Button) findViewById(R.id.button_fontColor);
        Button btn_background = (Button) findViewById(R.id.button_background);

        btn_font.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = new ColorDialog(AppWidgetActivity.this, font_color, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        font_color = AppWidgetActivity.this.dialog.getColor();
                        setColors(font_color, background_color);
                    }
                });

                dialog.show();

                // initialColor is the initially-selected color to be shown in the rectangle on the left of the arrow.
                // for example, 0xff000000 is black, 0xff0000ff is blue. Please be aware of the initial 0xff which is the alpha.
               /* AmbilWarnaDialog dialog = new AmbilWarnaDialog(AppWidgetActivity.this, font_color, true, new AmbilWarnaDialog.OnAmbilWarnaListener() {
                    @Override
                    public void onOk(AmbilWarnaDialog dialog, int color) {
                        font_color = color;
                        setColors(font_color, background_color);
                    }

                    @Override
                    public void onCancel(AmbilWarnaDialog dialog) {
                        // cancel was selected by the user
                    }
                });

                dialog.show();*/
            }
        });

        btn_background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = new ColorDialog(AppWidgetActivity.this, background_color, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        background_color = AppWidgetActivity.this.dialog.getColor();
                        setColors(font_color, background_color);
                    }
                });

                dialog.show();

                // initialColor is the initially-selected color to be shown in the rectangle on the left of the arrow.
                // for example, 0xff000000 is black, 0xff0000ff is blue. Please be aware of the initial 0xff which is the alpha.
                /*AmbilWarnaDialog dialog = new AmbilWarnaDialog(AppWidgetActivity.this, background_color, true, new AmbilWarnaDialog.OnAmbilWarnaListener() {
                    @Override
                    public void onOk(AmbilWarnaDialog dialog, int color) {
                        background_color = color;
                        setColors(font_color, background_color);
                    }

                    @Override
                    public void onCancel(AmbilWarnaDialog dialog) {
                        // cancel was selected by the user
                    }
                });

                dialog.show();*/
            }
        });

        setColors(font_color, background_color);
    }

    private void schriftgroesse() {
        SeekBar fontSizeSeek = (SeekBar) findViewById(R.id.seekBar_fontSize);
        textViewWidget.setTextSize(schriftgroesse);

        fontSizeSeek.setMax(40);

        fontSizeSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewWidget.setTextSize(min_fontSize + progress);
                textViewFontSize.setText(getResources().getString(R.string.widget_activity_fontsize) + ": " + (min_fontSize + progress));
                schriftgroesse = min_fontSize + progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        fontSizeSeek.setProgress(1);

    }

    private void radioButtons() {
        final RadioGroup content = (RadioGroup) findViewById(R.id.radiogroup_content);
        content.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radioButton_Losung:
                        contentRadio = Tags.LOSUNG_NOTIFICATION;
                        break;
                    case R.id.radioButton_lehrtext:
                        contentRadio = Tags.LEHRTEXT_NOTIFICATION;
                        break;
                    case R.id.radioButton_both:
                        contentRadio = Tags.LOSUNG_UND_LEHRTEXT_NOTIFICATION;
                        break;
                }

                upateText(checkedId);

            }
        });
    }

    private void fertig() {
        // Create an Intent to launch MainActivity screen
        Intent intent = new Intent(getBaseContext(), MainActivity.class);

        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);

        // This is needed to make this intent different from its previous intents
        intent.setData(Uri.parse("tel:/" + (int) System.currentTimeMillis()));

        // Creating a pending intent, which will be invoked when the user
        // clicks on the widget
        PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(), 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Getting an instance of WidgetManager
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getBaseContext());

        // Instantiating the class RemoteViews with widget_layout
        RemoteViews views = new RemoteViews(getBaseContext().getPackageName(), R.layout.appwidget);
        views.setOnClickPendingIntent(R.id.relLayout_widget, pendingIntent);

        Calendar calendar = Calendar.getInstance();
        DBHandler dbHandler = DBHandler.newInstance(AppWidgetActivity.this);
        Losung losung = dbHandler.getLosung(calendar.getTimeInMillis());
        String text = "";
        switch(contentRadio) {
            case Tags.LOSUNG_NOTIFICATION:
                text = losung.getLosungstext() + System.getProperty("line.separator") +
                        losung.getLosungsvers();
                break;
            case Tags.LEHRTEXT_NOTIFICATION:
                text = losung.getLehrtext() + System.getProperty("line.separator") +
                        losung.getLehrtextVers();
                break;
            case Tags.LOSUNG_UND_LEHRTEXT_NOTIFICATION:
                text = losung.getLosungstext() + " " +
                        losung.getLosungsvers() + System.getProperty("line.separator") + System.getProperty("line.separator") +
                        losung.getLehrtext() + " " +
                        losung.getLehrtextVers();
                break;
        }

        //Set scrollable


        views.setTextViewText(R.id.textView_main, text);
        views.setFloat(R.id.textView_main, "setTextSize", schriftgroesse);
        views.setInt(R.id.relLayout_widget, "setBackgroundColor",
                background_color);
        views.setTextColor(R.id.textView_main, font_color);
        // Setting the background color of the widget
        //views.setInt(R.id.relLayout_widget, "setBackgroundColor", color);

        //  Attach an on-click listener to the clock
        //views.setOnClickPendingIntent(R.id.widget_aclock, pendingIntent);

        // Tell the AppWidgetManager to perform an update on the app widget
        appWidgetManager.updateAppWidget(mAppWidgetId, views);

        // Return RESULT_OK from this activity
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(AppWidgetActivity.this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("widgetcontent" + mAppWidgetId, contentRadio);
        editor.putInt("widgetfontsize" + mAppWidgetId, schriftgroesse);
        editor.putInt("widgetcolor" + mAppWidgetId, font_color);
        editor.putInt("widgetbackground" + mAppWidgetId, background_color);
        editor.apply();

        setResult(RESULT_OK, resultValue);
        finish();
    }

    private void upateText(int radio) {
        Losung losung = dbHandler.getLosung(calendar.getTimeInMillis());

        String text = "";
        switch(contentRadio) {
            case Tags.LOSUNG_NOTIFICATION:
                text = losung.getLosungstext() + System.getProperty("line.separator") +
                        losung.getLosungsvers();
                break;
            case Tags.LEHRTEXT_NOTIFICATION:
                text = losung.getLehrtext() + System.getProperty("line.separator") +
                        losung.getLehrtextVers();
                break;
            case Tags.LOSUNG_UND_LEHRTEXT_NOTIFICATION:
                text = losung.getLosungstext() + " " +
                        losung.getLosungsvers() + System.getProperty("line.separator") + System.getProperty("line.separator") +
                        losung.getLehrtext() + " " +
                        losung.getLehrtextVers();
                break;
        }

        textViewWidget.setText(text);
    }

    private void setColors(int font_color, int background) {
        relWidget.setBackgroundColor(background);
        textViewWidget.setTextColor(font_color);
    }

    private int convertDpIntoPx(int dp) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}
