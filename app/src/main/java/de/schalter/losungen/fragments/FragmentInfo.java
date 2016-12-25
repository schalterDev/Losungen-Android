package de.schalter.losungen.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import de.schalter.losungen.AnalyticsApplication;
import de.schalter.losungen.R;
import de.schalter.losungen.settings.Tags;

/**
 * Created by marti on 27.10.2015.
 */
public class FragmentInfo extends Fragment {

    private SharedPreferences settings;

    /**
     * @return new FragmentInfo
     */
    public static FragmentInfo newInstance() {
        return new FragmentInfo();
    }

    //Send new screen to google analytics
    private void analytics() {
        if(settings.getBoolean(Tags.PREF_GOOGLEANALYTICS, true)) {
            // Obtain the shared Tracker instance.
            AnalyticsApplication application = (AnalyticsApplication) getActivity().getApplication();
            Tracker mTracker = application.getDefaultTracker();

            mTracker.setScreenName("Fragment-Info");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);

        settings = PreferenceManager.getDefaultSharedPreferences(getContext());
        analytics();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_info, container,
                false);

        initialise(view);

        return view;
    }

    private void initialise(View view) {
        //Until now there is everything defined in the XML-File
    }
}
