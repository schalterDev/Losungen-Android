package de.schalter.losungen.tabs;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.schalter.losungen.MainActivity;
import de.schalter.losungen.fragments.FragmentLosungTag;

/**
 * Created by marti on 27.10.2015.
 */
public class PagerAdapter extends android.support.v4.app.FragmentStatePagerAdapter {

    private long baseId = 0;

    //Immer 9 Tage in den Tabs anzeigen
    public static final int ITEMSBEFOR = 4;
    private final int ITEMSAFTER = 4;

    private MainActivity context;

    private List<String> titles;
    private List<Long> times;
    private FragmentLosungTag[] fragments;

    private long currentDate;

    public PagerAdapter(FragmentManager fm, MainActivity context) {
        super(fm);
        this.context = context;

        titles = new ArrayList<>();
        times = new ArrayList<>();
        fragments = new FragmentLosungTag[100];

        long timeNow = System.currentTimeMillis();
        currentDate = timeNow;

        String title = "";
        long time;
        for(int i = ITEMSBEFOR * -1; i < ITEMSAFTER + 1; i++) {
            time = timeNow + (i * 1000 * 60 * 60 * 24);
            title = getTitleByTime(time);

            titles.add(title);
            times.add(time);
        }

        this.notifyDataSetChanged();

    }

    private String getTitleByTime(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        Date date = calendar.getTime();
        DateFormat df = new SimpleDateFormat("E, dd.MM");
        return df.format(date);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title = titles.get(position);

        return title;
    }

    @Override
    public Fragment getItem(int position) {
        FragmentLosungTag fragmentLosungTag = FragmentLosungTag.newFragmentLosungTag(times.get(position), context);

        fragments[position] = fragmentLosungTag;

        return fragmentLosungTag;
    }

    @Override
    public int getCount() {
        return titles.size();
    }

    @Override
    public int getItemPosition(Object object) {
        int index = Arrays.asList(fragments).indexOf(object);

        if(index == -1)
            return POSITION_NONE;

        return index;
    }

    /*
    @Override
    public long getItemId(int position) {
        baseId++;
        return baseId;
    }*/

    public void setDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        long timeNow = calendar.getTimeInMillis();
        currentDate = timeNow;

        titles.clear();
        times.clear();

        //reset fragments
        for(int i = 0; i < fragments.length; i++) {
            fragments[i] = null;
        }

        String title = "";
        long time;
        for(int i = ITEMSBEFOR * -1; i < ITEMSAFTER + 1; i++) {
            time = timeNow + (i * 1000 * 60 * 60 * 24);
            title = getTitleByTime(time);

            titles.add(title);
            times.add(time);
        }
        this.notifyDataSetChanged();
    }

    public int gescrollt(int position, boolean scrolledToRight) {

        currentDate = times.get(position);

        if(scrolledToRight && (position > (titles.size() - 3))) { //Am Ende sollen immer noch 3 weitere Tage stehen
            for(int i = 0; i < (position - titles.size() + ITEMSAFTER + 4); i++) {
                long time = times.get(times.size() - 1) + (1000 * 60 * 60 * 24);
                times.add(time);
                titles.add(getTitleByTime(time));
            }

            //Error reported in Google-Play developer console
            try {
                this.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if(position < 1) {

            for(int i = 0; i < ITEMSBEFOR; i++) {
                long time = times.get(0) - (1000 * 60 * 60 * 24);
                times.add(0, time);
                titles.add(0, getTitleByTime(time));
            }

            //reset fragments
            for(int i = 0; i < fragments.length; i++) {
                fragments[i] = null;
            }

            //Error reported in Google-Play developer console
            try {
                this.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return position + ITEMSBEFOR;
        }

        return position;
    }

    public long getCurrentDate() {
        return currentDate;
    }
}
