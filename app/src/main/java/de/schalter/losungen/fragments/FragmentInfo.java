package de.schalter.losungen.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.schalter.losungen.BuildConfig;
import de.schalter.losungen.R;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);

        settings = PreferenceManager.getDefaultSharedPreferences(getContext());
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
        TextView version = view.findViewById(R.id.textView_version);
        String versionString = BuildConfig.VERSION_NAME;
        version.setText(versionString);
    }
}
