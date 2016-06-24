package de.schalter.losungen.intro;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import de.schalter.losungen.AnalyticsApplication;
import de.schalter.losungen.R;
import de.schalter.losungen.services.Notifications;
import de.schalter.losungen.settings.Tags;

/**
 * Created by marti on 05.11.2015.
 */
public class FragmentIntroSwitch extends Fragment {

    private String title;
    private String subtitle;
    private String prefTag;
    private String switchText;
    private int drawableResource;
    private boolean checked;
    private int color;

    private SharedPreferences settings;

    private TextView txt_title;
    private TextView txt_subtitle;
    private Switch switch_pref;
    private ImageView imageView;
    private LinearLayout linearLayout;

    private Tracker mTracker;

    private void analytics(String category, String action) {
        if(settings.getBoolean(Tags.PREF_GOOGLEANALYTICS, true)) {
            if (mTracker == null) {
                AnalyticsApplication application = (AnalyticsApplication) getActivity().getApplication();
                mTracker = application.getDefaultTracker();
            }

            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory(category)
                    .setAction(action)
                    .build());

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_slide_notifi, container, false);

        try {
            init(v);
        } catch (NullPointerException ignored) {

        }
        
        return v;
    }

    private void init(View v) throws NullPointerException{
        settings = PreferenceManager.getDefaultSharedPreferences(v.getContext());

        txt_title = (TextView) v.findViewById(R.id.textView_intro_title);
        txt_subtitle = (TextView) v.findViewById(R.id.textView_intro_subtitle);
        switch_pref = (Switch) v.findViewById(R.id.switch_intro);
        imageView = (ImageView) v.findViewById(R.id.imageView_intro);

        txt_title.setText(title);
        txt_subtitle.setText(subtitle);
        switch_pref.setText(switchText);
        imageView.setImageResource(drawableResource);

        // - Init Switch -
        switch_pref.setChecked(checked);

        switch_pref.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(prefTag != null) {
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean(prefTag, isChecked);
                    editor.apply();

                    if (prefTag.equals(Tags.PREF_NOTIFICATION)) {
                        if (isChecked)
                            Notifications.setNotifications(FragmentIntroSwitch.this.getContext(), 7 * 60 * 60 * 1000);
                        else
                            Notifications.removeNotifications(FragmentIntroSwitch.this.getContext());
                    }

                    analytics("Settings", "Intro: " + prefTag + ", " + isChecked);
                }
            }
        });

        linearLayout = (LinearLayout) v.findViewById(R.id.linear_layout_intro);
        linearLayout.setBackgroundColor(color);

        if(prefTag.equals(Tags.PREF_NOTIFICATION)) {
            if(checked)
                Notifications.setNotifications(FragmentIntroSwitch.this.getContext(), 7 * 60 * 60 * 1000);
            else
                Notifications.removeNotifications(FragmentIntroSwitch.this.getContext());
        }
        // ------------

        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(prefTag, checked);
        editor.apply();
    }

    public static FragmentIntroSwitch newInstance(String title, String subtitle, int color,
                                           int drawableResource, String switchText,
                                           String prefTag, boolean checked) {

        FragmentIntroSwitch fragment = new FragmentIntroSwitch();
        fragment.setTitle(title);
        fragment.setSubtitle(subtitle);
        fragment.setDrawableResource(drawableResource);
        fragment.setSwitchText(switchText);
        fragment.setPrefTag(prefTag);
        fragment.setChecked(checked);
        fragment.setColor(color);

        return fragment;
    }


    public void setTitle(String title) {
        this.title = title;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public void setPrefTag(String prefTag) {
        this.prefTag = prefTag;
    }

    public void setSwitchText(String switchText) {
        this.switchText = switchText;
    }

    public void setDrawableResource(int drawableResource) {
        this.drawableResource = drawableResource;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
