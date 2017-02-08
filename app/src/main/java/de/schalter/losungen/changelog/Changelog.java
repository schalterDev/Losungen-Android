package de.schalter.losungen.changelog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.widget.ListView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.schalter.losungen.R;

/**
 * Created by Smarti on 14.01.2016.
 */
public class Changelog {

    private List<ChangelogElement> changelogElementList;
    private Context context;

    public Changelog(Context context) {
        this.context = context;
        changelogElementList = new ArrayList<>();
    }

    /**
     * Parses the XML-File and saves in a List
     */
    public void prepair(String language) throws IOException {
        String file = "changelog/" + language + "/changelog";
        InputStream in = context.getResources().getAssets().open(file + ".xml");
        XmlParserChangelog xmlParser = new XmlParserChangelog();
        xmlParser.parseXML(in);
        changelogElementList = xmlParser.getChanges();
    }

    /**
     * shows a Dialog displaying all Changes in a ScrollView
     */
    public Dialog getDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.changelog_title);

        ListView modeList = new ListView(context);
        ChangelogAdapter adapter = new ChangelogAdapter(context, changelogElementList);
        modeList.setAdapter(adapter);

        builder.setView(modeList);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        return builder.create();
    }

    public static int getOldVersion(Context context, String versionTag) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getInt(versionTag, -1);

    }

    public boolean isNewVersion(String versionTag) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        int oldVersionCode = settings.getInt(versionTag, -1);

        int versionCode = 0;
        try {
            versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(),
                    0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return true;
        }

        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(versionTag, versionCode);
        editor.apply();

        return !(oldVersionCode == versionCode);
    }
}
