package de.schalter.losungen.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;

import de.schalter.losungen.R;

/**
 * Created by martin on 15.11.16.
 */

public class OpenUrlDialog {

    private final AlertDialog.Builder builder;

    /**
     * The dialog let the user choose between different urls to open
     * @param items the name of the Items to choose
     * @param urls the url of the Items
     */
    public OpenUrlDialog(final Context context, final String[] items, final String[] urls) {
        builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.open_in_browser));
        builder.setCancelable(true);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                openUrl(context, urls[which]);
            }
        });
    }

    public void show() {
        builder.show();
    }

    private void openUrl(Context context, String url) {
        Uri uri = Uri.parse(url);
        context.startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }
}
