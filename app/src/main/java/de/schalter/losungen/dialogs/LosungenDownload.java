package de.schalter.losungen.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import de.schalter.losungen.R;

/**
 * Created by martin on 14.11.16.
 */

public class LosungenDownload {

    private int year;
    private String language;

    public LosungenDownload(int year, String language) {
        this.year = year;
        this.language = language;
    }

    public void openDialog(Context context) {
        // get download_losung_dialog.xml view
        LayoutInflater li = LayoutInflater.from(context);
        View dialogView = li.inflate(R.layout.download_losung_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        // set downlaod_losung_dialog.xml to alertdialog builder
        alertDialogBuilder.setView(dialogView);

        alertDialogBuilder.setCancelable(false);

        alertDialogBuilder.setPositiveButton(R.string.download, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        alertDialogBuilder.show();

    }


}
