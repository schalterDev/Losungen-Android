package de.schalter.losungen.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.schalter.losungen.R;
import de.schalter.losungen.files.ImportLosungenIntoDB;
import de.schalter.losungen.settings.Tags;
import schalter.dev.customizelibrary.Colors;

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
    private List<Integer> indexAlreadyImportedLanguageIndependen;

    private Runnable ifFinished;

    private String selectedLanguage;

    /**
     * gets language, possible imports, ... from the settings and asks the user
     * what to import
     * @param context application import
     */
    public ImportLosungenDialog(Context context, boolean chooseLanguage, Runnable ifFinished) {
        settings = PreferenceManager.getDefaultSharedPreferences(context);

        this.context = context;
        this.ifFinished = ifFinished;

        loadDialog(chooseLanguage);
    }

    /**
     * prepares the dialog for showing
     * @param chooseLanguage true if the user should choose between different langauges
     */
    private void loadDialog(boolean chooseLanguage) {
        // get acutal langauge
        final String language = settings.getString(Tags.SELECTED_LANGUAGE, Locale.getDefault().getLanguage());
        final String[] itemsCountry = context.getResources().getStringArray(R.array.country_values);
        int countrySelected = Arrays.asList(itemsCountry).indexOf(language);
        if(countrySelected == -1)
            countrySelected = Arrays.asList(itemsCountry).indexOf("en");

        // get dialog_import.xml view
        LayoutInflater li = LayoutInflater.from(context);
        View dialogView = li.inflate(R.layout.dialog_import, null);
        builder = new AlertDialog.Builder(context);

        final LinearLayout linearLayout = (LinearLayout) dialogView.findViewById(R.id.linear_layout_import);

        // set dialog_import.xml to alertdialog builder
        builder.setView(dialogView);

        if(!chooseLanguage) {
            //remove Spinner and text from layout
            dialogView.findViewById(R.id.text_before_spinner).setVisibility(View.INVISIBLE);
            dialogView.findViewById(R.id.spinner_language).setVisibility(View.INVISIBLE);

            setupDialogForLangauge(itemsCountry[countrySelected], linearLayout);
        }

        //  -------------for language chooser ----------------

        // set dialog_import.xml to alertdialog builder
        builder.setView(dialogView);

        final Spinner spinner = (Spinner) dialogView.findViewById(R.id.spinner_language);
        final TextView notes = (TextView) dialogView.findViewById(R.id.textView_import_notes);

        // -------------- SPINNER --------------

        spinner.setSelection(countrySelected);
        final String[] selectedCountry = {itemsCountry[countrySelected]};


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCountry[0] = itemsCountry[position];

                notes.setText(getHintsForLanguage(context, selectedCountry[0]));

                setupDialogForLangauge(itemsCountry[position], linearLayout);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        // ------------ END SPINNER --------------

        // ------------ end language choose -------------------

        builder.setPositiveButton(R.string.importVerb, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                importClick();
            }
        });

        builder.setNegativeButton(R.string.cancel, null);

        builder.setCancelable(true);
    }

    /**
     * Loads all important data for this language, and will add checkboxes to the linearLayout
     * @param language which is schoosen by the user
     * @param linearLayout with the checkboxes
     */
    private void setupDialogForLangauge(String language, LinearLayout linearLayout) {
        selectedLanguage = language;

        String[] years = getYearsForLangugae(language);
        indexAlreadyImported = getYearsAlreadyImported(language, Arrays.asList(years));

        final List<Integer> yearsHaveToImport = whichLanguagesHaveToBeImported(years);
        String[] itemsToImportPossible = getYearsForLangugae(language);

        itemsToDisplay = years.clone();
        checkedItems = new boolean[itemsToImportPossible.length];

        for(int i = 0; i < checkedItems.length; i++) {
            if(yearsHaveToImport.contains(i) && !indexAlreadyImportedLanguageIndependen.contains(i)) {
                checkedItems[i] = true;
            }
        }

        //check if the losungen have to be downloaded first
                /*if(Tags.hasToBeDownloaded(selectedCountry[0], Integer.valueOf(itemsoriginal[position]))) {
                    LosungenDownload losungenDownloadDialog = new LosungenDownload(Integer.valueOf(itemsoriginal[position]), selectedCountry[0]);
                    losungenDownloadDialog.openDialog(context);
                }*/

        initialCheckboxes(context, linearLayout, years, checkedItems, yearsHaveToImport,
                new CheckboxChanged() {
                    @Override
                    public void onCheckedChange(boolean isChecked, int index) {
                        checkedItems[index] = isChecked;
                    }
                });
    }

    public void show() {
        builder.show();
    }

    /**
     * Will be called when the user accepted to import the losugnen for the selected years
     */
    private void importClick() {
        List<String> importsArray = new ArrayList<>();
        final List<String> years = new ArrayList<>();
        String imports = "";
        boolean first = true;

        final List<Integer> indexUpdate = new ArrayList<>();

        //Write imports into SharedPreferences
        for (int i = 0; i < itemsToDisplay.length; i++) {
            if (checkedItems[i]) {
                if (first) {
                    first = false;
                    imports += itemsToDisplay[i];
                } else {
                    imports += "," + itemsToDisplay[i];
                }

                if (!indexAlreadyImported.contains(i)) {
                    importsArray.add(selectedLanguage + "/" + itemsToDisplay[i]);
                    years.add(itemsToDisplay[i]);
                }

                if(indexAlreadyImportedLanguageIndependen.contains(i)) {
                    indexUpdate.add(importsArray.size() - 1);
                }
            }
        }

        SharedPreferences.Editor editor = settings.edit();
        editor.putString(Tags.PREF_IMPORTS, imports);
        editor.putString(Tags.SELECTED_LANGUAGE, selectedLanguage);
        editor.apply();

        Runnable afterYears = new Runnable() {
            @Override
            public void run() {
                ImportLosungenIntoDB.importMonthAndWeeks(context, selectedLanguage, years, ifFinished, indexUpdate);

            }
        };

        //not for month or week and dont update db
        ImportLosungenIntoDB.importLosungenFromAssets(context, importsArray, afterYears, indexUpdate, false, false);

    }

    /**
     * Get all available years for this langauge
     * @param language language
     * @return all available years for import for hits year
     */
    private String[] getYearsForLangugae(String language) {
        return Tags.getImport(language);
    }

    private List<Integer> getYearsAlreadyImported(String language, List<String> years) {
        String importedLanguage = settings.getString(Tags.SELECTED_LANGUAGE, "en");
        List<Integer> indexAlreadyImported = new ArrayList<>();

        String alreadyImportedFromSettings = settings.getString(Tags.PREF_IMPORTS, " ");
        String[] alreadyImportedArray = alreadyImportedFromSettings.split(",");

        // Compare every imported years with the years given in the parameter
        if (!(alreadyImportedArray.length == 1 && alreadyImportedArray[0].equals(" "))) {
            for (String importedYear : alreadyImportedArray) {
                indexAlreadyImported.add(years.indexOf(importedYear));
            }
        }

        indexAlreadyImportedLanguageIndependen = indexAlreadyImported;

        if(language.equals(importedLanguage)) {
            return indexAlreadyImported;
        } else {
            // the losungen imported are not the same langauge
            return new ArrayList<>();
        }

    }

    /**
     * Returns all indexes of the years that have to be imported that the app can be used
     * @param years all years that are possible to import
     * @return list of indexes that have to be imported
     */
    private List<Integer> whichLanguagesHaveToBeImported(String[] years) {
        List<Integer> result = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);

        for(int i = 0; i < years.length; i++) {
            try {
                int element = Integer.parseInt(years[i]);
                if(element == year)
                    result.add(i);
            } catch (NumberFormatException e) {
                Log.e("Losungen", "Not a year in years-Array in ImportLosungen");
            }
        }

        return result;
    }

    /**
     * Removes all checkboxes from the layout and adds for every year one with
     * the right intital values
     * @param context application context for creating new checkboxes
     * @param linearLayout containing existing / new checkboxes
     * @param years every year will have one checkbox
     * @param checked which checkboxes are already checked from beginning
     * @param checkboxesWithFixedValue all indexes of checkboxes that can not be changed
     * @param checkboxChanged listener that is called when a checkbox is checked and value changed
     */
    private void initialCheckboxes(Context context, LinearLayout linearLayout,
                                   String[] years, boolean[] checked,
                                   final List<Integer> checkboxesWithFixedValue,
                                   final CheckboxChanged checkboxChanged) {
        final List<CheckBox> checkBoxes = new ArrayList<>();
        linearLayout.removeAllViews();

        CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                for(int i = 0; i < checkBoxes.size(); i++) {
                    if(buttonView == checkBoxes.get(i)) {
                        if(checkboxesWithFixedValue.contains(i) && !indexAlreadyImportedLanguageIndependen.contains(i) && !isChecked) {
                            checkBoxes.get(i).setChecked(true);
                        } else {
                            checkboxChanged.onCheckedChange(isChecked, i);
                        }
                    }
                }
            }
        };

        //create new checkboxes
        for(int i = 0; i < itemsToDisplay.length; i++) {
            CheckBox checkBox = new CheckBox(context);
            checkBox.setText(years[i]);
            checkBox.setHighlightColor(Colors.getColor(context, Colors.ACCENT));
            checkBox.setChecked(checked[i]);

            checkBox.setOnCheckedChangeListener(onCheckedChangeListener);

            checkBoxes.add(checkBox);

            linearLayout.addView(checkBox);
        }
    }

    /**
     * For some langauges there has to be a note to be visible
     * @param context application context
     * @param country the country
     * @return the coutnry specific hints
     */
    private String getHintsForLanguage(Context context, String country) {
        String notes;

        if(!country.equals("de")) {
            notes = context.getResources().getString(R.string.import_maybeMistakes);
        } else {
            notes =  "";
        }

        if(!Arrays.asList(Tags.SUPPORTED_LANGUAGES).contains(country)) {
            if(notes.length() > 2) {
                notes += System.getProperty("line.separator") + System.getProperty("line.separator");
            }

            notes += context.getResources().getString(R.string.needTranslation);
        }

        return notes;
    }

    private interface CheckboxChanged {
        public void onCheckedChange(boolean isChecked, int index);
    }

}
