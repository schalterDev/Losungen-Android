package de.schalter.losungen;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.nononsenseapps.filepicker.FilePickerActivity;
import com.nononsenseapps.filepicker.Utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import de.schalter.losungen.files.DBHandler;
import de.schalter.losungen.files.Files;
import de.schalter.losungen.log.CustomLog;
import de.schalter.losungen.services.AudioDownloadService;
import de.schalter.losungen.services.Notifications;
import de.schalter.losungen.settings.Tags;
import schalter.dev.customizelibrary.Colors;
import schalter.dev.customizelibrary.CustomToolbar;
import schalter.dev.customizelibrary.DesignPref;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    /**
     * Determines whether to always show the simplified settings UI, where
     * settings are presented in a single list. When false, settings are shown
     * as a master/detail two-pane view on tablets. When true, a single pane is
     * shown on tablets.
     */
    private static final boolean ALWAYS_SIMPLE_PREFS = false;
    private SharedPreferences prefs;

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());

        Intent refresh = new Intent(this, MainActivity.class);
        startActivity(refresh);
        finish();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setTheme(Colors.getTheme(this));
        Colors.setStatusBarColor(this, Colors.PRIMARY_DARK);

        getWindow().getDecorView().setBackgroundColor(Colors.getColor(this, Colors.WINDOWS_BACKGROUND));
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == MainActivity.LOG_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = Uri.parse(data.getDataString().replace("external_files","storage"));
            CustomLog.exportLogToFile(this, uri);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        prefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);


        CustomToolbar toolbar;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
            toolbar = (CustomToolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
            root.addView(toolbar, 0); // insert at top
        } else {
            ViewGroup root = findViewById(android.R.id.content);
            ListView content = (ListView) root.getChildAt(0);

            root.removeAllViews();

            toolbar = (CustomToolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);


            int height;
            TypedValue tv = new TypedValue();
            if (getTheme().resolveAttribute(R.attr.actionBarSize, tv, true)) {
                height = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
            }else{
                height = toolbar.getHeight();
            }

            content.setPadding(0, height, 0, 0);

            root.addView(content);
            root.addView(toolbar);
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        setupSimplePreferencesScreen();

        Preference exportLogPreference = (Preference) findPreference(Tags.PREF_LOG_EXPORT);
        exportLogPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                CustomLog.exportLog(SettingsActivity.this);
                return true;
            }
        });
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                                         Preference preference){
        String key = preference.getKey();

        if (key.equals(Tags.PREF_LOG_EXPORT)) {
            CustomLog.exportLog(SettingsActivity.this);

            return true;
        }

        return false;
    }

    /**
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     */
    private void setupSimplePreferencesScreen() {
        if (!isSimplePreferences(this)) {
            return;
        }

        // In the simplified UI, fragments are not used at all and we instead
        // use the older PreferenceActivity APIs.

        // Add 'general' preferences.
        addPreferencesFromResource(R.xml.pref_general);


        // Add 'notifications' preferences, and a corresponding header.
        PreferenceCategory fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle(getResources().getString(R.string.pref_notifications));
        getPreferenceScreen().addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_notification);


        //Add 'Customize' preference, and a corresponding header
        PreferenceCategory fakeHeaderCustomize = new PreferenceCategory(this);
        fakeHeaderCustomize.setTitle(getResources().getString(R.string.pref_customize));
        getPreferenceScreen().addPreference(fakeHeaderCustomize);
        addPreferencesFromResource(R.xml.pref_customize);

        //Nur wenn die Sprache auch unterstützt wird
        if(Arrays.asList(Tags.ANDACHT_LANGUAGES).contains(Tags.getLanguage(this))) {
            try {
                PreferenceCategory fakeHeaderAudio = new PreferenceCategory(this);
                fakeHeaderAudio.setTitle(getResources().getString(R.string.pref_audio));
                getPreferenceScreen().addPreference(fakeHeaderAudio);
                addPreferencesFromResource(R.xml.pref_audio);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //Add 'Developer' preference, and a corresponding header
        PreferenceCategory fakeHeaderDeveloper = new PreferenceCategory(this);
        fakeHeaderDeveloper.setTitle(getResources().getString(R.string.pref_debug));
        getPreferenceScreen().addPreference(fakeHeaderDeveloper);
        addPreferencesFromResource(R.xml.pref_developer);

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences to
        // their values. When their values change, their summaries are updated
        // to reflect the new value, per the Android Design guidelines.
        // bindPreferenceSummaryToValue(findPreference(getString(R.string.key_checkbox)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this) && !isSimplePreferences(this);
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Determines whether the simplified settings UI should be shown. This is
     * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
     * doesn't have newer APIs like {@link PreferenceFragment}, or the device
     * doesn't have an extra-large screen. In these cases, a single-pane
     * "simplified" settings UI should be shown.
     */
    private static boolean isSimplePreferences(Context context) {
        return ALWAYS_SIMPLE_PREFS
                || (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
                || !isXLargeTablet(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        if (!isSimplePreferences(this)) {
            loadHeadersFromResource(R.xml.pref_headers, target);
        }
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return NotificationPreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName)
                || AudioPreferenceFragment.class.getName().equals(fragmentName)
                || CustomizePreferenceFragment.class.getName().equals(fragmentName);
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
        if(key.equals(Tags.PREF_NOTIFICATION)
                || key.equals(Tags.PREF_NOTIFICATIONTIME)) {

            if(sharedPreferences.getBoolean(Tags.PREF_NOTIFICATION, true)) {

                //Set time for notification or sermon download
                long time = sharedPreferences.getLong(Tags.PREF_NOTIFICATIONTIME, 60 * 7);
                Notifications.setNotifications(this, time * 60 * 1000);
            } else
                Notifications.removeNotifications(this);
        }

        if (key.equals(Tags.PREF_AUDIO_AUTODOWNLOAD)) {
            boolean autoDownload = sharedPreferences.getBoolean(Tags.PREF_AUDIO_AUTODOWNLOAD, true);
            if (autoDownload) {
                AudioDownloadService.scheduleAutoDownload(this);
            } else {
                AudioDownloadService.cancleAutoDownload(this);
            }
        }

        //Language change
        if(key.equals(Tags.PREF_LANGUAGE)) {
            try {
                setLocale(sharedPreferences.getString(key, "en"));
            } catch (Exception e) {
                Log.e("Losungen", "Error changing language: " + e.getMessage());
                e.printStackTrace();
            }
        }

        //SD-Card change
        if(key.equals(Tags.PREF_AUDIO_EXTERNAL_STORGAE)) {
            boolean sd_card = sharedPreferences.getBoolean(key, false);
            moveSermons(sd_card);
        }

        //Audio delete days change
        if(key.equals(Tags.PREF_AUDIO_DELETE_DAYS)) {
            String text = sharedPreferences.getString(key, "0");

            try {
                int days = Integer.parseInt(text);
            } catch (Exception e) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(key, "0");
                editor.apply();
                MainActivity.toast(this,this.getResources().getString(R.string.invalid_number), Toast.LENGTH_LONG);
            }
        }

        //Design changed
        if(key.equals(DesignPref.TAGSETTING)) {
            Intent refresh = new Intent(this, MainActivity.class);
            startActivity(refresh);
            finish();
        }
    }

    private void moveSermons(final boolean sdcard) {
        final ProgressDialog dialog = new ProgressDialog(this);

        if(sdcard)//move to sd-card
            dialog.setMessage(this.getString(R.string.moving_to_sdcard));
        else //move to internal storage
            dialog.setMessage(this.getString(R.string.moving_to_internal));

        dialog.show();

        Thread moveSermons = new Thread(new Runnable() {
            @Override
            public void run() {
                int success = 0;
                int failed = 0;

                //Get all sermons
                DBHandler dbHandler = DBHandler.newInstance(SettingsActivity.this);
                List<Long> idSermons = dbHandler.getAllAudios();

                Files files = new Files();

                String path = "";
                String newPath;
                File sermon;
                for(int i = 0; i < idSermons.size(); i++) {
                    path = dbHandler.getAudioLosungen(idSermons.get(i));
                    sermon = new File(path);
                    try {
                        InputStream inputSermon = Files.getInputStreamFromFile(sermon);

                        if(sdcard) { //move to sd-card
                            newPath = files.writeToRealExternalCacheStorage(SettingsActivity.this, inputSermon, "audio",
                                    path.substring(path.lastIndexOf("/"), path.length() - 1));
                        } else {
                            newPath = files.writeToPrivateStorage(SettingsActivity.this, inputSermon, "audio",
                                    path.substring(path.lastIndexOf("/"), path.length() - 1));
                        }

                        if(newPath != null) {
                            dbHandler.addAudioLosungen(idSermons.get(i), newPath);
                            sermon.delete();
                            success++;
                        } else {
                            Log.w("Losungen", "Failed to move sermon, path: " + path);
                            failed++;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        failed++;
                    }
                }

                Handler handler = new Handler(Looper.getMainLooper());
                final int finalSuccess = success;
                final int finalFailed = failed;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        dialog.cancel();
                        MainActivity.toast(SettingsActivity.this,
                                SettingsActivity.this.getString(R.string.successful) +
                                        ": " + finalSuccess +
                                        ", " + SettingsActivity.this.getString(R.string.failed) +
                                        ": " + finalFailed,
                                Toast.LENGTH_LONG);
                    }
                });
            }
        });
        moveSermons.start();

    }
    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
        }
    }

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
     public static class NotificationPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_notification);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class AudioPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_audio);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class CustomizePreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_customize);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class DeveloperPreferenceFragment extends  PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstaceState) {
            super.onCreate(savedInstaceState);
            addPreferencesFromResource(R.xml.pref_developer);
        }
    }
}
