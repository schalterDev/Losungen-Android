package de.schalter.losungen.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Arrays;

import de.schalter.losungen.files.DBHandler;
import de.schalter.losungen.network.Network;
import de.schalter.losungen.settings.Tags;

/**
 * Created by Smarti on 15.01.2016.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

        boolean languageSupported = Arrays.asList(Tags.ANDACHT_LANGUAGES).contains(Tags.getLanguage(context));

        boolean wifi = Tags.isWifiConnected(context);
        boolean mobile = Tags.isMobileConnected(context);
        //Wegen der Sprache wird hier "false" als Standartwert ausgewählt
        //Für andere Sprachen is das nämlich noch nicht vefügbar
        boolean downloadAudio = settings.getBoolean(Tags.PREF_AUDIO_DOWNLOAD, false);
        boolean autoDownloadAudio = settings.getBoolean(Tags.PREF_AUDIO_AUTODOWNLOAD, false);
        boolean onlyWifi = settings.getString(Tags.PREF_AUDIO_AUTODOWNLOAD_NETWORK, "0").equals("0");

        DBHandler dbHandler = DBHandler.newInstance(context);
        long datumJetzt = System.currentTimeMillis();
        final long datum = dbHandler.getLosung(datumJetzt).getDate();

        if(autoDownloadAudio && downloadAudio && wifi && languageSupported) {
            Network.downloadSermon(context, null, datum);
        } else if(autoDownloadAudio && downloadAudio && !onlyWifi && mobile && languageSupported) {
            Network.downloadSermon(context, null, datum);
        }

    }
}
