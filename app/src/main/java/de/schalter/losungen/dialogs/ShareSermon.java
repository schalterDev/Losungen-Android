package de.schalter.losungen.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import de.schalter.losungen.Losung;
import de.schalter.losungen.MainActivity;
import de.schalter.losungen.R;
import de.schalter.losungen.files.DBHandler;
import de.schalter.losungen.rss.SermonUrl;

/**
 * Created by martin on 06.04.17.
 */
public class ShareSermon {

    private AlertDialog.Builder builder;
    private Context context;

    /**
     * The dialog let the user choose between sharing the url to the sermon or the mp3 file
     * Maybe the file has to be downloaded first
     * @param context application context
     * @param losung reference to the actual daily word to which the sermon should be shared
     */
    public ShareSermon(final Context context, final Losung losung) {

        this.context = context;

        final String[] items = new String[2];
        items[0] = context.getResources().getString(R.string.share_url);
        items[1] = context.getResources().getString(R.string.share_mp3File);

        builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.sermon_share);
        builder.setCancelable(true);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which) {
                    case 0: //share url
                        MainActivity.toast(context, R.string.fetching_url, Toast.LENGTH_SHORT);
                        SermonUrl sermonUrl = new SermonUrl(context, losung.getDate(), new SermonUrl.SermonUrlListener() {
                            @Override
                            public void urlFound(String url) {
                                if(url != null)
                                    MainActivity.share(context, url);
                                else
                                    MainActivity.toast(context, R.string.need_internet, Toast.LENGTH_LONG);
                            }
                        });
                        sermonUrl.load();
                        break;
                    case 1: //share mp3
                        DBHandler dbHandler = DBHandler.newInstance(context);

                        //Check if audio-file exists allready
                        String pathAudioLosung = dbHandler.getAudioLosungen(losung.getDate());
                        if(pathAudioLosung != null) { //Es wurde bereits ein Pfad gespeichert
                            //Es kann aber immer noch sein, dass der Pfad nicht mehr stimmt
                            //Wenn zum Beispiel die SD-Karte entfernt wurde
                            //Deswegen wird überprüft ob die Datei existiert
                            File file = new File(pathAudioLosung);
                            if(!file.exists()) {
                                downloadMp3();
                            } else {
                                Log.d("Losungen", "share mp3, path: " + pathAudioLosung);
                                shareMp3(pathAudioLosung);
                            }
                        } else {
                            downloadMp3();
                        }
                }
            }
        });
    }

    private void downloadMp3() {
        MainActivity.toast(context, R.string.please_download_sermon_first, Toast.LENGTH_LONG);
        //TODO download mp3 and share file
    }

    private void shareMp3(String path) {
        Uri shareUri = FileProvider.getUriForFile(context, "de.schalter.losungen.provider", new File(path));

        Intent intentAudios = new Intent();
        intentAudios.setAction(Intent.ACTION_SEND);
        List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(intentAudios, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            context.grantUriPermission(packageName, shareUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("audio/*");
        share.putExtra(Intent.EXTRA_STREAM, shareUri);

        context.startActivity(Intent.createChooser(share, context.getResources().getString(R.string.share_mp3File)));
    }

    public void show() {
        builder.show();
    }

}
