package de.schalter.losungen.intro;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;

import de.schalter.losungen.R;
import de.schalter.losungen.dialogs.ChooseDialog;
import de.schalter.losungen.settings.Tags;

/**
 * Created by marti on 04.11.2015.
 */

public class MyIntro extends AppIntro {

    private static MyIntro myIntro;
    private SharedPreferences settings;

    @Override
    public void init(Bundle savedInstanceState) {
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        myIntro = this;
        // Add your slide's fragments here
        // AppIntro will automatically generate the dots indicator and buttons.
        addSlide(FragmentIntroSwitch.newInstance(getString(R.string.intro_notifications), getString(R.string.intro_notification_content),
                Color.parseColor(/*"#0c90ff"*/"#de4e00"), R.drawable.intro_notification, getString(R.string.intro_notification_switch), Tags.PREF_NOTIFICATION, true)); //Benachrichtigungen
        addSlide(FragmentIntroSwitch.newInstance(getString(R.string.intro_mark_title).toUpperCase(), getString(R.string.intro_mark_content),
                Color.parseColor(/*"#1cbd14"*/"#de4e00"), R.drawable.intro_mark, getString(R.string.intro_mark_switch), Tags.PREF_SHOWNOTES, true)); //Markieren + Notizen
        addSlide(FragmentIntroSwitch.newInstance(getString(R.string.intro_developer_title), getString(R.string.intor_developer_content),
                /*getResources().getColor(R.color.accent)*/Color.parseColor("#de4e00"), R.drawable.intro_ads, getString(R.string.intro_developer_switch), Tags.PREF_ADS, false));
        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_widgets_title),
                getString(R.string.intro_widgets_content),
                R.drawable.intro_widget, Color.parseColor("#de4e00"))); //Widgets

        // Instead of fragments, you can also use our default slide
        // Just set a title, description, background and image. AppIntro will do the rest
        //addSlide(AppIntroFragment.newInstance(title, description, image, background_colour));

        // OPTIONAL METHODS
        // Override bar/separator color
        //setBarColor(Color.parseColor("#3F51B5"));
        //setSeparatorColor(Color.parseColor("#2196F3"));

        // Hide Skip/Done button
        showSkipButton(false);
        showDoneButton(false);

        // Turn vibration on and set intensity
        // NOTE: you will probably need to ask VIBRATE permesssion in Manifest
        setVibrate(false);
        //setVibrateIntensity(30);

        dialogImport();
    }

    private void dialogImport() {
        Runnable run = new Runnable() {
            @Override
            public void run() {
                MyIntro.contentLoaded();
            }
        };

        ChooseDialog dialog = new ChooseDialog();
        dialog.importLosungenMitSprache(this, run);

        /*List<Integer> schonImport = new ArrayList<>();
        List<Integer> mussImport = new ArrayList<>();

        String[] items = Tags.IMPORT_LIST.clone();
        List<String> itemsList = Arrays.asList(items);

        String imports = settings.getString(Tags.PREF_IMPORTS, " ");
        String[] importsArray = imports.split(",");
        if(!(importsArray.length == 1 && importsArray[0].equals(" "))) {
            for (String anImportsArray : importsArray) {
                schonImport.add(itemsList.indexOf(anImportsArray));
            }
        }

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        String yearString = String.valueOf(year);
        int yearPos = itemsList.indexOf(yearString);

        if(!schonImport.contains(yearPos))
            mussImport.add(yearPos);

        ChooseDialog dialog = new ChooseDialog();
        dialog.importLosungenFromAssets(this, items, schonImport, mussImport, run);*/
    }

    public static void contentLoaded() {
        if(myIntro != null) {
            //myIntro.showSkipButton(true);
            myIntro.showDoneButton(true);
        }

    }

    @Override
    public void onSkipPressed() {
        finish();
    }

    @Override
    public void onDonePressed() {
        finish();
    }
}
