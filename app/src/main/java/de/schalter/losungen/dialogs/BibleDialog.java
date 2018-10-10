package de.schalter.losungen.dialogs;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.schalter.losungen.settings.Tags;

/**
 * Created by martin on 11.03.16.
 */
public class BibleDialog {

    private List<String> books_language;

    private Context context;

    private int book;
    private List<Integer> final_verses;
    private List<String> final_connectors;

    public BibleDialog(Context context) throws IOException {
        this.context = context;

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        String language = settings.getString(Tags.SELECTED_LANGUAGE, "en");

        books_language = getBooks(language);
    }

    public void openApp() {
        String intentExtra = "a:";
        for(int i = 0; i < final_verses.size(); i++) {
            intentExtra += String.valueOf(final_verses.get(i));
            if(final_connectors.size() > (i)) { //Wenn noch ein connector vorhanden ist
                if(final_connectors.get(i).equals("."))
                    intentExtra += ",";
                else
                    intentExtra += final_connectors.get(i);
            }
        }

        Intent intent = new Intent("yuku.alkitab.action.SHOW_VERSES_DIALOG");
        //Intent intent = new Intent("yuku.alkitab.action.VIEW");
        intent.putExtra("target", intentExtra);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    public void openDialog() throws ActivityNotFoundException{
        String intentExtra = "a:";
        for(int i = 0; i < final_verses.size(); i++) {
            intentExtra += String.valueOf(final_verses.get(i));
            if(final_connectors.size() > (i)) { //Wenn noch ein connector vorhanden ist
                if(final_connectors.get(i).equals("."))
                    intentExtra += ",";
                else
                    intentExtra += final_connectors.get(i);
            }
        }

        Intent intent = new Intent("yuku.alkitab.action.SHOW_VERSES_DIALOG");
        //Intent intent = new Intent("yuku.alkitab.action.VIEW");
        intent.putExtra("target", intentExtra);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    public void loadVers(String vers) throws NumberFormatException {
        String original_vers = vers;

        final_verses = new ArrayList<>();
        final_connectors = new ArrayList<>();

        loop:
        for(int i = 0; i < books_language.size(); i++) {
            if(original_vers.contains(books_language.get(i))) {
                book = i;
                break loop;
            }
        }

        vers = vers.replace(books_language.get(book), "");
        vers = vers.trim();

        //The format is now 15,17 or
        // 15 (only Judas),
        // 4,8-9 or
        // 3.5 or
        // 1,14-2,1 or
        // 4,15.17-18
        //more complex formats will I ignore :D

        if(vers.contains(",")) {
            String[] chapterSplit = vers.split(",");
            if(chapterSplit.length > 2) {
                //now the format was like
                //1,14-2,1
                //Array is then [1, 14-2, 1];
                //I will not handle this for now...
                throw new NumberFormatException("This verses can not be handelded");
            } else {
                //now only one chapter in position 0 of Array
                //Position 1 is the vers(range)
                int chapter = Integer.parseInt(chapterSplit[0]);

                Object[] result = getVerses(chapterSplit[1]);
                List<Integer> versesInt = (List<Integer>) result[0];
                List<String> dividersString = (List<String>) result[1];

                final_connectors = dividersString;
                for(int i = 0; i < versesInt.size(); i++) {
                    final_verses.add(getBibelStelle(book, chapter, versesInt.get(i)));
                }
            }
        } else {
            //Only Judas
            int chapter = 1;

            Object[] result = getVerses(vers);
            List<Integer> versesInt = (List<Integer>) result[0];
            List<String> dividersString = (List<String>) result[1];

            final_connectors = dividersString;
            for(int i = 0; i < versesInt.size(); i++) {
                final_verses.add(getBibelStelle(book, chapter, versesInt.get(i)));
            }
        }
    }

    private int getBibelStelle(int book, int chapter, int vers) {
        return book * 65536 + chapter * 256 + vers;
    }

    private Object[] getVerses(String verses) {
        List<Integer> versesInt = new ArrayList<>();
        List<String> dividersString = new ArrayList<>();

        if(verses.contains("-") || verses.contains(".")) {
            if(verses.contains("-") && verses.contains(".")) {
                //possible formats:
                //5.7-9
                //8-9.11
                //4.8.11.15-18
                //4.8-11.15

                while(verses.contains("-") || verses.contains(".")) {
                    //Get the next divider
                    int positionPoint = verses.indexOf(".");
                    int positionKomma = verses.indexOf("-");

                    String divider;
                    if((positionPoint < positionKomma && positionPoint != -1)
                            || positionKomma == -1) {
                        dividersString.add(".");
                        //Point is next divider
                        //Check if their is a divider after this
                        int positionNextKomma = verses.indexOf("-", positionPoint);
                        int positionNextPoint = verses.indexOf(".", positionPoint);

                        if(positionNextKomma == -1 && positionNextPoint == -1) {
                            //that is the last divider
                            List<Integer> newVerses = getVersPlus(verses.substring(0, positionNextPoint));
                            for(int i = 0; i < newVerses.size(); i++) {
                                versesInt.add(newVerses.get(i));
                            }
                        } else if((positionNextPoint < positionNextKomma && positionNextPoint != -1)
                                || positionNextKomma == -1){
                            //Point is the next divider
                            List<Integer> newVerses = getVersPlus(verses.substring(0, positionNextPoint));
                            for(int i = 0; i < newVerses.size(); i++) {
                                versesInt.add(newVerses.get(i));
                            }
                        } else {
                            //Komma is the next divider
                            List<Integer> newVerses = getVersPlus(verses.substring(0, positionNextKomma));
                            for(int i = 0; i < newVerses.size(); i++) {
                                versesInt.add(newVerses.get(i));
                            }
                        }

                    } else {
                        //Komma is next divider
                        dividersString.add("-");
                        //Check if their is a divider after this
                        int positionNextKomma = verses.indexOf("-", positionPoint);
                        int positionNextPoint = verses.indexOf(".", positionPoint);

                        if(positionNextKomma == -1 && positionNextPoint == -1) {
                            //that is the last divider
                            List<Integer> newVerses = getVersRange(verses.substring(0, positionNextPoint));
                            for(int i = 0; i < newVerses.size(); i++) {
                                versesInt.add(newVerses.get(i));
                            }
                        } else if((positionNextPoint < positionNextKomma && positionNextPoint != -1)
                                || positionNextKomma == -1){
                            //Point is the next divider
                            List<Integer> newVerses = getVersRange(verses.substring(0, positionNextPoint));
                            for(int i = 0; i < newVerses.size(); i++) {
                                versesInt.add(newVerses.get(i));
                            }
                        } else {
                            //Komma is the next divider
                            List<Integer> newVerses = getVersRange(verses.substring(0, positionNextKomma));
                            for(int i = 0; i < newVerses.size(); i++) {
                                versesInt.add(newVerses.get(i));
                            }
                        }
                    }


                }
            } else if(verses.contains("-")) {
                dividersString.add("-");
                versesInt.addAll(getVersRange(verses));
            } else {
                dividersString.add(".");
                versesInt.addAll(getVersPlus(verses));
            }
        } else {
            //Only one vers
            versesInt.add(Integer.parseInt(verses));
        }

        Object[] returnObject = {versesInt, dividersString};

        return returnObject;
    }

    private List<Integer> getVersRange(String verses) throws NumberFormatException {
        //Vers range
        String[] versesArray = verses.split("-");
        int first_vers = Integer.parseInt(versesArray[0]);
        int last_vers = Integer.parseInt(versesArray[1]);

        Integer[] versesInt = {first_vers, last_vers};

        return Arrays.asList(versesInt);
    }

    private List<Integer> getVersPlus(String verses) throws NumberFormatException {
        //Two verses
        String[] versesArray = verses.split("\\.");
        Integer[] vers = {Integer.parseInt(versesArray[0]),
                Integer.parseInt(versesArray[1])};

        return Arrays.asList(vers);
    }

    private List<String> getBooks(String language) throws IOException {
        String content = "";

        String FOLDER = "books";
        InputStream in = context.getResources().getAssets().open(FOLDER + "/" + language +  ".txt");
        byte[] input = new byte[in.available()];
        while (in.read(input) != -1) {
            content += new String(input);
        }

        String[] books = content.split(",");

        return Arrays.asList(books);
    }
}
