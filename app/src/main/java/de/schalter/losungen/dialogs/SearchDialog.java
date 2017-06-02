package de.schalter.losungen.dialogs;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.List;

import de.schalter.losungen.AnalyticsApplication;
import de.schalter.losungen.Losung;
import de.schalter.losungen.MainActivity;
import de.schalter.losungen.R;
import de.schalter.losungen.files.DBHandler;
import de.schalter.losungen.fragments.FragmentLosungenListe;
import de.schalter.losungen.settings.Tags;

/**
 * Created by marti on 30.10.2015.
 */
public class SearchDialog {

    private AlertDialog.Builder builder;
    private MainActivity activity;

    private EditText editText;
    private CheckBox check_losung;
    private CheckBox check_losungVers;
    private CheckBox check_lehrtext;
    private CheckBox check_lehrtextVers;
    private CheckBox check_notizen;
    private CheckBox check_markierte;
    private LinearLayout linearLayout_years;

    private SharedPreferences settings;

    /**
     * Sends a Action to Google Analytics
     * @param fav will this open the fovorite fragment (or search fragment)
     *            in this case only false
     */
    private void analytics(boolean fav) {
        if(settings.getBoolean(Tags.PREF_GOOGLEANALYTICS, true)) {
            // Obtain the shared Tracker instance.
            AnalyticsApplication application = (AnalyticsApplication) activity.getApplication();
            Tracker mTracker = application.getDefaultTracker();

            if(fav)
                mTracker.setScreenName("Fragment-Fav");
            else
                mTracker.setScreenName("Fragment-Suchen");

            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }

    public SearchDialog(MainActivity activity) {
        this.activity = activity;
        settings = PreferenceManager.getDefaultSharedPreferences(activity);

        LayoutInflater inflater = LayoutInflater.from(activity);
        final View view = inflater.inflate(R.layout.dialog_search, null);

        editText = (EditText) view.findViewById(R.id.editText);
        check_losung = (CheckBox) view.findViewById(R.id.checkBox_losung);
        check_losungVers = (CheckBox) view.findViewById(R.id.checkBox_losungsverse);
        check_lehrtext = (CheckBox) view.findViewById(R.id.checkBox_lehretext);
        check_lehrtextVers = (CheckBox) view.findViewById(R.id.checkBox_lehrtextvers);
        check_notizen = (CheckBox) view.findViewById(R.id.checkBox_notizen);
        check_markierte = (CheckBox) view.findViewById(R.id.checkBox_markiert);
        linearLayout_years = (LinearLayout) view.findViewById(R.id.linear_layout_years);

        // ---- YEARS ----
        //allYears will not be modified, it contains all imported years
        final List<Integer> allYears = new ArrayList<>();
        //will only contain the selected years
        final List<Integer> years = new ArrayList<>();

        //Get imported years (saved like this "2015,2016")
        //And save them into the to lists
        String yearsSetting = settings.getString(Tags.PREF_IMPORTS, " ");
        if(!yearsSetting.equals(" ")) {
            String[] yearsStringArray = yearsSetting.split(",");

            for (String aYearsStringArray : yearsStringArray) {
                years.add(Integer.valueOf(aYearsStringArray));
                allYears.add(Integer.valueOf(aYearsStringArray));
            }
        }

        //LayoutParams for every CheckBox
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        for(int i = 0; i < allYears.size(); i++) {
            CheckBox switchYear = new CheckBox(activity);
            switchYear.setText(String.valueOf(allYears.get(i)));
            switchYear.setChecked(true);

            final int finalI = i;
            switchYear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //If the selected year is in the list: remove
                    if(years.contains(allYears.get(finalI)))
                        years.remove(allYears.get(finalI));
                    else //if not: add
                        years.add(allYears.get(finalI));
                }
            });

            switchYear.setLayoutParams(params);
            linearLayout_years.addView(switchYear);
        }
        // ---- YEARS END ---

        builder = new AlertDialog.Builder(activity);
        builder.setTitle(activity.getResources().getString(R.string.suchen_dialog_title));
        builder.setCancelable(false); //there is a cancel button
        builder.setView(view);

        builder.setPositiveButton(R.string.suchen_dialog_positiv_button,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();

                        suchen(editText.getText().toString(), check_losung.isChecked(),
                                check_lehrtext.isChecked(), check_losungVers.isChecked(),
                                check_lehrtextVers.isChecked(), check_notizen.isChecked(),
                                check_markierte.isChecked(), years);
                    }
                });

        builder.setNegativeButton(R.string.suchen_dialog_negative_button,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
    }

    public void show() {
        builder.show();
    }

    /**
     * Will get all relevant entrys from files and open a fragment and list them there
     * @param text the entry of the search text-box
     * @param losungen search in the verses (only the text) for new testament
     * @param lehrtexte search in the verses (only the text) for old testament
     * @param losungsVerse search in the verses (not the text, only name of vers, e.g. Matthew 1,1) for new testament
     * @param lehrtextVerse search in the verses (not the text, only name of vers e.g. 1. Mose 2,1) for old testament
     * @param notizen search in the notes
     * @param nurMarkierte only marked days
     * @param years a list of all selected years
     */
    //TODO add monthly and weekly verses to search
    private void suchen(String text, boolean losungen,
                        boolean lehrtexte, boolean losungsVerse,
                        boolean lehrtextVerse, boolean notizen,
                        boolean nurMarkierte, List<Integer> years) {

        DBHandler dbHandler = DBHandler.newInstance(activity);
        List<Losung> losungenList = dbHandler.suchen(text, losungen, lehrtexte, losungsVerse, lehrtextVerse, notizen, nurMarkierte, years);

        //Send action to google analytics
        analytics(false);

        activity.showSearchFragment(FragmentLosungenListe.newInstance(losungenList));
    }
}
