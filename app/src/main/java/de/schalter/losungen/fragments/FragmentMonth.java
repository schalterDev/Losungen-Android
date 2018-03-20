package de.schalter.losungen.fragments;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.Calendar;

import de.schalter.losungen.AnalyticsApplication;
import de.schalter.losungen.Losung;
import de.schalter.losungen.MainActivity;
import de.schalter.losungen.R;
import de.schalter.losungen.files.DBHandler;
import de.schalter.losungen.settings.Tags;
import de.schalter.losungen.tabs.PagerAdapter;
import de.schalter.losungen.tabs.PagerAdapterMonth;
import schalter.dev.customizelibrary.Colors;

public class FragmentMonth extends Fragment {

    private Callbacks mCallbacks;

    private PagerAdapterMonth pagerAdapter;
    private ViewPager pager;

    private SharedPreferences settings;

    private MainActivity activity;

    public static FragmentMonth newInstance(MainActivity mainActivity) {
        FragmentMonth fragment = new FragmentMonth();
        fragment.setActivity(mainActivity);
        return fragment;
    }

    //Send screen to google Analytics
    private void analytics() {
        if(settings.getBoolean(Tags.PREF_GOOGLEANALYTICS, true)) {
            // Obtain the shared Tracker instance.
            AnalyticsApplication application = (AnalyticsApplication) getActivity().getApplication();
            Tracker mTracker = application.getDefaultTracker();

            mTracker.setScreenName("Fragment-Losungen-Month");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Activities containing this fragment must implement its callbacks
        mCallbacks = (Callbacks) activity;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        settings = PreferenceManager.getDefaultSharedPreferences(getContext());

        checkIfImported();

        analytics();
    }

    private void checkIfImported() {
        /*
        Thread background = new Thread(new Runnable() {
            @Override
            public void run() {*/
                DBHandler dbHandler = DBHandler.newInstance(getContext());
                Losung losung = dbHandler.getMonthlyWord(System.currentTimeMillis());
                if(losung.getLosungstext().equals(getContext().getResources().getString(R.string.no_date))) {
                    String yearsString = settings.getString(Tags.PREF_IMPORTS, "");
                    String[] years = yearsString.split(",");

                    Runnable restartFragment = new Runnable() {
                        @Override
                        public void run() {
                            mCallbacks.refreshMonthFragment();
                        }
                    };

                    //TODO update the commented thing

                    /*
                    ImportLosungenIntoDB.importMonthAndWeeks(getContext(),
                            settings.getString(Tags.SELECTED_LANGUAGE, "en"),
                            Arrays.asList(years), restartFragment, false);*/
                }
            /*}
        });
        background.start();*/
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.menu_losung_month, menu);

        super.onCreateOptionsMenu(menu, menuInflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return id != R.id.action_choose_date && super.onOptionsItemSelected(item);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_losung_month, container,
                false);

        initialise(view);

        return view;
    }

    private void initialise(View view) {
        Calendar calendar = Calendar.getInstance();

        pager = (ViewPager) view.findViewById(R.id.pager_month);
        pagerAdapter = new PagerAdapterMonth(getChildFragmentManager(),activity);
        pagerAdapter.setDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) view.findViewById(R.id.tabs_month);

        pager.setPadding(5, 5, 5, 0);
        pager.setAdapter(pagerAdapter);
        pager.setCurrentItem(PagerAdapter.ITEMSBEFOR, true);
        tabs.setViewPager(pager);

        tabs.setTextColor(Colors.getColor(getContext(), Colors.ICONS_TOOLBAR));
        tabs.setIndicatorColor(Colors.getColor(getContext(), Colors.ACCENT));
        tabs.setDividerColor(Colors.getColor(getContext(), Colors.PRIMARY));
        tabs.setIndicatorHeight(6);

        pager.setPageMargin(5);

        tabs.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            //If you reach the beginning or end of the tabs - load new
            @Override
            public void onPageSelected(int position) {
                boolean nachRechtsGescrollt = (position > PagerAdapter.ITEMSBEFOR);

                //This will add new tabs if necessary and will return the new right position
                int scrollTo = pagerAdapter.gescrollt(position, nachRechtsGescrollt);
                //If the count of the tabs changed - maybe you have to change the position
                if (scrollTo != position)
                    pager.setCurrentItem(scrollTo, true);

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void setActivity(MainActivity activity) {
        this.activity = activity;
    }

    public interface Callbacks {
        void refreshMonthFragment();
    }
}
