package de.schalter.losungen.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.schalter.losungen.Losung;
import de.schalter.losungen.MainActivity;
import de.schalter.losungen.R;
import de.schalter.losungen.files.DBHandler;
import de.schalter.losungen.lists.GridAdapter;

/**
 * Created by marti on 27.10.2015.
 */
public class FragmentLosungMonth extends Fragment {

    private Menu menu;
    private DBHandler dbHandler;
    private MainActivity mainActivity;
    private Context context;
    private SharedPreferences settings;
    private long date;

    private List<Losung> losungen;

    /**
     * @param time which day is it
     * @return the fragment with the right daily word
     */
    public static FragmentLosungMonth newFragmentLosungTag(long time, MainActivity context) {
        FragmentLosungMonth fragment = new FragmentLosungMonth();
        fragment.setDate(time, context);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_losung_list, container,
                false);

        context = getContext();

        initialise(view);

        return view;
    }

    private List<Losung> getLosungen(long datum) {
        //I had some errors with context = null
        //so no I check and try every available method
        //to get a context
        if(context == null) {
            context = mainActivity;
        }
        if(context == null)
            context = getActivity();

        List<Losung> losungen = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(datum);

        if(dbHandler == null)
            dbHandler = DBHandler.newInstance(context);

        Losung losungMonth = dbHandler.getMonthlyWord(datum);
        losungMonth.setTitleLosung(context.getResources().getString(R.string.month_title));
        losungen.add(losungMonth);

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int month = calendar.get(Calendar.MONTH);
        int monthOriginal = month;
        while(month == monthOriginal) {
            Losung losung = dbHandler.getWeeklyWord(calendar.getTimeInMillis());
            losung.setTitleLosung(Losung.getFullDatumFromTime(losung.getDatum())); //TODO change title
            losungen.add(losung);
            calendar.add(Calendar.DAY_OF_MONTH, 7);
            month = calendar.get(Calendar.MONTH);
        }

        return losungen;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.menu_losung_month_item, menu);

        this.menu = menu;

        super.onCreateOptionsMenu(menu, menuInflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            //Managed by MainActivity
            return false;
        } else if (id == R.id.action_search) {
            //Show search dialog
            //SearchDialog dialog = new SearchDialog(mainActivity);
            //dialog.show();
            //TODO search
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initialise(View view) {
        //I had some problems with context = null
        //So now I try every method to get a context
        if(context != null)
            settings = PreferenceManager.getDefaultSharedPreferences(context);
        else if(getContext() != null) {
            settings = PreferenceManager.getDefaultSharedPreferences(getContext());
            context = getContext();
        } else if (getActivity() != null) {
            settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
            context = getActivity();
        } else if(mainActivity != null) {
            settings = PreferenceManager.getDefaultSharedPreferences(mainActivity);
            context = mainActivity;
        } else {
            Log.e("Losungen", "in FragmentLosungTag:  context, getContext and getActivity are null");
        }

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_losung_list);
        recyclerView.setHasFixedSize(false);

        //Number of columns, Tablet has 2 - phone 1
        boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
        RecyclerView.LayoutManager layoutManager;
        if (tabletSize) {
            layoutManager = new GridLayoutManager(view.getContext(), 2);
        } else {
            layoutManager = new GridLayoutManager(view.getContext(), 1);
        }

        recyclerView.setLayoutManager(layoutManager);

        RecyclerView.Adapter adapter = new GridAdapter(losungen, getContext(), false, true);
        recyclerView.setAdapter(adapter);
    }

    public void setDate(long time, MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.date = time;
        losungen = getLosungen(date);
    }

}
