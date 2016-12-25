package de.schalter.losungen.dialogs;

import android.app.DatePickerDialog;
import android.content.Context;

import java.util.Calendar;

/**
 * Created by martin on 15.11.16.
 */

public class DateChooserDialog {

    private DatePickerDialog dialog;

    /**
     * Shows a dialog to pick a date
     * @param context the application context
     * @param calendar the start date to pick from
     * @param listener what will happen when you choose a date
     */
    public DateChooserDialog(Context context, Calendar calendar, DatePickerDialog.OnDateSetListener listener) {
        dialog = new DatePickerDialog(context, listener,
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
    }

    public void show() {
        dialog.show();
    }

}
