package de.schalter.losungen;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.nononsenseapps.filepicker.FilePickerActivity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.schalter.losungen.dialogs.ImportLosungenDialog;
import de.schalter.losungen.files.DBHandler;
import de.schalter.losungen.files.XmlNotesImport;
import de.schalter.losungen.files.XmlWriter;
import de.schalter.losungen.fragments.FragmentInfo;
import de.schalter.losungen.fragments.FragmentLosung;
import de.schalter.losungen.fragments.FragmentLosungenListe;
import de.schalter.losungen.fragments.FragmentMonth;
import de.schalter.losungen.fragments.FragmentWidgets;
import de.schalter.losungen.intro.MyIntro;
import de.schalter.losungen.log.CustomLog;
import de.schalter.losungen.services.NetworkChangeReceiver;
import de.schalter.losungen.services.WidgetBroadcast;
import de.schalter.losungen.settings.Tags;
import de.schalter.losungen.versionUpdates.NewVersion;
import schalter.dev.customizelibrary.Colors;
import schalter.dev.customizelibrary.CustomToolbar;

public class MainActivity extends AppCompatActivity implements FragmentMonth.Callbacks {

    private static final int FILE_CODE = 55;
    public static final int LOG_CODE = 57;
    private static final int IMPORT_CODE = 56;
    private Drawer navigationDrawer;
    private CoordinatorLayout coordinatorLayout;
    private AdView adview;

    private SharedPreferences settings;

    private CustomToolbar toolbar;

    private boolean searchResultShown;
    private boolean firstStart = false;
    private boolean firstStartResume = false;
    private PrimaryDrawerItem itemLosung;

    private static MainActivity activity;

    public static MainActivity getInstance() {
        return activity;
    }

