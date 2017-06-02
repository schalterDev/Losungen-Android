package schalter.dev.customizelibrary;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Smarti on 28.01.2016.
 */
public class DesignPref extends DialogPreference {

    private Context context;

    private GridAdapter adapter;

    private int selectedDefault;

    public static final String TAGSETTING = "color_custom_design";

    public DesignPref(Context ctxt) {
        this(ctxt, null);
        context = ctxt;
    }

    public DesignPref(Context ctxt, AttributeSet attrs) {
        this(ctxt, attrs, Resources.getSystem().getIdentifier("dialogPreferenceStyle", "attr", "android"));
        context = ctxt;
    }

    public DesignPref(Context ctxt, AttributeSet attrs, int defStyle) {
        super(ctxt, attrs, defStyle);

        context = ctxt;

        setPositiveButtonText(getContext().getResources().getString(R.string.save));
        setNegativeButtonText(getContext().getResources().getString(R.string.cancel));
    }


    @Override
    protected View onCreateDialogView() {
        LayoutInflater li = (LayoutInflater)getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View view = li.inflate(R.layout.design_pref, null, false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_desing_pref);
        recyclerView.setHasFixedSize(false);

        //Number of columns, Tablet has 6 - phone 3
        boolean tabletSize = context.getResources().getBoolean(R.bool.isTablet);
        RecyclerView.LayoutManager layoutManager;
        if (tabletSize) {
            layoutManager = new GridLayoutManager(view.getContext(), 6);
        } else {
            layoutManager = new GridLayoutManager(view.getContext(), 3);
        }

        //Colors
        List<Integer> colors = new ArrayList<>();
        colors.add(context.getResources().getColor(R.color.blue));
        colors.add(context.getResources().getColor(R.color.yellow));
        colors.add(context.getResources().getColor(R.color.green));
        colors.add(context.getResources().getColor(R.color.red));
        colors.add(context.getResources().getColor(R.color.dark));
        colors.add(context.getResources().getColor(R.color.light));

        recyclerView.setLayoutManager(layoutManager);

        adapter = new GridAdapter(colors, getContext(), selectedDefault);
        recyclerView.setAdapter(adapter);

        return (view);
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
        adapter.setCheckedItem(selectedDefault);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {

            int position = adapter.getColorPosition();
            selectedDefault = position;

            setSummary(getSummary());
            if(callChangeListener(position)) {
                persistInt(position);
                notifyChanged();
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        int integer = a.getInt(index, index);
        return integer;
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {

        if (restoreValue) {
            if (defaultValue == null) {
                selectedDefault = (getPersistedInt(0));
            } else {
                selectedDefault = (int) (defaultValue);
            }
        } else {
            if (defaultValue == null) {
                selectedDefault = 0;
            } else {
                selectedDefault = (int) (defaultValue);
            }
        }
        setSummary(getSummary());
    }

    @Override
    public CharSequence getSummary() {
        //TODO show color name
        return context.getResources().getString(R.string.choose_color);
    }
}
