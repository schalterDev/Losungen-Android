package de.schalter.losungen.preferences;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import de.schalter.losungen.Losung;
import de.schalter.losungen.MainActivity;
import de.schalter.losungen.R;
import de.schalter.losungen.files.DBHandler;

/**
 * Created by Smarti on 19.02.2016.
 */
public class AudioDeletePreference extends ListPreference {

    //Need to make sure the SEPARATOR is unique and weird enough that it doesn't match one of the entries.
    //Not using any fancy symbols because this is interpreted as a regex for splitting strings.
    private static final String SEPARATOR = "OV=I=XseparatorX=I=VO";

    private boolean[] mClickedDialogEntry;
    private Long[] entryDates;

    private DBHandler dbHandler;

    public AudioDeletePreference(Context ctxt) {
        this(ctxt, null);
    }

    public AudioDeletePreference(Context ctxt, AttributeSet attrs) {
        super(ctxt,attrs);

        setPositiveButtonText(getContext().getResources().getString(R.string.delete));
        setNegativeButtonText(getContext().getResources().getString(R.string.cancel));

        mClickedDialogEntry = new boolean[getEntryValuesLong().length];
        for(int i = 0; i < mClickedDialogEntry.length; i++) {
            mClickedDialogEntry[i] = true;
        }
    }

    @Override
    public CharSequence[] getEntries() {
        if(entryDates != null) {
            CharSequence[] entries = new CharSequence[entryDates.length];

            for (int i = 0; i < entryDates.length; i++) {
                entries[i] = Losung.getDatumLongFromTime(entryDates[i]);
            }
            return entries;
        }

        return null;
    }

    @Override
    public CharSequence[] getEntryValues() {
        return null;
    }

    public Long[] getEntryValuesLong() {
        if(dbHandler == null)
            dbHandler = DBHandler.newInstance(getContext());

        List<Long> dates = dbHandler.getAllAudios();

        /*
        CharSequence[] entrys = getEntryValues();
        long[] entryLong = new long[entrys.length];
        for(int i = 0; i < entrys.length; i++) {
            entryLong[i] = Long.valueOf(String.valueOf(entrys[i]));
        }

        return entryLong;*/

        Long[] finalEntrys = dates.toArray(new Long[dates.size()]);
        return finalEntrys;
    }

    @Override
    public void setEntries(CharSequence[] entries) {
        super.setEntries(entries);
        mClickedDialogEntry = new boolean[entries.length];
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        Long[] entryValues = getEntryValuesLong();
        entryDates = entryValues;
        CharSequence[] entries = getEntries();

        if (entries == null || entryValues == null || entries.length != entryValues.length ) {
            throw new IllegalStateException(
                    "ListPreference requires an entries array and an entryValues array which are both the same length");
        }

        builder.setMultiChoiceItems(entries, mClickedDialogEntry,
                new DialogInterface.OnMultiChoiceClickListener() {
                    public void onClick(DialogInterface dialog, int which, boolean val) {
                        mClickedDialogEntry[which] = val;
                    }
                });
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
    //super.onDialogClosed(positiveResult);

        if(positiveResult) {
            //Lösche gewählte Tage
            for(int i = 0; i < mClickedDialogEntry.length; i++) {
                if(mClickedDialogEntry[i]) {
                    //Error reported in developer console. IndexOutOfBound size = 0, index = 0
                    //I dont know why know
                    //TODO check error
                    if(entryDates.length > i)
                        deleteDate(entryDates[i]);
                }
            }
        }
    }

    private void deleteDate(long date) {
        if(dbHandler == null) {
            dbHandler = DBHandler.newInstance(getContext());
        }

        String path = dbHandler.getAudioLosungen(date);

        File file = new File(path);
        boolean deleted = file.delete();
        if(!deleted) {
            MainActivity.toast(getContext(), getContext().getResources().getString(R.string.delete_failed), Toast.LENGTH_SHORT);
        } else {
            dbHandler.setAudioNull(date);
        }
    }
}