    private void loadLanguage() {
        String lang = settings.getString(Tags.PREF_LANGUAGE, "---");
        if (!lang.equals("---") && !lang.equals("0")) {
            Locale locale = new Locale(lang);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());
        }
    }

    private void setupDownloadedAudios() {
        // deactivate network change broadcast receiver for android >= 5
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            PackageManager packageManager = this.getPackageManager();
            ComponentName componentName = new ComponentName(this, NetworkChangeReceiver.class);
            packageManager.setComponentEnabledSetting(
                    componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
        }

        // delete old audios
        Thread delteAudios = new Thread(new Runnable() {
            @Override
            public void run() {
                String daysString = settings.getString(Tags.PREF_AUDIO_DELETE_DAYS, "0");
                int days = Integer.parseInt(daysString);
                if (days > 0) {
                    long timeLimit = System.currentTimeMillis() - days * 24l * 60l * 60l * 1000l;
                    DBHandler dbHandler = DBHandler.newInstance(MainActivity.this);
                    List<Long> allAudios = dbHandler.getAllAudios();
                    for (int i = 0; i < allAudios.size(); i++) {
                        if (allAudios.get(i) < timeLimit) {
                            //If not marked
                            Losung losung = dbHandler.getLosung(allAudios.get(i));
                            if (!losung.isMarked()) {
                                //DELTE AUDIO
                                String path = dbHandler.getAudioLosungen(allAudios.get(i));

                                File file = new File(path);
                                boolean deleted = file.delete();
                                if (!deleted) {
                                    Log.e("Losungen", "Failed to delete Audio file: " + allAudios.get(i));
                                } else {
                                    dbHandler.setAudioNull(allAudios.get(i));
                                }
                            }
                        }
                    }
                }
            }
        });
        delteAudios.start();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setTheme(Colors.getTheme(this));

        //Logger
        CustomLog.setPreference(this, Tags.PREF_DEBUG_LOG, false);

        settings = PreferenceManager.getDefaultSharedPreferences(this);
        loadLanguage();
        NewVersion.checkForNewVersion(this);
        setupDownloadedAudios();

        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.losungen));

        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        ads();

        setSupportActionBar(toolbar);

        if (firstStart()) {
            firstInit();
        }

        setupNavigationDrawer();

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        MainActivity.activity = this;
    }

    private void ads() {
        adview = findViewById(R.id.adView);

        if (settings.getBoolean(Tags.PREF_ADS, false)) {
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice("38D4E5DE97D0586BC967B91DA47A055B")
                    .addTestDevice("C0E595547EB0BEF935787005A0EE4148")
                    .build();
            adview.loadAd(adRequest);
        } else {
            LinearLayout linearLayout = findViewById(R.id.linear_layout_main);
            linearLayout.removeView(adview);
        }
    }

    public void snackbar(String titel, int duration, boolean fav) {
        Snackbar snackbar = Snackbar.make(coordinatorLayout, titel, duration);
        if (fav) {
            View snackBarView = snackbar.getView();
            snackBarView.setBackgroundColor(Colors.getColor(this, Colors.ACCENT));
        }

        snackbar.show();
    }

    private static Handler mHandler;

    public static void toast(final Context context, final String title, final int duration) {
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, title, duration).show();
            }
        });
    }

    public static void toast(final Context context, final int titleResource, final int duration) {
        if(mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, titleResource, duration).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (firstStart) {
            if (firstStartResume) {
                firstStart = !firstStart;
                navigationDrawer.setSelection(itemLosung, true);
            } else
                firstStartResume = !firstStartResume;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } /*else if(id == R.id.action_import) {
            dialogImport();
            return true;
        }*/ else if (id == R.id.action_change_language) {
            final Runnable restart = new Runnable() {
                @Override
                public void run() {
                    Intent refresh = new Intent(MainActivity.this, MainActivity.class);
                    startActivity(refresh);
                    finish();
                }
            };

            ImportLosungenDialog importLosungen = new ImportLosungenDialog(this, true, restart);
            importLosungen.show();
            return true;
        } else if (id == R.id.action_export) {
            exportNotes();
        } else if (id == R.id.action_import) {
            importNotes();
        }

        return super.onOptionsItemSelected(item);
    }

    public static void share(Context context, String title, String text) {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, title);
        sharingIntent.putExtra(Intent.EXTRA_TEXT, text);

        Intent finalIntent = Intent.createChooser(sharingIntent, context.getResources().getString(R.string.share));
        finalIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(finalIntent);
    }

    public static void share(Context context, String text) {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_TEXT, text);

        Intent finalIntent = Intent.createChooser(sharingIntent, context.getResources().getString(R.string.share));
        finalIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(finalIntent);
    }

    public void showSearchFragment(FragmentLosungenListe fragment) {
        searchResultShown = true;
        toolbar.setTitle(getResources().getString(R.string.search_results));

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_content, fragment)
                .commit();

        PrimaryDrawerItem item = new PrimaryDrawerItem().withName(R.string.search_results)
                .withIconTintingEnabled(true)
                .withIcon(R.drawable.ic_action_search);

        navigationDrawer.addItemAtPosition(item, 2);

        navigationDrawer.setSelection(item);
    }

    private boolean firstStart() {
        long lastStart = settings.getLong(Tags.TAG_LASTSTART, 0);

        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(Tags.TAG_LASTSTART, System.currentTimeMillis());
        editor.apply();

        boolean importiert = !settings.getString(Tags.PREF_IMPORTS, "---").equals("---");

        if (!(lastStart == 0 || !importiert)) {
            List<String> years = Arrays.asList(Tags.getImport(settings.getString(Tags.SELECTED_LANGUAGE, "en")));
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);

            List<String> schonImportiert = Arrays.asList(settings.getString(Tags.PREF_IMPORTS, " ").split(","));

            if (!schonImportiert.contains(String.valueOf(year))) {
                if (years.contains(String.valueOf(year))) {
                    dialogImport();
                }
            }

        }

        return (lastStart == 0 || !importiert);
    }

    private void firstInit() {
        firstStart = true; //Damit bei OnResume Losungen im Navigation Drawer ausgewählt wird

        startActivity(new Intent(this, MyIntro.class));

    }

    private void dialogImport() {
        final Runnable restart = new Runnable() {
            @Override
            public void run() {
                Intent refresh = new Intent(MainActivity.this, MainActivity.class);
                startActivity(refresh);
                finish();
            }
        };

        ImportLosungenDialog dialog = new ImportLosungenDialog(this, false, restart);
        dialog.show();
    }

    private void setupNavigationDrawer() {
        new DrawerBuilder().withActivity(this).build();

        //if you want to update the items at a later time it is recommended to keep it in a variable
        itemLosung = new PrimaryDrawerItem().withName(R.string.losungen)
                .withIconTintingEnabled(true)
                .withIcon(R.drawable.ic_berichte);
        final PrimaryDrawerItem itemMonatslosung = new PrimaryDrawerItem().withName(R.string.monthly_verses)
                .withIconTintingEnabled(true)
                .withIcon(R.drawable.ic_berichte);
        final PrimaryDrawerItem itemFavoriten = new PrimaryDrawerItem().withName(R.string.favoriten)
                .withIconTintingEnabled(true)
                .withIcon(R.drawable.ic_action_favorite);
        final PrimaryDrawerItem itemWidget = new PrimaryDrawerItem().withName(R.string.widget)
                .withIconTintingEnabled(true)
                .withIcon(R.drawable.ic_image_palette);
        final PrimaryDrawerItem itemEinstellungen = new PrimaryDrawerItem().withName(R.string.settings)
                .withIconTintingEnabled(true)
                .withIcon(R.drawable.ic_settings_white_24dp)
                .withSelectable(false);
        final PrimaryDrawerItem itemBewerten = new PrimaryDrawerItem().withName(R.string.rate)
                .withIconTintingEnabled(true)
                .withIcon(R.drawable.ic_action_star)
                .withSelectable(false);
        final PrimaryDrawerItem itemFeedeback = new PrimaryDrawerItem().withName(R.string.feedback)
                .withIconTintingEnabled(true)
                .withIcon(R.drawable.ic_action_email)
                .withSelectable(false);
        final PrimaryDrawerItem itemInfo = new PrimaryDrawerItem().withName(R.string.info)
                .withIcon(R.drawable.ic_info_black_24dp)
                .withIconTintingEnabled(true);
        final PrimaryDrawerItem privacyItem = new PrimaryDrawerItem().withName(R.string.privacy_policy)
                .withIcon(R.drawable.ic_info_black_24dp) // TODO change icon
                .withIconTintingEnabled(true);

        //create the drawer and remember the `Drawer` result object
        navigationDrawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .addDrawerItems(
                        itemLosung,
                        itemMonatslosung,
                        itemFavoriten,
                        itemWidget,
                        itemEinstellungen,
                        new DividerDrawerItem(),
                        itemBewerten,
                        itemFeedeback,
                        itemInfo,
                        privacyItem
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

                        if (drawerItem.equals(itemLosung)) {
                            if (searchResultShown) {
                                navigationDrawer.removeItemByPosition(2);
                                searchResultShown = false;
                            }

                            toolbar.setTitle(R.string.losungen);

                            getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.fragment_content, FragmentLosung.newInstance(MainActivity.this))
                                    .commit();

                        } else if (drawerItem.equals(itemMonatslosung)) {
                            if (searchResultShown) {
                                navigationDrawer.removeItemByPosition(2);
                                searchResultShown = false;
                            }

                            toolbar.setTitle(R.string.monthly_verses);

                            getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.fragment_content, FragmentMonth.newInstance(MainActivity.this))
                                    .commit();

                        } else if (drawerItem.equals(itemFavoriten)) {
                            if (searchResultShown) {
                                navigationDrawer.removeItemByPosition(2);
                                searchResultShown = false;
                            }

                            toolbar.setTitle(R.string.favoriten);

                            getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.fragment_content, FragmentLosungenListe.newInstance(MainActivity.this, true))
                                    .commit();

                        } else if (drawerItem.equals(itemWidget)) {
                            toolbar.setTitle(R.string.widget);

                            List<Integer> widgetIds = new ArrayList<>();

                            AppWidgetManager mAppWidgetManager = AppWidgetManager.getInstance(MainActivity.this);
                            int[] ids = mAppWidgetManager.getAppWidgetIds(new ComponentName(MainActivity.this, WidgetBroadcast.class));

                            for (int i = 0; i < ids.length; i++) {
                                widgetIds.add(ids[i]);
                            }

                            getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.fragment_content, FragmentWidgets.newInstance(widgetIds))
                                    .commit();

                        } else if (drawerItem.equals(itemEinstellungen)) {

                            startActivity(new Intent(view.getContext(), SettingsActivity.class));

                        } else if (drawerItem.equals(itemBewerten)) {

                            Uri uri = Uri.parse("market://details?id=" + MainActivity.this.getPackageName());
                            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                            try {
                                startActivity(goToMarket);
                            } catch (ActivityNotFoundException e) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + MainActivity.this.getPackageName())));
                            }

                        } else if (drawerItem.equals(itemFeedeback)) {

                            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                                    "mailto", "schalter.dev@gmail.com", null));
                            intent.putExtra(Intent.EXTRA_SUBJECT, "Losungen - APP Feedback");
                            //intent.putExtra(Intent.EXTRA_TEXT, message);
                            startActivity(Intent.createChooser(intent, getResources().getString(R.string.mail_senden)));

                        } else if (drawerItem.equals(itemInfo)) {
                            if (searchResultShown) {
                                navigationDrawer.removeItemByPosition(2);
                                searchResultShown = false;
                            }

                            toolbar.setTitle(R.string.info);

                            getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.fragment_content, FragmentInfo.newInstance())
                                    .commit();
                        } else if (drawerItem.equals(privacyItem)) {
                            // open privacy website
                            String url =  getResources().getString(R.string.privacy_url);
                            Uri uri = Uri.parse(url);
                            startActivity(new Intent(Intent.ACTION_VIEW, uri));
                        }

                        return false;
                    }
                })
                .build();

        if (!firstStart)
            navigationDrawer.setSelection(itemLosung, true);
    }

    @Override
    public void refreshMonthFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_content, FragmentMonth.newInstance(MainActivity.this))
                .commit();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == LOG_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            CustomLog.exportLogToFile(this, uri);
        }

        if (requestCode == FILE_CODE && resultCode == Activity.RESULT_OK) {
            if (data.getBooleanExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false)) {
                // For JellyBean and above
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    ClipData clip = data.getClipData();

                    if (clip != null) {
                        for (int i = 0; i < clip.getItemCount(); i++) {
                            Uri uri = clip.getItemAt(i).getUri();
                            exportNotes(uri);
                        }
                    }
                    // For Ice Cream Sandwich
                } else {
                    ArrayList<String> paths = data.getStringArrayListExtra
                            (FilePickerActivity.EXTRA_PATHS);

                    if (paths != null) {
                        for (String path : paths) {
                            Uri uri = Uri.parse(path);
                            exportNotes(uri);
                        }
                    }
                }

            } else {
                Uri uri = data.getData();
                exportNotes(uri);
            }
        } else if (requestCode == IMPORT_CODE && resultCode == Activity.RESULT_OK) {
            if (data.getBooleanExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false)) {
                // For JellyBean and above
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    ClipData clip = data.getClipData();

                    if (clip != null) {
                        for (int i = 0; i < clip.getItemCount(); i++) {
                            Uri uri = clip.getItemAt(i).getUri();
                            importNotes(uri);
                        }
                    }
                // For Ice Cream Sandwich
                } else {
                    ArrayList<String> paths = data.getStringArrayListExtra
                            (FilePickerActivity.EXTRA_PATHS);

                    if (paths != null) {
                        for (String path : paths) {
                            Uri uri = Uri.parse(path);
                            importNotes(uri);
                        }
                    }
                }

            } else {
                Uri uri = data.getData();
                importNotes(uri);
            }
        }
    }

    private void exportNotes(final Uri uri) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage(getResources().getString(R.string.exporting));
        dialog.show();

        Thread export = new Thread(new Runnable() {
            @Override
            public void run() {
                DBHandler dbHandler = DBHandler.newInstance(MainActivity.this);
                ArrayList<String[]> map = dbHandler.getAllNotes();

                ArrayList<String> tags = new ArrayList<>();
                ArrayList<String> values = new ArrayList<>();

                tags.add(XmlWriter.STARTTAG);
                values.add("LosungenNotizen");
                for (int i = 0; i < map.size(); i++) {
                    String key = String.valueOf(map.get(i)[0]);
                    String value = String.valueOf(map.get(i)[1]);

                    tags.add(XmlWriter.STARTTAG);
                    values.add("Losungen");
                    tags.add(XmlWriter.STARTTAG);
                    values.add("Datum");
                    tags.add(XmlWriter.TEXT);
                    values.add(Losung.getDatumForXml(Long.valueOf(key)));
                    tags.add(XmlWriter.ENDTAG);
                    values.add("Datum");

                    tags.add(XmlWriter.STARTTAG);
                    values.add("Notizen");
                    tags.add(XmlWriter.TEXT);
                    values.add(value);
                    tags.add(XmlWriter.ENDTAG);
                    values.add("Notizen");
                    tags.add(XmlWriter.ENDTAG);
                    values.add("Losungen");
                }
                tags.add(XmlWriter.ENDTAG);
                values.add("LosungenNotizen");

                XmlWriter xmlWriter = new XmlWriter();
                xmlWriter.setData(tags, values);

                File dir = new File(uri.getPath());

                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat df1 = new SimpleDateFormat("dd-MM-yyyy", Locale.GERMAN);
                String formattedDate = df1.format(calendar.getTime());

                final File file = new File(dir, "notes" + formattedDate + ".xml");

                xmlWriter.writeXml(file);

                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        dialog.cancel();
                        MainActivity.toast(MainActivity.this,
                                getResources().getString(R.string.exported_to) + file.getPath(),
                                Toast.LENGTH_LONG);
                    }
                });
            }
        });
        export.start();
    }

    private void exportNotes() {
        // This always works
        Intent i = new Intent(this, FilePickerActivity.class);
        // This works if you defined the intent filter
        // Intent i = new Intent(Intent.ACTION_GET_CONTENT);

        // Set these depending on your use case. These are the defaults.
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
        i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);

        // Configure initial directory by specifying a String.
        // You could specify a String like "/storage/emulated/0/", but that can
        // dangerous. Always use Android's API calls to get paths to the SD-card or
        // internal memory.
        i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());

        startActivityForResult(i, FILE_CODE);
    }

    private void importNotes() {
        // This always works
        Intent i = new Intent(this, FilePickerActivity.class);
        // This works if you defined the intent filter
        // Intent i = new Intent(Intent.ACTION_GET_CONTENT);

        // Set these depending on your use case. These are the defaults.
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
        i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);

        // Configure initial directory by specifying a String.
        // You could specify a String like "/storage/emulated/0/", but that can
        // dangerous. Always use Android's API calls to get paths to the SD-card or
        // internal memory.
        i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());

        startActivityForResult(i, IMPORT_CODE);
    }

    private void importNotes(final Uri uri) {
        //TODO check if FILE is right parse FILE and update to database
        File importFile = new File(uri.getPath());
        XmlNotesImport xmlImport = new XmlNotesImport();
        boolean success = xmlImport.parseXML(importFile);
        if(!success)
            MainActivity.toast(this, getResources().getString(R.string.import_failed),
                    Toast.LENGTH_LONG);
        else {
            ArrayList<String[]> notes = xmlImport.getNotes();
            DBHandler dbHandler = DBHandler.newInstance(this);
            dbHandler.importNotes(notes, false);
            MainActivity.toast(this, getResources().getString(R.string.import_success),
                    Toast.LENGTH_LONG);

            Intent refresh = new Intent(MainActivity.this, MainActivity.class);
            startActivity(refresh);
            finish();
        }
    }

    public static void runOnMainThread(Runnable run) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(run);
    }

}