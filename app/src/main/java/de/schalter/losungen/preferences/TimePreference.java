package de.schalter.losungen.preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import de.schalter.losungen.R;

/**
 * Created by marti on 30.10.2015.
 */
public class TimePreference extends DialogPreference {

    private Calendar calendar;
    private TimePicker picker = null;
    private long time = 1000 * 60 * 60 * 7;

    public TimePreference(Context ctxt) {
        this(ctxt, null);
    }


    @SuppressWarnings("static-access")
    public TimePreference(Context ctxt, AttributeSet attrs) {
        this(ctxt, attrs, ctxt.getResources().getSystem().getIdentifier("dialogPreferenceStyle", "attr", "android"));
    }

    public TimePreference(Context ctxt, AttributeSet attrs, int defStyle) {
        super(ctxt, attrs, defStyle);

        setPositiveButtonText(getContext().getResources().getString(R.string.save));
        setNegativeButtonText(getContext().getResources().getString(R.string.cancel));
        calendar = new GregorianCalendar();
    }

    @Override
    protected View onCreateDialogView() {
        picker = new TimePicker(getContext());
        return (picker);
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
        picker.setCurrentHour((int)time/60);
        picker.setCurrentMinute((int)time % 60);
        picker.setIs24HourView(DateFormat.is24HourFormat(getContext()));
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {

            calendar.set(Calendar.HOUR_OF_DAY, picker.getCurrentHour());
            calendar.set(Calendar.MINUTE, picker.getCurrentMinute());
            //Zeit von 00:00 Uhr in Minuten
            time = picker.getCurrentHour() * 60 + picker.getCurrentMinute();

            setSummary(getSummary());
            if (callChangeListener(time)) {
                persistLong(time);
                notifyChanged();
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        int integer = a.getInt(index, 60 * 7);
        time = integer;
        return String.valueOf(integer);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {

        if (restoreValue) {
            if (defaultValue == null) {
                time=(getPersistedLong(System.currentTimeMillis()));
            } else {
                time=(Long.parseLong(getPersistedString((String) defaultValue)));
            }
        } else {
            if (defaultValue == null) {
                time = (System.currentTimeMillis());
            } else {
                time = (Long.parseLong((String) defaultValue));
            }
        }
        setSummary(getSummary());
    }

    @Override
    public CharSequence getSummary() {
        int hours = (int) time / 60;
        int minutes = (int) time % 60;
        calendar.set(Calendar.HOUR_OF_DAY, hours);
        calendar.set(Calendar.MINUTE, minutes);
        String date = DateFormat.getTimeFormat(getContext()).format(new Date(calendar.getTimeInMillis()));
        return date;
    }
}
