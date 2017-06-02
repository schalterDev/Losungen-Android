package de.schalter.losungen.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

import de.schalter.losungen.Losung;
import de.schalter.losungen.MainActivity;
import de.schalter.losungen.R;
import de.schalter.losungen.files.DBHandler;

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
                        losung.getSermonUrlDownload(context, new Runnable() {
                            @Override
                            public void run() {
                                String url = losung.getUrlForDownload();
                                if(url != null)
                                    MainActivity.share(context, url);
                                else
                                    MainActivity.toast(context, R.string.need_internet, Toast.LENGTH_LONG);
                            }
                        });
                        break;
                    case 1: //share mp3
                        DBHandler dbHandler = DBHandler.newInstance(context);

                        //Check if audio-file exists allready
                        String pathAudioLosung = dbHandler.getAudioLosungen(losung.getDatum());
                        if(pathAudioLosung != null) { //Es wurde bereits ein Pfad gespeichert
                            //Es kann aber immer noch sein, dass der Pfad nicht mehr stimmt
                            //Wenn zum Beispiel die SD-Karte entfernt wurde
                            //Deswegen wird überprüft ob die Datei existiert
                            File file = new File(pathAudioLosung);
                            if(!file.exists()) {
                                downloadMp3();
                            } else {
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
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("audio/*");
        share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///" + path));

        Log.d("Losungen", "URL: " + Uri.parse("file:///" + path));

        context.startActivity(Intent.createChooser(share, context.getResources().getString(R.string.share_mp3File)));
    }

    public void show() {
        builder.show();
    }

}
