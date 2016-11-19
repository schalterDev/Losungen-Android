package de.schalter.losungen.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import de.schalter.losungen.MainActivity;
import de.schalter.losungen.R;
import de.schalter.losungen.files.ImportLosungenIntoDB;
import de.schalter.losungen.settings.Tags;

/**
 * Created by martin on 15.11.16.
 */

public class ImportLosungenDialog {

    private Context context;
    private AlertDialog.Builder builder;
    private SharedPreferences settings;

    private  String[] itemsToDisplay;
    private boolean[] checkedItems;
    private List<Integer> indexAlreadyImported;

    /**
     * gets language, possible imports, ... from the settings and asks the user
     * what to import
     * @param context application import
     */
    public ImportLosungenDialog(Context context, boolean chooseLanguage, Runnable ifFinished) {
        //TODO chooseLanguage
        this.context = context;

        settings = PreferenceManager.getDefaultSharedPreferences(context);

        indexAlreadyImported = new ArrayList<>();
        List<Integer> indexHaveToImport = new ArrayList<>();

        String language = settings.getString(Tags.SELECTED_LANGUAGE, "en");
        String[] itemsToImportPossible = Tags.getImport(language);

        List<String> itemsToImportPossibleList = Arrays.asList(itemsToImportPossible);

        String alreadyImported = settings.getString(Tags.PREF_IMPORTS, " ");
        String[] alreadyImportedArray = alreadyImported.split(",");
        if (!(alreadyImportedArray.length == 1 && alreadyImportedArray[0].equals(" "))) {
            for (String importedYear : alreadyImportedArray) {
                indexAlreadyImported.add(itemsToImportPossibleList.indexOf(importedYear));
            }
        }

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        String yearString = String.valueOf(year);
        int yearPos = itemsToImportPossibleList.indexOf(yearString);

        if (!indexAlreadyImported.contains(yearPos))
            indexHaveToImport.add(yearPos);

        loadDialog(itemsToImportPossible, indexAlreadyImported,
                indexHaveToImport, chooseLanguage, ifFinished);
    }

    /**
     * Asks the user which years he want to import
     *
     * @param context application context
     * @param itemsToImportPossible all years that can be imported
     * @param indexAlreadyImported index of all years that are already imported
     * @param indexHaveToImport index of all years that have to be imported (because it's the actual year)
     * @param ifFinished will be called after all imports
     */
    public ImportLosungenDialog(Context context, String[] itemsToImportPossible,
                                List<Integer> indexAlreadyImported,
                                List<Integer> indexHaveToImport, Runnable ifFinished,
                                boolean chooseLanguage) {

        //TODO chooseLanguage

        this.context = context;

        loadDialog(itemsToImportPossible, indexAlreadyImported,
                indexHaveToImport, chooseLanguage, ifFinished);
    }

    /**
     * prepares the dialog
     */
    private void loadDialog(String[] itemsToImportPossible, final List<Integer> indexAlreadyImported,
                            final List<Integer> indexHaveToImport, boolean chooseLanguage,
                            Runnable ifFinished) {

        //TODO ifFinised and chooseLanguage

        itemsToDisplay = itemsToImportPossible.clone();
        checkedItems = new boolean[itemsToImportPossible.length];

        for(int i = 0; i < itemsToImportPossible.length; i++) {
            if(indexAlreadyImported.contains(i)) {
                // this year is already imported
                checkedItems[i] = true;
                itemsToDisplay[i] = itemsToDisplay[i] + " " + context.getResources().getString(R.string.allready_imported);

            } else if (indexHaveToImport.contains(i)) {
                // this year is not imported but has to be
                checkedItems[i] = true;

                itemsToDisplay[i] = itemsToDisplay[i] + " " + context.getResources().getString(R.string.haveTo_import);
            } else {
                // this year is not imported but can be
                checkedItems[i] = false;

            }
        }

        builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.importVerb);
        builder.setMultiChoiceItems(itemsToDisplay, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if (indexHaveToImport.contains(which) && !isChecked) {
                    MainActivity.toast(context, "Wird trotzdem importiert", Toast.LENGTH_LONG);
                } else if (indexAlreadyImported.contains(which)) {
                    MainActivity.toast(context, "Ist bereits importiert", Toast.LENGTH_SHORT);
                } else {
                    checkedItems[which] = !checkedItems[which];
                }
            }
        });

        builder.setPositiveButton(R.string.importVerb, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                importClick();
            }
        });

        builder.setCancelable(true);
    }

    public void show() {
        builder.show();
    }

    /**
     * Will be called when the user accepted to import the losugnen for the selected years
     */
    private void importClick() {
        List<String> importsArray = new ArrayList<>();
        String imports = "";
        boolean first = true;
        for (int i = 0; i < itemsToDisplay.length; i++) {
            if (checkedItems[i]) {
                if (first) {
                    first = false;
                    imports += itemsToDisplay[i];
                } else {
                    imports += "," + itemsToDisplay[i];
                }

                if (!indexAlreadyImported.contains(i))
                    importsArray.add(settings.getString(Tags.SELECTED_LANGUAGE, "en") + "/" + itemsToDisplay[i]);
            }
        }

        SharedPreferences.Editor editor = settings.edit();
        editor.putString(Tags.PREF_IMPORTS, imports);
        editor.apply();

        //not for month or week and dont update db
        ImportLosungenIntoDB.importLosungenFromAssets(context, importsArray, null, false, false, false);
        //TODO import months and weeks
    }

    /*
    private void checkBoxChanged(String language, int year, boolean isChecked) {
        if (indexHaveToImport.contains(which) && !isChecked) {
            MainActivity.toast(context, "Wird trotzdem importiert", Toast.LENGTH_LONG);
        } else if (indexAlreadyImported.contains(which)) {
            MainActivity.toast(context, "Ist bereits importiert", Toast.LENGTH_SHORT);
        } else {
            checkedItems[which] = !checkedItems[which];
        }
    }*/



}
