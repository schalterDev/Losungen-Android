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

import de.schalter.losungen.R;

/**
 * Created by martin on 27.11.16.
 */

public class PetitionDialog {

    private static final String url = "https://www.openpetition.de/petition/online/losungen-der-herrnhuter-bruedergemeine-apps-fuer-mobile-endgeraete";
    public static final String PREFERENCE_CHECKBOX = "petition_show_again";

    private SharedPreferences settings;

    private Context context;
    private AlertDialog.Builder builder;
    private AlertDialog dialog;

    public PetitionDialog(final Context context, int titleResource, int checkBoxResource,
                          int messageResource, int positiveButton) {

        this.context = context;

        builder = new AlertDialog.Builder(context);
        builder.setTitle(titleResource);
        builder.setCancelable(true);
        builder.setMessage(messageResource);

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Uri uri = Uri.parse(url);
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    context.startActivity(goToMarket);
                } catch (ActivityNotFoundException ignored) {

                }
            }
        });

        addCheckbox(checkBoxResource);

        dialog = builder.create();

    }

    private void addCheckbox(int checkboxResource) {
        settings = PreferenceManager.getDefaultSharedPreferences(context);

        //CheckBox
        View checkBoxView = View.inflate(context, R.layout.list_with_checkbox, null);
        CheckBox checkBox = checkBoxView.findViewById(R.id.checkbox);

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(PREFERENCE_CHECKBOX, isChecked);
                editor.apply();
            }
        });

        checkBox.setText(checkboxResource);

        builder.setView(checkBoxView);
    }

    public void show() {
        dialog.show();
    }

}
