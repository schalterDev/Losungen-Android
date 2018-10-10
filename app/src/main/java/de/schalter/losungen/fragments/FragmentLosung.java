package de.schalter.losungen.fragments;

import android.app.DatePickerDialog;
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
import android.widget.DatePicker;

import com.astuetz.PagerSlidingTabStrip;

import java.util.Calendar;

import de.schalter.losungen.MainActivity;
import de.schalter.losungen.R;
import de.schalter.losungen.dialogs.DateChooserDialog;
import de.schalter.losungen.tabs.PagerAdapter;
import schalter.dev.customizelibrary.Colors;

/**
 * Created by marti on 27.10.2015.
 */
public class FragmentLosung extends Fragment {

    private PagerAdapter pagerAdapter;
    private ViewPager pager;

    private SharedPreferences settings;

    private MainActivity activity;

    public static FragmentLosung newInstance(MainActivity mainActivity) {
        FragmentLosung fragment = new FragmentLosung();
        fragment.setActivity(mainActivity);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        settings = PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.menu_losung, menu);

        super.onCreateOptionsMenu(menu, menuInflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_choose_date) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(pagerAdapter.getCurrentDate());

            DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    setDate(year, monthOfYear, dayOfMonth);
                }
            };

            DateChooserDialog dialog = new DateChooserDialog(getContext(), calendar, listener);
            dialog.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_losung, container,
                false);

        initialise(view);

        return view;
    }

    //Update View to show this date
    private void setDate(int year, int month, int day) {
        pagerAdapter.setDate(year, month, day);
        pager.setCurrentItem(PagerAdapter.ITEMSBEFOR, false);
    }

    private void initialise(View view) {
        pager = view.findViewById(R.id.pager);
        pagerAdapter = new PagerAdapter(getChildFragmentManager(),activity);
        PagerSlidingTabStrip tabs = view.findViewById(R.id.tabs);

        pager.setPadding(5, 5, 5, 0);
        pager.setAdapter(pagerAdapter);
        pager.setCurrentItem(PagerAdapter.ITEMSBEFOR, true);
        tabs.setViewPager(pager);

        tabs.setTextColor(Colors.getColor(getContext(), Colors.ICONS_TOOLBAR));
        tabs.setIndicatorColor(Colors.getColor(getContext(), Colors.ACCENT));
        tabs.setDividerColor(Colors.getColor(getContext(), Colors.PRIMARY));
        tabs.setIndicatorHeight(15);

        pager.setPageMargin(5);

        tabs.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            //If you reach the beginning or end of the tabs - load new
            @Override
            public void onPageSelected(int position) {
                boolean scrolledToRight = (position > PagerAdapter.ITEMSBEFOR);

                //This will add new tabs if necessary and will return the new right position
                int scrollTo = pagerAdapter.gescrollt(position, scrolledToRight);
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
}
