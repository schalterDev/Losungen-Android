package de.schalter.losungen.dialogs;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import java.io.IOException;

import de.schalter.losungen.Losung;
import de.schalter.losungen.MainActivity;
import de.schalter.losungen.R;
import de.schalter.losungen.settings.Tags;

public class LosungOpenDialog {

    private SharedPreferences settings;

    private Context context;

    private boolean saveChoice;

    private String[] items;
    private Losung losung;
    private boolean losungOrLehrtext;

    public LosungOpenDialog(Context context) {
        this.context = context;
        settings = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * The dialog let the user choose between different urls to open
     * @param items the name of the Items to choose (first open in app, second open in browser)
     */
    public void show(final String[] items,
                     final Losung losung, final boolean losungOrLehrtext) {

        this.items = items;
        this.losung = losung;
        this.losungOrLehrtext = losungOrLehrtext;

        int howToOpen = Integer.parseInt(settings.getString(Tags.OPEN_WITH_DEFAULT, "2"));
        switch (howToOpen) {
            case 0:
                showInApp();
                break;
            case 1:
                showInBrowser();
                break;
            case 2:
                showDialog();
                break;
        }


    }

    private void showInApp() {
        //Open in Quick Bible on click
        try {
            BibleDialog bibleDialog = new BibleDialog(context);
            if (losungOrLehrtext)
                bibleDialog.loadVers(losung.getLosungsvers());
            else
                bibleDialog.loadVers(losung.getLehrtextVers());
            bibleDialog.openApp();
        } catch (IOException | ActivityNotFoundException e) {
            MainActivity.toast(context, context.getResources().getString(R.string.open_in_app_failed),
                    Toast.LENGTH_SHORT);
        } catch (NumberFormatException e) {
            MainActivity.toast(context, context.getResources().getString(R.string.cant_parse_number),
                    Toast.LENGTH_LONG);
        }
    }

    private void showInBrowser() {
        //Get the right bible-translation for bibleserver.com
        String uebersetzung = Tags.getUebersetzung(settings.getString(Tags.SELECTED_LANGUAGE, "en"));

        String urlLosung = "http://www.bibleserver.com/text/" + uebersetzung + "/" + losung.getLosungsvers();
        String urlLehrtext = "http://www.bibleserver.com/text/" + uebersetzung + "/" + losung.getLehrtextVers();

        Uri uri;
        if (losungOrLehrtext)
            uri = Uri.parse(urlLosung);
        else
            uri = Uri.parse(urlLehrtext);
        context.startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }

    private void showDialog() {
        saveChoice = false;

        //CheckBox
        View checkBoxView = View.inflate(context, R.layout.list_with_checkbox, null);
        CheckBox checkBox = (CheckBox) checkBoxView.findViewById(R.id.checkbox);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveChoice = isChecked;
            }
        });
        checkBox.setText(context.getResources().getString(R.string.remember_my_decision));

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.open_verse));
        builder.setCancelable(true);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = settings.edit();
                if (saveChoice) {
                    editor.putString(Tags.OPEN_WITH_DEFAULT, String.valueOf(which));
                } else {
                    editor.putString(Tags.OPEN_WITH_DEFAULT, "2");
                }
                editor.apply();

                switch (which) {
                    //Open in APP
                    case 0:
                        showInApp();
                        break;
                    //Open in Browser
                    case 1:
                        showInBrowser();
                        break;
                }
            }
        });

        builder.setView(checkBoxView);

        builder.show();
    }
}
