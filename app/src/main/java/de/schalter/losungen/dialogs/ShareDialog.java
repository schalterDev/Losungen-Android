package de.schalter.losungen.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import de.schalter.losungen.MainActivity;
import de.schalter.losungen.R;

/**
 * Created by martin on 15.11.16.
 */

public class ShareDialog {

    private AlertDialog.Builder builder;

    /**
     * The dialog let the user choose between different items to share (simple text share)
     * @param items the name of the items
     * @param titles the title of the items
     * @param contents the content of the items
     */
    public ShareDialog(final Context context, final String[] items, final String[] titles,
                                final String[] contents) {
        builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.share));
        builder.setCancelable(true);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MainActivity.share(context, titles[which], contents[which]);
            }
        });
    }

    public void show() {
        builder.show();
    }

}
