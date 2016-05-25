package de.schalter.losungen.dialogs;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
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
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.schalter.losungen.MainActivity;
import de.schalter.losungen.R;
import de.schalter.losungen.files.XmlParser;
import de.schalter.losungen.settings.Tags;
import schalter.dev.customizelibrary.Colors;

/**
 * Created by marti on 30.10.2015.
 */
public class ChooseDialog {

    private AlertDialog.Builder builder;

    public ChooseDialog() {

    }

    public void chooseDateDialog(Context context, Calendar calendar, DatePickerDialog.OnDateSetListener listener) {
        DatePickerDialog dialog = new DatePickerDialog(context, listener,
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        dialog.show();
    }

    /**
     * The dialog let the user choose between different urls to open
     * @param items the name of the Items to choose
     * @param urls the url of the Items
     */
    public void openUrlDialog(final Context context, final String[] items, final String[] urls) {
        builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.open_in_browser));
        builder.setCancelable(true);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                openUrl(context, urls[which]);
            }
        });

        builder.show();
    }

    /**
     * The dialog let the user choose between different items to share (simple text share)
     * @param items the name of the items
     * @param titles the title of the items
     * @param inhalte the content of the items
     */
    public void openShareDialog(final Context context, final String[] items, final String[] titles,
                                final String[] inhalte) {
        builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.share));
        builder.setCancelable(true);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MainActivity.share(context, titles[which], inhalte[which]);
            }
        });

        builder.show();
    }

    public void importLosungen(final Context context, String[] items, final List<Integer> schonImport,
                               final List<Integer> mussImport, final Runnable ifFinished) {
        final String[] itemsoriginal = items.clone();
        final boolean[] checkedItemsBool = new boolean[items.length];
        final boolean[] checkedItemsBoolFertig = new boolean[items.length];

        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

        for(int i = 0; i < items.length; i++) {
            if(schonImport.contains(i)) {
                checkedItemsBool[i] = true;
                checkedItemsBoolFertig[i] = true;
                items[i] = items[i] + " " + context.getResources().getString(R.string.allready_imported);
            } else if(mussImport.contains(i)) {
                checkedItemsBool[i] = true;
                checkedItemsBoolFertig[i] = true;
                items[i] = items[i] + " " + context.getResources().getString(R.string.haveTo_import);
            } else {
                checkedItemsBool[i] = false;
                checkedItemsBoolFertig[i] = false;
            }
        }

        builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.importVerb));
        builder.setMultiChoiceItems(items, checkedItemsBool, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if (mussImport.contains(which) && !isChecked) {
                    MainActivity.toast(context, "Wird trotzdem importiert", Toast.LENGTH_LONG);
                } else if (schonImport.contains(which)) {
                    MainActivity.toast(context, "Ist bereits importiert", Toast.LENGTH_SHORT);
                } else {
                    checkedItemsBoolFertig[which] = !checkedItemsBoolFertig[which];
                }
            }
        });

        builder.setPositiveButton(context.getResources().getString(R.string.importVerb), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                List<String> importsArray = new ArrayList<>();
                String imports = "";
                boolean first = true;
                for (int i = 0; i < itemsoriginal.length; i++) {
                    if (checkedItemsBoolFertig[i]) {
                        if (first) {
                            first = false;
                            imports += itemsoriginal[i];
                        } else {
                            imports += "," + itemsoriginal[i];
                        }

                        if (!schonImport.contains(i))
                            importsArray.add(settings.getString(Tags.SELECTED_LANGUAGE, "en") + "/" + itemsoriginal[i]);
                    }
                }

                SharedPreferences.Editor editor = settings.edit();
                editor.putString(Tags.PREF_IMPORTS, imports);
                editor.apply();

                importLosungen(context, importsArray, ifFinished, false, false);
                //TODO import monat und woche
            }
        });

        builder.setCancelable(true);
        builder.show();
    }

    public void changeLanguage(final Context context, final Runnable ifFinished,
                               final boolean monat) {
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

        String imports = settings.getString(Tags.PREF_IMPORTS, " ");
        String[] importsArray = imports.split(",");

        String language = Locale.getDefault().getLanguage();
        items = getJahre(language);
/*
        List<String> newItems = new ArrayList<>();

        //Alle Einträge in items löschen die noch nicht importieret sind
        for(int i = 0; i < items.length; i++) {
            if(Arrays.asList(importsArray).contains(items[i])) {
                newItems.add(items[i]);
            }
        }
        items = newItems.toArray(new String[newItems.size()]);*/

        //Welche Einträge sind von Anfang an ausgewählt
        checkedItemsBool = new boolean[items.length];
        for(int i = 0; i < items.length; i++) {
            checkedItemsBool[i] = false;
        }

        final String[] selectedCountry = new String[1];

        // get dialog_import.xml view
        LayoutInflater li = LayoutInflater.from(context);
        View dialogView = li.inflate(R.layout.dialog_import, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        // set dialog_import.xml to alertdialog builder
        alertDialogBuilder.setView(dialogView);

        final Spinner spinner = (Spinner) dialogView.findViewById(R.id.spinner_language);
        final LinearLayout linearLayout = (LinearLayout) dialogView.findViewById(R.id.linear_layout_import);
        final TextView notes = (TextView) dialogView.findViewById(R.id.textView_import_notes);

        // -------------- SPINNER --------------
        final String[] itemsCountry = context.getResources().getStringArray(R.array.country_values);
        int countrySelected = Arrays.asList(itemsCountry).indexOf(language);
        if(countrySelected == -1)
            countrySelected = Arrays.asList(itemsCountry).indexOf("en");

        spinner.setSelection(countrySelected);
        selectedCountry[0] = itemsCountry[countrySelected];

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCountry[0] = itemsCountry[position];

                items = getJahre(selectedCountry[0]);/*
                String imports = settings.getString(Tags.PREF_IMPORTS, " ");
                String[] importsArray = imports.split(",");
                List<String> newItems = new ArrayList<>();
                //Alle Einträge in items löschen die noch nicht importieret sind
                for(int i = 0; i < items.length; i++) {
                    if(Arrays.asList(importsArray).contains(items[i])) {
                        newItems.add(items[i]);
                    }
                }
                items = newItems.toArray(new String[newItems.size()]);*/

                checkedItemsBool = new boolean[items.length];

                for(int i = 0; i < items.length; i++) {
                    checkedItemsBool[i] = false;
                }

                checkBoxes = resetCheckboxesLanguage(context, linearLayout, items,checkedItemsBool);
                notes.setText(getNotesString(context, selectedCountry[0]));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        // ------------ END SPINNER --------------

        // --------------- YEARS ---------------
        checkBoxes = resetCheckboxesLanguage(context, linearLayout, items, checkedItemsBool);

        notes.setText(getNotesString(context, selectedCountry[0]));
        // ----------- END YEARS ----------------

        // set dialog message
        alertDialogBuilder
                .setCancelable(true)
                .setPositiveButton(context.getResources().getString(R.string.importVerb),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String schonImports = settings.getString(Tags.PREF_IMPORTS, " ");
                                String[] schonImportsArray = schonImports.split(",");

                                final List<String> importsArray = new ArrayList<>();
                                final List<String> importsArrayWeek = new ArrayList<>();
                                final List<String> importsArrayMonth = new ArrayList<>();
                                String imports = "";
                                boolean first = true;
                                for (int i = 0; i < items.length; i++) {
                                    //Wenn die Checkbox gedrückt ist
                                    if (checkedItemsBool[i]) {
                                        if (first) {
                                            first = false;
                                            imports += items[i];
                                        } else {
                                            imports += "," + items[i];
                                        }

                                        if(monat) {
                                            importsArrayMonth.add(selectedCountry[0] + "/" + items[i] + "_month");
                                            importsArrayWeek.add(selectedCountry[0] + "/" + items[i] + "_week");
                                        }

                                        importsArray.add(selectedCountry[0] + "/" + items[i]);

                                        //Oder es schon vorher importiert war
                                    } else if(Arrays.asList(schonImportsArray).contains(items[i])) {
                                        if (first) {
                                            first = false;
                                            imports += items[i];
                                        } else {
                                            imports += "," + items[i];
                                        }
                                    }
                                }

                                SharedPreferences.Editor editor = settings.edit();
                                editor.putString(Tags.PREF_IMPORTS, imports);
                                editor.putString(Tags.SELECTED_LANGUAGE, selectedCountry[0]);
                                editor.apply();

                                final Runnable week = new Runnable() {
                                    @Override
                                    public void run() {
                                        updateLosungen(context, importsArrayWeek, ifFinished, false, true);
                                    }
                                };
                                Runnable month = new Runnable() {
                                    @Override
                                    public void run() {
                                        updateLosungen(context, importsArrayMonth, week, true, false);
                                    }
                                };

                                if(monat)
                                    updateLosungen(context, importsArray, month, false, false);
                                else {
                                    updateLosungen(context, importsArray, ifFinished, false, false);
                                }
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    private List<Integer> mussImport;
    private String[] itemsoriginal;
    private boolean[] checkedItemsBool;
    private boolean[] checkedItemsBoolFertig;
    private String[] items;
    private List<CheckBox> checkBoxes;

    public void importLosungenMitSprache(final Context context, final Runnable ifFinished) {

        String language = Locale.getDefault().getLanguage();
        items = getJahre(language);
        mussImport = getMussImport(items);

        itemsoriginal = items.clone();
        checkedItemsBool = new boolean[items.length];
        checkedItemsBoolFertig = new boolean[items.length];

        final String[] selectedCountry = new String[1];

        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

        for(int i = 0; i < items.length; i++) {
            if(mussImport.contains(i)) {
                checkedItemsBool[i] = true;
                checkedItemsBoolFertig[i] = true;
                items[i] = items[i] + " " + context.getResources().getString(R.string.haveTo_import);
            } else {
                checkedItemsBool[i] = false;
                checkedItemsBoolFertig[i] = false;
            }
        }

        // get dialog_import.xml view
        LayoutInflater li = LayoutInflater.from(context);
        View dialogView = li.inflate(R.layout.dialog_import, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        // set dialog_import.xml to alertdialog builder
        alertDialogBuilder.setView(dialogView);

        final Spinner spinner = (Spinner) dialogView.findViewById(R.id.spinner_language);
        final LinearLayout linearLayout = (LinearLayout) dialogView.findViewById(R.id.linear_layout_import);
        final TextView notes = (TextView) dialogView.findViewById(R.id.textView_import_notes);

        // -------------- SPINNER --------------
        final String[] itemsCountry = context.getResources().getStringArray(R.array.country_values);
        int countrySelected = Arrays.asList(itemsCountry).indexOf(language);
        if(countrySelected == -1)
            countrySelected = Arrays.asList(itemsCountry).indexOf("en");

        spinner.setSelection(countrySelected);
        selectedCountry[0] = itemsCountry[countrySelected];

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCountry[0] = itemsCountry[position];

                items = getJahre(selectedCountry[0]);
                mussImport = getMussImport(items);

                itemsoriginal = items.clone();
                checkedItemsBool = new boolean[items.length];
                checkedItemsBoolFertig = new boolean[items.length];

                for(int i = 0; i < items.length; i++) {
                    if(mussImport.contains(i)) {
                        checkedItemsBool[i] = true;
                        checkedItemsBoolFertig[i] = true;
                        items[i] = items[i] + " " + context.getResources().getString(R.string.haveTo_import);
                    } else {
                        checkedItemsBool[i] = false;
                        checkedItemsBoolFertig[i] = false;
                    }
                }

                checkBoxes = resetCheckboxes(context, linearLayout, items,checkedItemsBool);
                notes.setText(getNotesString(context, selectedCountry[0]));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        // ------------ END SPINNER --------------

        // --------------- YEARS ---------------
        checkBoxes = resetCheckboxes(context, linearLayout, items, checkedItemsBool);

        notes.setText(getNotesString(context, selectedCountry[0]));
        // ----------- END YEARS ----------------

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(context.getResources().getString(R.string.importVerb),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                final List<String> importsArray = new ArrayList<>();
                                String imports = "";
                                boolean first = true;
                                for (int i = 0; i < itemsoriginal.length; i++) {
                                    if (checkedItemsBoolFertig[i]) {
                                        if (first) {
                                            first = false;
                                            imports += itemsoriginal[i];
                                        } else {
                                            imports += "," + itemsoriginal[i];
                                        }

                                        importsArray.add(selectedCountry[0] + "/" + itemsoriginal[i]);
                                    }
                                }

                                SharedPreferences.Editor editor = settings.edit();
                                editor.putString(Tags.PREF_IMPORTS, imports);
                                editor.putString(Tags.SELECTED_LANGUAGE, selectedCountry[0]);
                                editor.apply();

                                final List<String> importsArrayOriginal = new ArrayList<String>(importsArray);

                                final Runnable month = new Runnable() {
                                    @Override
                                    public void run() {
                                        //Monat
                                        for(int i = 0; i < importsArray.size(); i++) {
                                            importsArray.set(i, importsArrayOriginal.get(i) + "_month");
                                        }
                                        importLosungen(context, importsArray, ifFinished, true, false);
                                    }
                                };

                                Runnable week = new Runnable() {
                                    @Override
                                    public void run() {
                                        //Woche
                                        for(int i = 0; i < importsArray.size(); i++) {
                                            importsArray.set(i, importsArrayOriginal.get(i) + "_week");
                                        }
                                        importLosungen(context, importsArray, month, false, true);
                                    }
                                };

                                importLosungen(context, importsArray, week, false, false);
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();

        /*
        builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.importVerb));
        builder.setMultiChoiceItems(items, checkedItemsBool, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if (mussImport.contains(which) && !isChecked) {
                    MainActivity.toast(context, "Wird trotzdem importiert", Toast.LENGTH_LONG);
                } else if (schonImport.contains(which)) {
                    MainActivity.toast(context, "Ist bereits importiert", Toast.LENGTH_SHORT);
                } else {
                    checkedItemsBoolFertig[which] = !checkedItemsBoolFertig[which];
                }
            }
        });*/

    }

    private String getNotesString(Context context, String country) {
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

    private String[] getJahre(String language) {
        return Tags.getImport(language);
    }

    private List<Integer> getMussImport(String[] list) {
        List<Integer> result = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);

        for(int i = 0; i < list.length; i++) {
            try {
                int element = Integer.parseInt(list[i]);
                if(element == year)
                    result.add(i);
            } catch (NumberFormatException e) {
                Log.e("Losungen", "Falsche liste bei getMussImport in ChooseDialog");
            }
        }

        //if(result.size() == 0)
        //result.add(0);

        return result;
    }

    public List<CheckBox> resetCheckboxesLanguage(Context context, LinearLayout linearLayout, String[] items, boolean[] checked) {
        final List<CheckBox> checkBoxes = new ArrayList<>();
        linearLayout.removeAllViews();

        CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                for(int i = 0; i < checkBoxes.size(); i++) {
                    if(buttonView == checkBoxes.get(i)) {
                        checkedItemsBool[i] = !checkedItemsBool[i];
                    }
                }

            }
        };

        for(int i = 0; i < items.length; i++) {
            CheckBox checkBox = new CheckBox(context);
            checkBox.setText(items[i]);
            checkBox.setHighlightColor(Colors.getColor(context, Colors.ACCENT));
            checkBox.setChecked(checked[i]);

            checkBox.setOnCheckedChangeListener(listener);

            checkBoxes.add(checkBox);

            linearLayout.addView(checkBox);
        }

        return checkBoxes;
    }

    private List<CheckBox> resetCheckboxes(Context context, LinearLayout linearLayout, String[] items, boolean[] checked) {
        final List<CheckBox> checkBoxes = new ArrayList<>();
        linearLayout.removeAllViews();

        CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                for(int i = 0; i < checkBoxes.size(); i++) {
                    if(buttonView == checkBoxes.get(i)) {
                        if (mussImport.contains(i) && !isChecked) {
                            checkBoxes.get(i).setChecked(true);
                        } else {
                            checkedItemsBoolFertig[i] = !checkedItemsBoolFertig[i];
                        }
                    }
                }

            }
        };

        for(int i = 0; i < items.length; i++) {
            CheckBox checkBox = new CheckBox(context);
            checkBox.setText(items[i]);
            //checkBox.setHighlightColor(context.getResources().getColor(Colors.getColor(context, Colors.PRIMARY)));
            checkBox.setChecked(checked[i]);

            checkBox.setOnCheckedChangeListener(listener);

            checkBoxes.add(checkBox);

            linearLayout.addView(checkBox);
        }

        return checkBoxes;
    }

    private AlertDialog wait;
    public void showLoadDialog(Context context) {
        builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.importString));
        builder.setCancelable(false);
        builder.setMessage(R.string.importing);

        wait = builder.create();
        wait.show();
    }

    public void closeLoadDialog() {
        wait.cancel();
    }

    public void importMonatUndWoche(final Context context, String language, List<String> years, final Runnable restartFragment) {
        final List<String> filesMonth = new ArrayList<>();
        final List<String> filesWeek = new ArrayList<>();
        for(int i = 0; i < years.size(); i++) {
            filesMonth.add(language + "/" + years.get(i) + "_month");
            filesWeek.add(language + "/" + years.get(i) + "_week");
        }

        //showLoadDialog(context);

        final Runnable ifFinished = new Runnable() {
            @Override
            public void run() {
                //closeLoadDialog();
            }
        };
        Runnable afterMonth = new Runnable() {
            @Override
            public void run() {
                importLosungen(context, filesWeek, restartFragment, false, true);
            }
        };

        importLosungen(context, filesMonth, afterMonth, true, false);
    }

    private void importLosungen(final Context context, final List<String> files,
                                final Runnable runnable, final boolean monat, final boolean woche) {
        showLoadDialog(context);


        Thread background = new Thread(new Runnable() {
            @Override
            public void run() {
                XmlParser xmlParser = new XmlParser();
                InputStream in = null;
                for(int i = 0; i < files.size(); i++) {
                    try {
                        in = context.getResources().getAssets().open(files.get(i) + ".xml");
                        xmlParser.parseXML(in);
                        xmlParser.writeIntoDatabase(context, monat, woche);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                closeLoadDialog();

                Handler mainHandler = new Handler(Looper.getMainLooper());
                mainHandler.post(runnable);
            }
        });
        background.start();

    }

    private void updateLosungen(final Context context, final List<String> files, final Runnable runnable,
                                final boolean monat, final boolean woche) {
        showLoadDialog(context);

        Thread background = new Thread(new Runnable() {
            @Override
            public void run() {
                XmlParser xmlParser = new XmlParser();
                InputStream in = null;
                for (int i = 0; i < files.size(); i++) {
                    try {
                        in = context.getResources().getAssets().open(files.get(i) + ".xml");
                        xmlParser.parseXML(in);
                        xmlParser.updateIntoDatabase(context, monat, woche);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                closeLoadDialog();
                Handler mainHandler = new Handler(Looper.getMainLooper());
                mainHandler.post(runnable);

            }
        });
        background.start();
    }

    private void openUrl(Context context, String url) {
        Uri uri = Uri.parse(url);
        context.startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }
}
